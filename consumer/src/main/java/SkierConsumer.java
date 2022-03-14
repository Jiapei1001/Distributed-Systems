import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkierConsumer implements Runnable {

    private static final String SKIER_QUEUE_NAME = "skier_message_queue";
    private final Channel channel;
    private Map<Integer, List<String>> map;
    private final Gson gson;

    public SkierConsumer(Channel channel, Map<Integer, List<String>> map) {
        this.channel = channel;
        this.map = map;
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
        Integer skierID = Integer.valueOf(String.valueOf(json.get("skierID")));
        this.map.computeIfAbsent(skierID, a -> new ArrayList<>()).add(json.toString());

        System.out.println(json.toString());
        System.out.println(" [" + skierID + "] Done");
    }
}