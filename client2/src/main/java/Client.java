import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Client {

    private static final float LATCH_THRESHOLD = 0.2f;

    private static final int P1_START_TIME = 1;
    private static final int P1_END_TIME = 90;

    private static final int P2_START_TIME = 91;
    private static final int P2_END_TIME = 360;

    private static final int P3_START_TIME = 361;
    private static final int P3_END_TIME = 420;

    private static final int DEFAULT_WAIT_TIME = 10;

    private static final float SINGLE_THREAD_THROUGHPUT = 15.65f; // 15.65, single thread throughput

    public static void main(String[] args) throws Exception {
        // initiate default args
        // InputArgs input = new InputArgs();
        CmdLineParser cmdParser = new CmdLineParser();
        InputArgs input = cmdParser.parseInputArgs(args);

        // assign
        int numThread = input.numThread;
        int numSkier = input.numSkier;
        int numLift = input.numLift;
        int numAvgRide = input.numAvgRide;
        String basePath = input.ipAndPort + (input.ipAndPort.charAt(input.ipAndPort.length() - 1) == '/' ? "" : "/") + input.context;

        Stats stats = new Stats();

        int resortID = 2;
        String seasonID = "2022";
        String dayID = "166";
        List<LatencyRecord> latencies = new ArrayList<>();

        long start = System.currentTimeMillis();

        // phase 1: start up
        String phase1 = "Phase1";
        ThreadDetail td1 = new ThreadDetail();
        ThreadDetail p1 = td1.getThreadDetail(phase1, numThread, numAvgRide, numSkier);
        CountDownLatch thdLatchP1 = new CountDownLatch((int) (p1.numThreadP * LATCH_THRESHOLD));
        CountDownLatch reqLatchP1 = new CountDownLatch(p1.totalReq);


        int seg = p1.numSkierPerThread;
        for (int i = 0; i < p1.numThreadP; i++) {
            int startSkierID = i * seg + 1;
            int endSkierID = i == (p1.numThreadP - 1) ? (i * seg + (numSkier - i * seg)) : (i + 1) * seg;
            SkiThread thread = new SkiThread(startSkierID, endSkierID, P1_START_TIME, P1_END_TIME, numLift, p1.reqPerThread, DEFAULT_WAIT_TIME, basePath, resortID, seasonID, dayID, thdLatchP1, reqLatchP1, stats, latencies);
            thread.start();
        }
        printDetails(phase1, p1);

        thdLatchP1.await();

        // phase 2: peak
        String phase2 = "Phase2";
        ThreadDetail td2 = new ThreadDetail();
        ThreadDetail p2 = td2.getThreadDetail(phase2, numThread, numAvgRide, numSkier);
        CountDownLatch thdLatchP2 = new CountDownLatch((int) (p2.numThreadP * LATCH_THRESHOLD));
        CountDownLatch reqLatchP2 = new CountDownLatch(p2.totalReq);


        seg = p2.numSkierPerThread;
        for (int i = 0; i < p2.numThreadP; i++) {
            int startSkierID = i * seg + 1;
            int endSkierID = (i == p2.numThreadP - 1) ? (i * seg + (numSkier - i * seg)) : (i + 1) * seg;
            SkiThread thread = new SkiThread(startSkierID, endSkierID, P2_START_TIME, P2_END_TIME, numLift, p2.reqPerThread, DEFAULT_WAIT_TIME, basePath, resortID, seasonID, dayID, thdLatchP2, reqLatchP2, stats, latencies);
            thread.start();
        }
        printDetails(phase2, p2);

        thdLatchP2.await();

        // phase 3: cool down
        String phase3 = "Phase3";
        ThreadDetail td3 = new ThreadDetail();
        ThreadDetail p3 = td3.getThreadDetail(phase3, numThread, numAvgRide, numSkier);
        CountDownLatch thdLatchP3 = new CountDownLatch((int) (p3.numThreadP * LATCH_THRESHOLD)); // don't call await for latchP3, just pass into constructor
        CountDownLatch reqLatchP3 = new CountDownLatch(p3.totalReq);


        seg = p3.numSkierPerThread;
        for (int i = 0; i < p3.numThreadP; i++) {
            int startSkierID = i * seg + 1;
            int endSkierID = (i == p3.numThreadP - 1) ? (i * seg + (numSkier - i * seg)) : (i + 1) * seg;
            SkiThread thread = new SkiThread(startSkierID, endSkierID, P3_START_TIME, P3_END_TIME, numLift, p3.reqPerThread, DEFAULT_WAIT_TIME, basePath, resortID, seasonID, dayID, thdLatchP3, reqLatchP3, stats, latencies);
            thread.start();
        }
        printDetails(phase3, p3);

        // sleep(4000);
        // stats increment or decrement is locked and takes more time, this makes sure they are all updated
        reqLatchP1.await();
        reqLatchP2.await();
        reqLatchP3.await();

        long end = System.currentTimeMillis();
        long wall = end - start;
        float throughput = (float) (p1.totalReq + p2.totalReq + p3.totalReq) * 1000 / wall;

        float predictedMaxThroughput =
                (float) (p1.totalReq + p2.totalReq + p3.totalReq) / (p1.reqPerThread
                        + p2.reqPerThread + p3.reqPerThread) * SINGLE_THREAD_THROUGHPUT;
        float predictedMinThroughput = (float) (SINGLE_THREAD_THROUGHPUT * numThread * ThreadDetail.P3_AVG_RIDES_FACTOR);
        float predictedThroughput = ((float) (predictedMaxThroughput + predictedMinThroughput) / 2);

        System.out.printf("\nReport #1 (threads: %d):\n", numThread);
        System.out.printf("# of successful:\t\t\t %d\n", stats.getSuccessfulPosts());
        System.out.printf("# of fail:\t\t\t\t\t\t %d\n",stats.getFailedPosts());
        System.out.printf("wall time:\t\t\t\t\t\t %.2f second\n", (float)wall /1000);
        System.out.printf("throughput per second:\t %.2f request/second\n", throughput);

        System.out.println("\nThroughput prediction:");
        System.out.printf("Max throughput: \t\t %.2f\n", predictedMaxThroughput);
        System.out.printf("Min throughput: \t\t %.2f\n", predictedMinThroughput);
        System.out.printf("Predict throughput: \t %.2f\n", predictedThroughput);

        System.out.printf("\nReport #2 (threads: %d):\n", numThread);
        LatencyProcessor p = new LatencyProcessor(latencies, throughput, numThread);
        p.writeToCSV();
        p.processAndPrintResults();
    }

    private static void printDetails(String phase, ThreadDetail p) {
        System.out.println(phase + " # of threads: " + p.numThreadP);
        System.out.println(phase + " # of skiers: " + p.numSkierPerThread);
        System.out.println(phase + " # of requests per thread: " + p.reqPerThread);
        System.out.println(phase + " # of total requests: " + p.totalReq + "\n");
    }
}
