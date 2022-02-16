package model;

public class LiftRide {
    Integer time;
    Integer liftID;
    Integer waitTime;

    public LiftRide(Integer time, Integer liftID, Integer waitTime) {
        this.time = time;
        this.liftID = liftID;
        this.waitTime = waitTime;
    }
}
