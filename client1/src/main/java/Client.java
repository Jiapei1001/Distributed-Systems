import java.util.concurrent.CountDownLatch;

public class Client {


    private static final float P1_LATCH_THRESHOLD = 0.2f;
    private static final Integer DEFAULT_WAIT_TIME = 10;
    private static final Integer P1_START_TIME = 1;
    private static final Integer P1_END_TIME = 90;


    public static void main(String[] args) throws Exception {
        // initiate default args
        InputArgs input = new InputArgs();

        // assign
        Integer numThread = input.numThread;
        Integer numSkier = input.numSkier;
        Integer numLift = input.numLift;
        Integer numAvgRide = input.numAvgRide;
        String basePath = input.ipAndPort + (input.ipAndPort.charAt(input.ipAndPort.length() - 1) == '/' ? "" : "/") + input.context;

        Stats stats = new Stats();

        int resortID = 2;
        String seasonID = "2022";
        String dayID = "166";

        ThreadDetail td = new ThreadDetail();
        ThreadDetail p1 = td.getThreadDetail("Phase1", numThread, numAvgRide, numSkier);
        CountDownLatch latchP1 = new CountDownLatch((int) (p1.numThreadP * P1_LATCH_THRESHOLD));

        for (int i = 0; i < p1.numThreadP; i++) {
            int startSkierID = i * p1.numSkierPerThread + 1;
            int endSkierID = i == (p1.numThreadP - 1) ? (p1.numThreadP - (i + 1) * p1.numSkierPerThread) : (i + 1) * p1.numSkierPerThread;
            SkiThread thread = new SkiThread(startSkierID, endSkierID, P1_START_TIME, P1_END_TIME, numLift, p1.reqPerThread, DEFAULT_WAIT_TIME, basePath, resortID, seasonID, dayID, latchP1, stats);
            thread.start();
        }

        System.out.println("number of threads: " + p1.numThreadP);
        System.out.println("number of skiers: " + p1.numSkierPerThread);
        System.out.println("number of requests: " + p1.reqPerThread);
    }

}
