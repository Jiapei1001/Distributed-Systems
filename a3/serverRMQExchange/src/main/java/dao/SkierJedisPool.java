package dao;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SkierJedisPool {

    // private static final String SKIER_REDIS_HOST = "localhost";   // "127.0.0.1"
    private static final String SKIER_REDIS_HOST = "3.91.79.59";
    private static final int SKIER_REDIS_PORT = 6379;

    private JedisPoolConfig poolConfig;
    private JedisPool jedisPool;

    public SkierJedisPool() throws Exception {
        initialJedisPool();
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    private void initialJedisPool() throws Exception {
        try {
            // Easier way of using connection pool
            // Reference - https://github.com/redis/jedis
            // JedisPooled jedis = new JedisPooled("127.0.0.1", 6379);

            // Jedis Config - https://gist.github.com/JonCole/925630df72be1351b21440625ff2671f
            // the above simple JedisPooled way doesn't work for poolConfig, thus still need to use try block
            poolConfig = new JedisPoolConfig();

            // Each thread trying to access Redis needs its own Jedis instance from the pool.
            // Using too small a value here can lead to performance problems, too big and you have wasted resources.
            poolConfig.setMaxTotal(512);

            // int maxIdle = 1024;
            // poolConfig.setMaxIdle(maxIdle);

            // Using "false" here will make it easier to debug when your maxTotal/minIdle/etc settings need adjusting.
            // Setting it to "true" will result better behavior when unexpected load hits in production
            // poolConfig.setBlockWhenExhausted(true);

            // How long to wait before throwing when pool is exhausted
            // long operationTimeout = 10 * 1000;
            // poolConfig.setMaxWaitMillis(operationTimeout);

            // // This controls the number of connections that should be maintained for bursts of load.
            // // Increase this value when you see pool.getResource() taking a long time to complete under burst scenarios
            poolConfig.setMinIdle(64);

            jedisPool = new JedisPool(poolConfig, SKIER_REDIS_HOST, SKIER_REDIS_PORT, 10 * 1000,
                    "default", "admin123456!!");
            // jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
        } catch (Exception e) {
            throw new Exception("First create JedisPool error : " + e);
        }
    }
}
