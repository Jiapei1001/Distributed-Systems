package model;

public class Resort {
    private Integer resortID;
    private String resortName;

    public Resort(Integer resortID, String resortName) {
        this.resortID = resortID;
        this.resortName = resortName;
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
