public class ThreadDetail {

    private static final int P1_THREAD_DIVISOR = 4;
    private static final float P1_AVG_RIDES_FACTOR = 0.2f;

    private static final int P2_THREAD_DIVISOR = 1;
    private static final float P2_AVG_RIDES_FACTOR = 0.6f;

    private static final int P3_THREAD_DIVISOR = 10;
    private static final float P3_AVG_RIDES_FACTOR = 0.1f;

    int numThreadP;
    int numAvgRideP;
    int numSkierPerThread;
    int reqPerThread;

    public ThreadDetail() {
        this.numThreadP = 0;
        this.numAvgRideP = 0;
        this.numSkierPerThread = 0;
        this.reqPerThread = 0;
    }

    public ThreadDetail getThreadDetail(String phase, int numThread, int numAvgRide, int numSkier) {
        switch (phase) {
            case "Phase1":
                numThreadP = (int) (numThread / P1_THREAD_DIVISOR); //16
                numAvgRideP = (int) (numAvgRide * P1_AVG_RIDES_FACTOR); //20 * 0.2 = 4
                numSkierPerThread = (int) Math.ceil((float) numSkier / numThreadP);
                reqPerThread = (numAvgRideP * numSkier) / (numThreadP); // NOTE: here * numSkiers, (numRunsx0.2)x(numSkiers/(numThreads/4))
                break;
            case "Phase2":
                numThreadP = (int) (numThread / P2_THREAD_DIVISOR);
                numAvgRideP = (int) (numAvgRide * P2_AVG_RIDES_FACTOR);
                numSkierPerThread = (int) Math.ceil((float) numSkier / numThreadP);
                reqPerThread = (numAvgRideP * numSkier) / (numThreadP);
                break;
            case "Phase3":
                numThreadP = (int) (numThread / P3_THREAD_DIVISOR);
                numAvgRideP = (int) (numAvgRide * P3_AVG_RIDES_FACTOR);
                numSkierPerThread = (int) Math.ceil((float) numSkier / numThreadP);
                reqPerThread = (numAvgRideP * numSkier) / (numThreadP);
                break;
        }
        return this;
    }
}
