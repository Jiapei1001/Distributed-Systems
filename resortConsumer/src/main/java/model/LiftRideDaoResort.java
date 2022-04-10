package model;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class LiftRideDaoResort {

    // private static final String RESORT_REDIS_HOST = "localhost";   // "127.0.0.1"
    private static final String RESORT_REDIS_HOST = "18.209.62.2";
    private static final int RESORT_REDIS_PORT = 6379;

    private static JedisPoolConfig poolConfig;
    private static JedisPool jedisPool;
    private final Gson gson;

    private static final int MINUTES_PER_HOUR = 60;

    public LiftRideDaoResort() throws Exception {
        initialJedisPool();
        this.gson = new Gson();
    }

    // How to avoid failure when connecting to Redis instance?
    // 1.   Open and add port number to the instance's security group
    // 2.   Redis config: 1. comment out BIN 127.0.0.1; 2. protected-mode no
    //      https://stackoverflow.com/questions/37867633/cannot-connect-to-redis-using-jedis
    // 3.   Increase connection timeout, default is 2000 ms
    //      https://stackoverflow.com/questions/14993644/configure-jedis-timeout
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
            poolConfig.setMaxTotal(20480);

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
            poolConfig.setMinIdle(10000);

            jedisPool = new JedisPool(poolConfig, RESORT_REDIS_HOST, RESORT_REDIS_PORT, 10 * 1000);
            // jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
        } catch (Exception e) {
            throw new Exception("First create JedisPool error : " + e);
        }
    }

    // process and save message to db
    public void process(String message) {
        // JsonObject json = this.gson.fromJson(message, JsonObject.class);
        LiftRide r = this.gson.fromJson(message, LiftRide.class);

        // Jedis jedis = jedisPool.getResource();
        try (Jedis jedis = jedisPool.getResource()) {
            // How many unique skiers visited resort X on day N?
            // Resort_Season_Day, as a SET
            String Resort_Season_Day = r.resortID + "_" + r.seasonID + "_" + r.dayID;
            String skier = String.valueOf(r.skierID);
            jedis.sadd(Resort_Season_Day, skier);

            // How many rides on lift N happened on day N?
            // MAP, Rides_Resort_Season_Day, increment by 1
            String Rides_Resort_Season_Day = "R_" + Resort_Season_Day;
            jedis.incr(Rides_Resort_Season_Day);

            // On day N, show me how many lift rides took place in each hour of the ski day
            // HASH, Hours_Resort_Season_Day -> Hour : increment by 1
            String Ride_Hours_Resort_Season_Day = "H_" + Resort_Season_Day;
            // too much time
            // String Hour = getHourLot(r.time);
            // jedis.hincrBy(Ride_Hours_Resort_Season_Day, Hour, 1);
            String time = String.valueOf(r.time);
            jedis.rpush(Ride_Hours_Resort_Season_Day, time);

            // String key = String.valueOf(r.resortID);
            // String data = r.resortID + "_" + r.seasonID + "_" + r.dayID + "_" + r.time;
            // jedis.rpush(key, data);
        }

        // Redis LIST, append to the key, SkierN
        System.out.println(r);
        System.out.println(" [" + r.skierID + "] Done");
    }

    // Get hour's index lot, total range as 9:00 am to 4:00 pm
    // "time": 217 -> 217 % 60 -> index lot 4
    private String getHourLot(Integer time) {
        return String.valueOf(time % MINUTES_PER_HOUR);
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
        String Rides_Resort_Season_Day = "Rides_" + Resort_Season_Day;

        // MAP
        try (Jedis jedis = jedisPool.getResource()) {
            res = Integer.parseInt(jedis.get(Rides_Resort_Season_Day));
        }
        return res;
    }

    // Query #3 - On day N, show me how many lift rides took place in each hour of the ski day?
    public Map<String, String> getNumOfRidesPerHour(String resortID, String seasonID, String dayID) {
        Map<String, String> res;
        String Resort_Season_Day = resortID + "_" + seasonID + "_" + dayID;
        String Ride_Hours_Resort_Season_Day = "Ride_Hours_" + Resort_Season_Day;

        // HASH
        try (Jedis jedis = jedisPool.getResource()) {
            // hash: key, field, value
            res = jedis.hgetAll(Ride_Hours_Resort_Season_Day);
        }

        return res;
    }
}