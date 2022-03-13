package model;

import java.util.List;

public class ResortsList {
    List<Resort> resorts;

    public ResortsList(List<Resort> resorts) {
        this.resorts = resorts;
    }

    public List<Resort> getResorts() {
        return resorts;
    }

    public void setResorts(List<Resort> resorts) {
        this.resorts = resorts;
    }
}
