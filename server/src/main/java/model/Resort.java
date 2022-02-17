package model;

import java.util.HashSet;
import java.util.Set;

public class Resort {
    private Integer resortID;
    private String resortName;
    private Set<Season> seasons;

    public Resort(Integer resortID, String resortName) {
        this.resortID = resortID;
        this.resortName = resortName;
        this.seasons = new HashSet<>();
    }

    public void addSeason(Season s) {
        this.seasons.add(s);
    }

    public Integer getResortID() {
        return resortID;
    }

    public void setResortID(Integer resortID) {
        this.resortID = resortID;
    }

    public String getResortName() {
        return resortName;
    }

    public void setResortName(String resortName) {
        this.resortName = resortName;
    }
}
