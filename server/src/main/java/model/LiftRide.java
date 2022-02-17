package model;

public class LiftRide {
    Integer resortID;
    String seasonID;
    String dayID;
    Integer skierID;
    Integer time;
    Integer liftID;
    Integer waitTime;

    public LiftRide(Integer resortID, String seasonID, String dayID, Integer skierID,
            Integer time, Integer liftID, Integer waitTime) {
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.skierID = skierID;
        this.time = time;
        this.liftID = liftID;
        this.waitTime = waitTime;
    }
}
