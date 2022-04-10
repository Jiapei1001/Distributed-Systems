package model;

import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class LiftRideDaoSkier {

    // Connection Error Exception
    // redis.clients.jedis.exceptions.JedisDataException: DENIED Redis is running in protected mode because protected mode is enabled, no bind address was specified, no authentication password is requested to clients. In this mode connections are only accepted from the loopback interface. If you want to connect from external computers to Redis you may adopt one of the following solutions: 1) Just disable protected mode sending the command 'CONFIG SET protected-mode no' from the loopback interface by connecting to Redis from the same host the server is running, however MAKE SURE Redis is not publicly accessible from internet if you do so. Use CONFIG REWRITE to make this change permanent. 2) Alternatively you can just disable the protected mode by editing the Redis configuration file, and setting the protected mode option to 'no', and then restarting the server. 3) If you started the server manually just for testing, restart it with the '--protected-mode no' option. 4) Setup a bind address or an authentication password. NOTE: You only need to do one of the above things in order for the server to start accepting connections from the outside.

    // private static final String SKIER_REDIS_HOST = "localhost";   // "127.0.0.1"
    private static final String SKIER_REDIS_HOST = "54.87.193.140";
    private static final int SKIER_REDIS_PORT = 6379;

    private static JedisPoolConfig poolConfig;
    private static JedisPool jedisPool;
    private final Gson gson;

    public LiftRideDaoSkier() throws Exception {
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
            poolConfig.setMinIdle(10240);

            jedisPool = new JedisPool(poolConfig, SKIER_REDIS_HOST, SKIER_REDIS_PORT, 10 * 1000);
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
            // For skier N, how many days have they skied this season?
            // SkierN_Season, a SET that stores the days. As the elements in the set is unique, we can use the set's size to answer the question above.
            // String Skier_Season = "Skier_" + r.skierID + "_Season_" + r.seasonID;
            String Skier_Season = "Skier_" + r.skierID + "_" + r.seasonID;
            jedis.sadd(Skier_Season, r.dayID);

            // For skier N, what are the vertical totals for each ski day? (calculate vertical as liftID*10)
            // Redis HASH, SkierN -> skiDay : vertical.
            // HINCRBY, HGETALL, HKEYS, HVALS
            int increment = r.liftID * 10;
            String Skier = "Skier_" + r.skierID;
            jedis.hincrBy(Skier, r.seasonID + "_" + r.dayID, increment);

            // For skier N, show me the lifts they rode on each ski day
            // Redis SET, append the liftRide to the set of the key
            // String Skier_Season_Day =
            //         "Skier_" + r.skierID + "_Season_" + r.seasonID + "_Day_" + r.dayID;
            String Skier_Season_Day = "Skier_" + r.skierID + "_" + r.seasonID + "_" + r.dayID;
            jedis.sadd(Skier_Season_Day, r.liftID.toString());
        }

        // Redis LIST, append to the key, SkierN
        System.out.println(r);
        System.out.println(" [" + r.skierID + "] Done");
    }

    // Query #1 - For skier N, how many days have they skied this season?
    public int getSkiDaysThisSeason(String skierID, String seasonID) {
        long res = -1;

        String Skier_Season = "Skier_" + skierID + "_" + seasonID;
        try (Jedis jedis = jedisPool.getResource()) {
            // set length
            res = jedis.llen(Skier_Season);
        }

        return (int) res;
    }

    // Query #2 - For skier N, what are the vertical totals for each ski day? (calculate vertical as liftID*10)
    public int getVerticalTotals(String skierID, String seasonID, String dayID) {
        Integer res = null;

        String Skier = "Skier_" + skierID;
        String Season_Day = seasonID + "_" + dayID;
        try (Jedis jedis = jedisPool.getResource()) {
            // hash, key, field
            res = Integer.parseInt(jedis.hget(Skier, Season_Day));
        }
        return res;
    }

    // Query #3 - For skier N, show me the lifts they rode on each ski day
    public Set<String> getLifts(String skierID, String seasonID, String dayID) {
        Set<String> res = new HashSet<>();

        String Skier_Season_Day = "Skier_" + skierID + "_" + seasonID + "_" + dayID;
        try (Jedis jedis = jedisPool.getResource()) {
            // set members
            res = jedis.smembers(Skier_Season_Day);
        }

        return res;
    }
}