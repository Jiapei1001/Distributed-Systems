import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.LiftRide;
import model.LiftRideDao;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

public class SkierConsumer implements Runnable {

    private static final String SKIER_QUEUE_NAME = "skier_message_queue";
    private final Channel channel;
    private final JedisPool jedisPool;
    private final Gson gson;

    public SkierConsumer(Channel channel, LiftRideDao liftRideDao) {
        this.channel = channel;
        this.jedisPool = liftRideDao.getJedisPool();
        this.gson = new Gson();
    }

    @Override
    public void run() {
        // Interactive Guide - https://try.redis.io/
        // Jedis JSON - https://github.com/redis/jedis/blob/master/docs/redisjson.md
        // Here the try block avoid closing the resource, or there is a need of adding Finally

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

            channel.basicConsume(SKIER_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            Logger.getLogger(SkierConsumer.class.getName()).log(Level.INFO, e.getMessage());
        }
    }

    private void process(String message) {
        // JsonObject json = this.gson.fromJson(message, JsonObject.class);
        LiftRide r = this.gson.fromJson(message, LiftRide.class);

        Jedis jedis = jedisPool.getResource();
        try {
            // For skier N, how many days have they skied this season?
            // SkierN_Season, a SET that stores the days. As the elements in the set is unique, we can use the set's size to answer the question above.
            String Skier_Season = "Skier_" + r.skierID + "_Season_" + r.seasonID;
            jedis.sadd(Skier_Season, r.dayID);

            // For skier N, what are the vertical totals for each ski day? (calculate vertical as liftID*10)
            // Redis HASH, SkierN -> skiDay : vertical.
            // HINCRBY, HGETALL, HKEYS, HVALS
            int increment = r.liftID * 10;
            String Skier = "Skier_" + r.skierID;
            jedis.hincrBy(Skier, r.seasonID + "_" + r.dayID, increment);

            // For skier N, show me the lifts they rode on each ski day
            // Redis SET, append the liftRide to the set of the key
            String Skier_Season_Day =
                    "Skier_" + r.skierID + "_Season_" + r.seasonID + "_Day_" + r.dayID;
            jedis.sadd(Skier_Season_Day, r.liftID.toString());

        } catch (JedisException e) {
            if (jedis != null) {
                jedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            jedisPool.returnResource(jedis);
        }

        // Redis LIST, append to the key, SkierN
        System.out.println(r);
        System.out.println(" [" + r.skierID + "] Done");
    }
}