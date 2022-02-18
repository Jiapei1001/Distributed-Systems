import static java.lang.Thread.sleep;

import java.util.concurrent.CountDownLatch;

public class Client {

    private static final float LATCH_THRESHOLD = 0.2f;

    private static final Integer P1_START_TIME = 1;
    private static final Integer P1_END_TIME = 90;

    private static final Integer P2_START_TIME = 91;
    private static final Integer P2_END_TIME = 360;

    private static final Integer P3_START_TIME = 361;
    private static final Integer P3_END_TIME = 420;

    private static final Integer DEFAULT_WAIT_TIME = 10;

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

        long start = System.currentTimeMillis();

        // phase 1: start up
        ThreadDetail td = new ThreadDetail();
        String phase1 = "Phase1";
        ThreadDetail p1 = td.getThreadDetail(phase1, numThread, numAvgRide, numSkier);
        CountDownLatch latchP1 = new CountDownLatch((int) (p1.numThreadP * LATCH_THRESHOLD));

        int seg = p1.numSkierPerThread;
        for (int i = 0; i < p1.numThreadP; i++) {
            int startSkierID = i * seg + 1;
            int endSkierID = i == (p1.numThreadP - 1) ? (i * seg + (numSkier - i * seg)) : (i + 1) * seg;
            SkiThread thread = new SkiThread(startSkierID, endSkierID, P1_START_TIME, P1_END_TIME, numLift, p1.reqPerThread, DEFAULT_WAIT_TIME, basePath, resortID, seasonID, dayID, latchP1, stats);
            thread.start();
        }
        printDetails(phase1, p1);

        latchP1.await();

        // phase 2: peak
        String phase2 = "Phase2";
        ThreadDetail p2 = td.getThreadDetail(phase2, numThread, numAvgRide, numSkier);
        CountDownLatch latchP2 = new CountDownLatch((int) (p2.numThreadP * LATCH_THRESHOLD));

        seg = p2.numSkierPerThread;
        for (int i = 0; i < p2.numThreadP; i++) {
            int startSkierID = i * seg + 1;
            int endSkierID = (i == p2.numThreadP - 1) ? (i * seg + (numSkier - i * seg)) : (i + 1) * seg;
            SkiThread thread = new SkiThread(startSkierID, endSkierID, P2_START_TIME, P2_END_TIME, numLift, p2.reqPerThread, DEFAULT_WAIT_TIME, basePath, resortID, seasonID, dayID, latchP2, stats);
            thread.start();
        }
        printDetails(phase2, p2);

        latchP2.await();

        // phase 3: cool down
        String phase3 = "Phase3";
        ThreadDetail p3 = td.getThreadDetail(phase3, numThread, numAvgRide, numSkier);
        CountDownLatch latchP3 = new CountDownLatch((int) (p2.numThreadP * LATCH_THRESHOLD)); // don't call await for latchP3, just pass into constructor

        seg = p3.numSkierPerThread;
        for (int i = 0; i < p3.numThreadP; i++) {
            int startSkierID = i * seg + 1;
            int endSkierID = (i == p3.numThreadP - 1) ? (i * seg + (numSkier - i * seg)) : (i + 1) * seg;
            SkiThread thread = new SkiThread(startSkierID, endSkierID, P3_START_TIME, P3_END_TIME, numLift, p3.reqPerThread, DEFAULT_WAIT_TIME, basePath, resortID, seasonID, dayID, latchP3, stats);
            thread.start();
        }
        printDetails(phase3, p3);

        long end = System.currentTimeMillis();
        long wall = end - start;
        float throughput = (float) (p1.totalReq + p2.totalReq + p3.totalReq) * 1000 / wall;

        sleep(4000);
        System.out.println("# of successful:\t\t" + stats.getSuccessfulPosts());
        System.out.println("# of fail:\t\t" + stats.getFailedPosts());
        System.out.println("wall time:\t\t" + (float)wall /1000);
        System.out.println("throughput per second:\t\t" + throughput);
    }

    private static void printDetails(String phase, ThreadDetail p) {
        System.out.println(phase + " # of threads: " + p.numThreadP);
        System.out.println(phase + " # of skiers: " + p.numSkierPerThread);
        System.out.println(phase + " # of requests per thread: " + p.reqPerThread);
        System.out.println(phase + " # of total requests: " + p.totalReq + "\n\n");
    }
}
