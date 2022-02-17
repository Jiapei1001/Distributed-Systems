package model;

public class ResortReport {
    private String resortName;
    private Integer numSkiers;

    public ResortReport(String resortName, Integer numSkiers) {
        this.resortName = resortName;
        this.numSkiers = numSkiers;
    }

    public String getResortName() {
        return resortName;
    }

    public void setResortName(String resortName) {
        this.resortName = resortName;
    }

    public Integer getNumSkiers() {
        return numSkiers;
    }

    public void setNumSkiers(Integer numSkiers) {
        this.numSkiers = numSkiers;
    }
}
