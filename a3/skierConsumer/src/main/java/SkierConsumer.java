import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.LiftRideDaoSkier;

public class SkierConsumer implements Runnable {

    // bind a queue to an exchange
    private static final String EXCHANGE_NAME = "lift_ride";
    private static final String SKIER_QUEUE_NAME = "skier_message_queue";
    private final Channel channel;
    private final LiftRideDaoSkier liftRideDao;

    public SkierConsumer(Channel channel, LiftRideDaoSkier liftRideDao) {
        this.channel = channel;
        this.liftRideDao = liftRideDao;
    }

    @Override
    public void run() {
        // Interactive Guide - https://try.redis.io/
        // Jedis JSON - https://github.com/redis/jedis/blob/master/docs/redisjson.md
        // Here the try block avoid closing the resource, or there is a need of adding Finally

        try {
            channel.queueDeclare(SKIER_QUEUE_NAME, true, false, false, null);

            // // Exchange & Bind
            // channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            // channel.queueBind(SKIER_QUEUE_NAME, EXCHANGE_NAME, "");
            System.out.println(" [*] Waiting for skier messages for skier db. To exit press CTRL+C");

            // basic.qos method to make it possible to limit the number of unacknowledged messages on a channel (or connection) when consuming (aka "prefetch count").
            channel.basicQos(1);

            boolean autoAck = false;

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                liftRideDao.process(message);

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(SKIER_QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            Logger.getLogger(SkierConsumer.class.getName()).log(Level.INFO, e.getMessage());
        }
    }
}