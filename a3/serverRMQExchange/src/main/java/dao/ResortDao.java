package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ResortDao {

    private static final int MINUTES_PER_HOUR = 60;
    private JedisPool jedisPool;

    public ResortDao(JedisPool jedisPool) throws Exception {
        this.jedisPool = jedisPool;
    }

    // Get hour's index lot, total range as 9:00 am to 4:00 pm
    // "time": 217 -> 217 % 60 -> index lot 4
    private Integer getHourLot(Integer time) {
        return (time % MINUTES_PER_HOUR);
    }

    // Query #1 - How many unique skiers visited resort X on day N?
    public int getUniqueSkiersPerDay(String resortID, String seasonID, String dayID) {
        int res;
        String Resort_Season_Day = "Resort_" + resortID + "_Season_" + seasonID + "_Day_" + dayID;

        // SET, SCARD key, Returns the set cardinality (number of elements) of the set stored at
        try (Jedis jedis = jedisPool.getResource()) {
            // SCARD - Integer reply: the cardinality (number of elements) of the set, or 0 if key does not exist.
            res = (int) jedis.scard(Resort_Season_Day);
        }
        return res;
    }

    // Query #2 - How many rides on lift N happened on day N?
    public int getNumOfRidesPerDay(String resortID, String seasonID, String dayID) {
        int res;
        String Resort_Season_Day = "Resort_" + resortID + "_Season_" + seasonID + "_Day_" + dayID;
        String Rides_Resort_Season_Day = "Ride_" + Resort_Season_Day;

        // MAP
        try (Jedis jedis = jedisPool.getResource()) {
            res = Integer.parseInt(jedis.get(Rides_Resort_Season_Day));
        }
        return res;
    }

    // Query #3 - On day N, show me how many lift rides took place in each hour of the ski day?
    public Map<Integer, Integer> getNumOfRidesPerHour(String resortID, String seasonID,
            String dayID) {
        Map<Integer, Integer> res = new HashMap<>();

        List<String> timeSpots;
        String Resort_Season_Day = "Resort_" + resortID + "_Season_" + seasonID + "_Day_" + dayID;
        String Ride_Hours_Resort_Season_Day = "Hour_" + Resort_Season_Day;

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