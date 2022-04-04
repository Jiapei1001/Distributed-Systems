import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.UnifiedJedis;

public class SkierConsumer implements Runnable {

    private static final String SKIER_QUEUE_NAME = "skier_message_queue";
    private final Channel channel;
    private final JedisPool jedisPool;
    private final Gson gson;

    public SkierConsumer(Channel channel, JedisPool jedisPool) {
        this.channel = channel;
        this.jedisPool = jedisPool;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            channel.queueDeclare(SKIER_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for skier messages. To exit press CTRL+C");

            // basic.qos method to make it possible to limit the number of unacknowledged messages on a channel (or connection) when consuming (aka "prefetch count").
            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                process(message);

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(SKIER_QUEUE_NAME, false, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(String message) {
        JsonObject json = this.gson.fromJson(message, JsonObject.class);

        String skierID = String.valueOf(json.get("skierID"));
        String seasonID = String.valueOf(json.get("seasonID"));
        String dayID = String.valueOf(json.get("dayID"));
        String liftID = String.valueOf(json.get("liftID"));

        // Interactive Guide - https://try.redis.io/
        // Jedis JSON - https://github.com/redis/jedis/blob/master/docs/redisjson.md
        // Here the try block avoid closing the resource, or there is a need of adding Finally
        try (Jedis jedis = this.jedisPool.getResource()) {
            // For skier N, how many days have they skied this season?
            // SkierN_Season, a SET that stores the days. As the elements in the set is unique, we can use the set's size to answer the question above.
            String SkierN_Season = skierID + "_" + seasonID;
            jedis.sadd(SkierN_Season, dayID);

            // For skier N, what are the vertical totals for each ski day? (calculate vertical as liftID*10)
            // Redis HASH, SkierN -> skiDay : vertical.
            // HINCRBY, HGETALL, HKEYS, HVALS
            int increment = Integer.parseInt(liftID) * 10;
            jedis.hincrBy(skierID, dayID, increment);

            // For skier N, show me the lifts they rode on each ski day
            // Redis LIST, append the liftRide to the list of the key, SkierN_SkiDay
            // append the liftRide to a list
            jedis.rpush("liftRides", json.toString());
        }

        // Redis LIST, append to the key, SkierN
        System.out.println(json.toString());
        System.out.println(" [" + skierID + "] Done");
    }
}