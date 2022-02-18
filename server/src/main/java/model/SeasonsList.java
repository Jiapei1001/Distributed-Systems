package model;

import java.util.ArrayList;
import java.util.List;

public class SeasonsList {

    List<String> seasons;

    public SeasonsList() {
        this.seasons = new ArrayList<>();
    }

    public SeasonsList(List<String> seasons) {
        this.seasons = seasons;
    }

    public List<String> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<String> seasons) {
        this.seasons = seasons;
    }
}
