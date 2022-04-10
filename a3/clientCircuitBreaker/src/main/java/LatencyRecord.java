enum REQUEST {
    GET, POST;
}


public class LatencyRecord {
    long startTime;
    REQUEST type;
    long latency;
    int resCode;

    public LatencyRecord(long startTime, REQUEST type, long latency, int resCode) {
        this.startTime = startTime;
        this.type = type;
        this.latency = latency;
        this.resCode = resCode;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public REQUEST getType() {
        return type;
    }

    public void setType(REQUEST type) {
        this.type = type;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public int getResCode() {
        return resCode;
    }

    public void setResCode(int resCode) {
        this.resCode = resCode;
    }
}