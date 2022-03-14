import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Consumers {

    private static final String HOST = "3.83.152.143";
    private static final int PORT = 5672;
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";

    private static final int NUM_THREADS = 256;
    private static Map<Integer, List<String>> map = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        // factory.setHost("localhost");
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USER);
        factory.setPassword(PASSWORD);

        // Reference - https://github.com/gortonator/bsds-6650/blob/master/code/week-2/producerconsumerex/ProducerConsumerEx.java
        // Alternative way is to initiate and define a Runnable in this main class
        for (int i = 0; i < NUM_THREADS; i++) {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();

            new Thread(new SkierConsumer(channel, map)).start();
        }
    }
}