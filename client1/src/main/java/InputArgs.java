public class InputArgs {

    public Integer numThread;
    public Integer numSkier;
    public Integer numLift;
    public Integer numAvgRide;
    public String ipAndPort;
    public String context;

    public InputArgs() {
        this.initDefault();
    }

    public InputArgs(Integer numThread, Integer numSkier, Integer numLift, Integer numAvgRide, String ipAndPort) {
        this.numThread = numThread;
        this.numSkier = numSkier;
        this.numLift = numLift;
        this.numAvgRide = numAvgRide;
        this.ipAndPort = ipAndPort;
    }

    public void initDefault() {
        this.numThread = 64;
        this.numSkier = 1024;
        this.numLift = 40;
        this.numAvgRide = 20;
        this.ipAndPort = "http://localhost:8080/";
        this.context = "server";
    }
}
