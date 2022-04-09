import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.LiftRideDaoResort;

public class Main {

    // private static final String MQ_HOST = "localhost";  // "127.0.0.1"
    private static final String MQ_HOST = "3.86.161.121";
    private static final int MQ_PORT = 5672;
    private static final String MQ_USER = "admin123456";
    private static final String MQ_PASSWORD = "123456";

    private static final int NUM_THREADS = 512;

    public static void main(String[] args) throws Exception {
        // Jedis connection
        LiftRideDaoResort daoResort = new LiftRideDaoResort();

        // RabbitMQ connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MQ_HOST);
        factory.setPort(MQ_PORT);
        factory.setUsername(MQ_USER);
        factory.setPassword(MQ_PASSWORD);

        Connection conn = factory.newConnection();
        for (int i = 0; i < NUM_THREADS; i++) {
            Channel channel = conn.createChannel();

            new Thread(new ResortConsumer(channel, daoResort)).start();
        }
    }
}
