import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.LiftRideDaoSkier;

public class Main {

    // private static final String MQ_HOST = "localhost";  // "127.0.0.1"
    private static final String MQ_HOST = "44.201.192.31";
    private static final int MQ_PORT = 5672;
    private static final String MQ_USER = "admin123456";
    private static final String MQ_PASSWORD = "123456";

    private static final int NUM_THREADS = 512;

    public static void main(String[] args) throws Exception {
        // NOTE: JedisFactory.getInstance() factory method is not thread-safe.
        // the pool instance is thread safe
        LiftRideDaoSkier liftRideDao = new LiftRideDaoSkier();

        // ConnectionFactory for RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();

        // factory.setHost("localhost");
        factory.setHost(MQ_HOST);
        factory.setPort(MQ_PORT);
        factory.setUsername(MQ_USER);
        factory.setPassword(MQ_PASSWORD);


        // Reference - https://github.com/gortonator/bsds-6650/blob/master/code/week-2/producerconsumerex/ProducerConsumerEx.java
        // Alternative way is to initiate and define a Runnable in this main class
        Connection conn = factory.newConnection();
        for (int i = 0; i < NUM_THREADS; i++) {
            Channel channel = conn.createChannel();

            // bind queue to an exchange
            // https://www.rabbitmq.com/tutorials/tutorial-three-java.html

            new Thread(new SkierConsumer(channel, liftRideDao)).start();
        }
    }
}