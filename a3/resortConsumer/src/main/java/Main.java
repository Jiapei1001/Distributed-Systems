import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.LiftRideDaoResort;

public class Main {

    // Connection Error Exception
    // redis.clients.jedis.exceptions.JedisDataException: DENIED Redis is running in protected mode because protected mode is enabled, no bind address was specified, no authentication password is requested to clients. In this mode connections are only accepted from the loopback interface. If you want to connect from external computers to Redis you may adopt one of the following solutions: 1) Just disable protected mode sending the command 'CONFIG SET protected-mode no' from the loopback interface by connecting to Redis from the same host the server is running, however MAKE SURE Redis is not publicly accessible from internet if you do so. Use CONFIG REWRITE to make this change permanent. 2) Alternatively you can just disable the protected mode by editing the Redis configuration file, and setting the protected mode option to 'no', and then restarting the server. 3) If you started the server manually just for testing, restart it with the '--protected-mode no' option. 4) Setup a bind address or an authentication password. NOTE: You only need to do one of the above things in order for the server to start accepting connections from the outside.

    private static final String MQ_HOST = "127.0.0.1";  // "127.0.0.1"
    // private static final String MQ_HOST = "44.201.192.31";
    private static final int MQ_PORT = 5672;
    private static final String MQ_USER = "admin123456";
    private static final String MQ_PASSWORD = "123456";

    private static final int NUM_THREADS = 800;

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
