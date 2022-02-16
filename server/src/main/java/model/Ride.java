package model;

public class Ride {
    Integer resortID;
    String seasonID;
    String dayID;
    Integer skierID;
    Integer vertical;

    public Ride(Integer resortID, String seasonID, String dayID, Integer skierID,
            Integer vertical) {
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.skierID = skierID;
        this.vertical = vertical;
    }
}
