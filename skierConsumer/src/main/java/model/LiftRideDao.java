package model;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class LiftRideDao {
    // private static final String REDIS_HOST = "localhost";   // "127.0.0.1"
    private static final String REDIS_HOST = "52.87.191.4";
    private static final int REDIS_PORT = 6379;

    private static JedisPoolConfig poolConfig;
    private static JedisPool jedisPool;

    public LiftRideDao() throws Exception {
        initialJedisPool();
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    // How to avoid failure when connecting to Redis instance?
    // Open and add port number to the instance's security group

    // Redis config: 1. comment out BIN 127.0.0.1; 2. protected-mode no
    // https://stackoverflow.com/questions/37867633/cannot-connect-to-redis-using-jedis

    // Increase connection timeout, default is 2000 ms
    // https://stackoverflow.com/questions/14993644/configure-jedis-timeout

    // Jedis Config - https://gist.github.com/JonCole/925630df72be1351b21440625ff2671f
    private static void initialJedisPool() throws Exception {
        try {
            // Easier way of using connection pool
            // Reference - https://github.com/redis/jedis
            // JedisPooled jedis = new JedisPooled("127.0.0.1", 6379);

            // the above simple JedisPooled way doesn't work for poolConfig, thus still need to use try block
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
            // long operationTimeout = 10 * 1000;
            // poolConfig.setMaxWaitMillis(operationTimeout);

            // // This controls the number of connections that should be maintained for bursts of load.
            // // Increase this value when you see pool.getResource() taking a long time to complete under burst scenarios
            // poolConfig.setMinIdle(256);

            jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT, 10 * 1000);
            // jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
        } catch (Exception e) {
            throw new Exception("First create JedisPool error : " + e);
        }
    }
}
