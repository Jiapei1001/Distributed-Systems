import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.LiftRideDaoResort;

public class ResortConsumer implements Runnable {
    private static final String SKIER_QUEUE_NAME = "skier_message_queue";
    private final Channel channel;
    private final LiftRideDaoResort daoResort;

    public ResortConsumer(Channel channel, LiftRideDaoResort daoResort) {
        this.channel = channel;
        this.daoResort = daoResort;
    }

    @Override
    public void run() {
        try {
            channel.queueDeclare(SKIER_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for skier messages for resort db. To exit press CTRL+C");

            // basic.qos method to make it possible to limit the number of unacknowledged messages on a channel (or connection) when consuming (aka "prefetch count").
            channel.basicQos(1);
            boolean autoAck = false;

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                daoResort.process(message);

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };

            channel.basicConsume(SKIER_QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            Logger.getLogger(ResortConsumer.class.getName()).log(Level.INFO, e.getMessage());
        }
    }
}
