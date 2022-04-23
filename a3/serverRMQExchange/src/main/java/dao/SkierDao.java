package dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SkierDao {

    public static JedisPool jedisPool;

    public SkierDao() throws Exception {
        jedisPool = SkierJedisPool.getJedisPool();
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
    public Map<String, Integer> getVerticalTotalsPerResort(String skierID, String resortID) {
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

    public Map<String, Integer> getVerticalTotalPerResortAndSeason(String skierID, String resortID, String seasonID) {
        Map<String, Integer> seasonVerticals = getVerticalTotalsPerResort(skierID, resortID);

        if (seasonVerticals == null || !seasonVerticals.containsKey(seasonID)) {
            return null;
        }

        Map<String, Integer> res = new HashMap<>();
        res.put(resortID, seasonVerticals.get(seasonID));

        return res;
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
