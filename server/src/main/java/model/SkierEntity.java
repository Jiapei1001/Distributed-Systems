package model;

public class SkierEntity {
    private Integer skierID;
    private Integer resortID;
    private String seasonID;
    private Integer vertical;

    public SkierEntity(Integer skierID, Integer resortID, String seasonID, Integer vertical) {
        this.skierID = skierID;
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.vertical = vertical;
    }

    public Integer getSkierID() {
        return skierID;
    }

    public void setSkierID(Integer skierID) {
        this.skierID = skierID;
    }

    public Integer getResortID() {
        return resortID;
    }

    public void setResortID(Integer resortID) {
        this.resortID = resortID;
    }

    public String getSeasonID() {
        return seasonID;
    }

    public void setSeasonID(String seasonID) {
        this.seasonID = seasonID;
    }

    public Integer getVertical() {
        return vertical;
    }

    public void setVertical(Integer vertical) {
        this.vertical = vertical;
    }
}
