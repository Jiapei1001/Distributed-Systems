package dao;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class ResortDao {

    // private static final String SKIER_REDIS_HOST = "localhost";   // "127.0.0.1"
    private static final String SKIER_REDIS_HOST = "3.92.77.251";
    private static final int SKIER_REDIS_PORT = 6379;

    private static final int MINUTES_PER_HOUR = 60;
    private static JedisPoolConfig poolConfig;
    private static JedisPool jedisPool;
    private final Gson gson;

    public ResortDao() throws Exception {
        initialJedisPool();
        this.gson = new Gson();
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
                    "default", "admin123456");
            // jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
        } catch (Exception e) {
            throw new Exception("First create JedisPool error : " + e);
        }
    }

    // Get hour's index lot, total range as 9:00 am to 4:00 pm
    // "time": 217 -> 217 % 60 -> index lot 4
    private Integer getHourLot(Integer time) {
        return (time % MINUTES_PER_HOUR);
    }

    // Query #1 - How many unique skiers visited resort X on day N?
    public int getUniqueSkiersPerDay(String resortID, String seasonID, String dayID) {
        int res;
        String Resort_Season_Day = resortID + "_" + seasonID + "_" + dayID;

        // SET, SCARD key, Returns the set cardinality (number of elements) of the set stored at
        try (Jedis jedis = jedisPool.getResource()) {
            res = (int) jedis.scard(Resort_Season_Day);
        }
        return res;
    }

    // Query #2 - How many rides on lift N happened on day N?
    public int getNumOfRidesPerDay(String resortID, String seasonID, String dayID) {
        int res;
        String Resort_Season_Day = resortID + "_" + seasonID + "_" + dayID;
        String Rides_Resort_Season_Day = "R_" + Resort_Season_Day;

        // MAP
        try (Jedis jedis = jedisPool.getResource()) {
            res = Integer.parseInt(jedis.get(Rides_Resort_Season_Day));
        }
        return res;
    }

    // Query #3 - On day N, show me how many lift rides took place in each hour of the ski day?
    public Map<Integer, Integer> getNumOfRidesPerHour(String resortID, String seasonID,
            String dayID) {
        // Map<String, String> res;
        // String Resort_Season_Day = resortID + "_" + seasonID + "_" + dayID;
        // String Ride_Hours_Resort_Season_Day = "R_H_" + Resort_Season_Day;
        //
        // // HASH
        // try (Jedis jedis = jedisPool.getResource()) {
        //     // hash: key, field, value
        //     res = jedis.hgetAll(Ride_Hours_Resort_Season_Day);
        // }

        Map<Integer, Integer> res = new HashMap<>();

        List<String> timeSpots;
        String Resort_Season_Day = resortID + "_" + seasonID + "_" + dayID;
        String Ride_Hours_Resort_Season_Day = "H_" + Resort_Season_Day;

        try (Jedis jedis = jedisPool.getResource()) {
            // get all results from list
            timeSpots = jedis.lrange(Ride_Hours_Resort_Season_Day, 0, -1);
        }

        for (String t : timeSpots) {
            int hourLot = getHourLot(Integer.parseInt(t));
            res.put(hourLot, res.getOrDefault(hourLot, 0) + 1);
        }

        return res;
    }
}