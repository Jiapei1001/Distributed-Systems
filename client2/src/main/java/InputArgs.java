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

    public InputArgs(Integer numThread, Integer numSkier, Integer numLift, Integer numAvgRide, String ipAndPort, String context) {
        this.numThread = numThread;
        this.numSkier = numSkier;
        this.numLift = numLift;
        this.numAvgRide = numAvgRide;
        this.ipAndPort = ipAndPort;
        this.context = context;
    }

    public void initDefault() {
        this.numThread = 256;
        this.numSkier = 20000;
        this.numLift = 40;
        this.numAvgRide = 10;
        this.ipAndPort = "http://servers-load-balancer-1347539645.us-east-1.elb.amazonaws.com:8080/";
        this.context = "server_war";
        // this.ipAndPort = "http://localhost:8080/";
        // this.context = "server";
    }

    public Integer getNumThread() {
        return numThread;
    }

    public void setNumThread(Integer numThread) {
        this.numThread = numThread;
    }

    public Integer getNumSkier() {
        return numSkier;
    }

    public void setNumSkier(Integer numSkier) {
        this.numSkier = numSkier;
    }

    public Integer getNumLift() {
        return numLift;
    }

    public void setNumLift(Integer numLift) {
        this.numLift = numLift;
    }

    public Integer getNumAvgRide() {
        return numAvgRide;
    }

    public void setNumAvgRide(Integer numAvgRide) {
        this.numAvgRide = numAvgRide;
    }

    public String getIpAndPort() {
        return ipAndPort;
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

}