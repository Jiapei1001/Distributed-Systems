import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Consumers {

    private static final String HOST = "3.83.152.143";
    private static final int PORT = 5672;
    private static final String USER = "admin";
    private static final String PASSWORD = "admin";

    private static final String REDIS_HOST = "127.0.0.1";
    private static final int REDIS_PORT = 6379;

    private static final int NUM_THREADS = 256;

    private static JedisPoolConfig poolConfig;
    private static JedisPool jedisPool;

    // Jedis Config - https://gist.github.com/JonCole/925630df72be1351b21440625ff2671f
    private static void initialJedisPool() throws Exception {
        try {
            poolConfig = new JedisPoolConfig();

            // Each thread trying to access Redis needs its own Jedis instance from the pool.
            // Using too small a value here can lead to performance problems, too big and you have wasted resources.
            int maxConnections = 512;
            poolConfig.setMaxTotal(maxConnections);
            int maxIdle = 256;
            poolConfig.setMaxIdle(maxIdle);

            // Using "false" here will make it easier to debug when your maxTotal/minIdle/etc settings need adjusting.
            // Setting it to "true" will result better behavior when unexpected load hits in production
            poolConfig.setBlockWhenExhausted(true);

            // How long to wait before throwing when pool is exhausted
            long operationTimeout = 5000;
            poolConfig.setMaxWaitMillis(operationTimeout);

            // This controls the number of connections that should be maintained for bursts of load.
            // Increase this value when you see pool.getResource() taking a long time to complete under burst scenarios
            poolConfig.setMinIdle(50);

            jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
        } catch (Exception e) {
            throw new Exception("First create JedisPool error : " + e);
        }
    }

    public static void main(String[] args) throws Exception {
        // NOTE: JedisFactory.getInstance() factory method is not thread-safe.
        // the pool instance is thread safe

        // Easier way of using connection pool
        // Reference - https://github.com/redis/jedis
        // JedisPooled jedis = new JedisPooled("127.0.0.1", 6379);

        // the above simple JedisPooled way doesn't work for poolConfig, thus still need to use try block
        initialJedisPool();

        // ConnectionFactory for RabbitMQ
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

            new Thread(new SkierConsumer(channel, jedisPool)).start();
        }

    }
}