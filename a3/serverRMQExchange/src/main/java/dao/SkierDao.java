package dao;


import com.google.gson.Gson;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SkierDao {

    // private static final String SKIER_REDIS_HOST = "localhost";   // "127.0.0.1"
    private static final String SKIER_REDIS_HOST = "3.92.77.251";
    private static final int SKIER_REDIS_PORT = 6379;

    private static JedisPoolConfig poolConfig;
    private static JedisPool jedisPool;
    private final Gson gson;

    public SkierDao() throws Exception {
        initialJedisPool();
        this.gson = new Gson();
    }

    private static void initialJedisPool() throws Exception {
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
                    "default", "admin123456");
            // jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
        } catch (Exception e) {
            throw new Exception("First create JedisPool error : " + e);
        }
    }

    // Query #1 - For skier N, how many days have they skied this season?
    public int getSkiDaysThisSeason(String skierID, String seasonID) {
        long res = -1;

        String Skier_Season = "Skier_" + skierID + "_Season_" + seasonID;
        try (Jedis jedis = jedisPool.getResource()) {
            // set length
            res = jedis.llen(Skier_Season);
        }

        return (int) res;
    }

    // Query #2 - For skier N, what are the vertical totals for each ski day? (calculate vertical as liftID*10)
    public int getVerticalTotalsPerDay(String skierID, Integer resortID, String seasonID,
            String dayID) {
        Integer res = null;

        String Skier_Resort = "Skier_" + skierID + "_Resort_" + resortID;
        String Season_Day = seasonID + "_" + dayID;
        try (Jedis jedis = jedisPool.getResource()) {
            // hash, key, field
            res = Integer.parseInt(jedis.hget(Skier_Resort, Season_Day));
        }

        return res;
    }

    // For skier N, get the total vertical for the skier the specified resort.
    public Map<String, Integer> getVerticalTotalsPerResort(String skierID, Integer resortID) {
        Map<String, Integer> res = new HashMap<>();

        Map<String, String> seasonVerticals;
        String Skier_Resort = "Skier_" + skierID + "_Resort_" + resortID;
        try (Jedis jedis = jedisPool.getResource()) {
            // hash, key, field
            seasonVerticals = jedis.hgetAll(Skier_Resort);
        }

        for (Map.Entry<String, String> e : seasonVerticals.entrySet()) {
            res.put(e.getKey(), res.getOrDefault(e.getKey(), 0) + Integer.parseInt(e.getValue()));
        }

        return res.isEmpty() ? null : res;
    }

    // Query #3 - For skier N, show me the lifts they rode on each ski day
    public Set<String> getLifts(String skierID, String seasonID, String dayID) {
        Set<String> res = new HashSet<>();

        String Skier_Season_Day = "Skier_" + skierID + "_Season_" + seasonID + "_Day_" + dayID;
        try (Jedis jedis = jedisPool.getResource()) {
            // set members
            res = jedis.smembers(Skier_Season_Day);
        }

        return res;
    }
}
