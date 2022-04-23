package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ResortDao {

    private static final int MINUTES_PER_HOUR = 60;
    private static JedisPool jedisPool;

    public ResortDao() throws Exception {
        jedisPool = ResortJedisPool.getJedisPool();
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