package model;

public class LiftRidePayload {

    Integer time;
    Integer liftID;
    Integer waitTime;

    public LiftRidePayload(Integer time, Integer liftID, Integer waitTime) {
        this.time = time;
        this.liftID = liftID;
        this.waitTime = waitTime;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getLiftID() {
        return liftID;
    }

    public void setLiftID(Integer liftID) {
        this.liftID = liftID;
    }

    public Integer getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Integer waitTime) {
        this.waitTime = waitTime;
    }

}