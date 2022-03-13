import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class SkiThread extends Thread {

    private static final int RETRIES = 5;

    private final Integer startSkierID;
    private final Integer endSkierID;
    private final Integer startTime;
    private final Integer endTime;
    private final Integer numLifts;
    private final Integer totalRequest;
    private final Integer waitTime;

    private final String basePath;

    private final Integer resortID;
    private final String seasonID;
    private final String dayID;

    private final CountDownLatch threadLatch;
    private final CountDownLatch reqLatch;
    private final Stats stats;
    private List<LatencyRecord> latencies;

    private int successfulReqCntLocal = 0;
    private int failedReqCntLocal = 0;

    public SkiThread(Integer startSkierID, Integer endSkierID, Integer startTime,
            Integer endTime, Integer numLifts, Integer totalRequest,
            Integer waitTime, String basePath, Integer resortID, String seasonID,
            String dayID, CountDownLatch currLatch, CountDownLatch reqLatch, Stats stats, List<LatencyRecord> latencies) {
        this.startSkierID = startSkierID;
        this.endSkierID = endSkierID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numLifts = numLifts;
        this.totalRequest = totalRequest;
        this.waitTime = waitTime;
        this.basePath = basePath;
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.threadLatch = currLatch;
        this.reqLatch = reqLatch;
        this.stats = stats;
        this.latencies = latencies;
    }

    @Override
    public void run() {
        // POST
        SkiersApi apiInstance = new SkiersApi();
        ApiClient client = apiInstance.getApiClient();

        client.setBasePath(basePath);

        for (int i = 0; i < totalRequest; i++) {
            // [s, e - s + 1]
            int rdSkierID = startSkierID +
                    ThreadLocalRandom.current().nextInt(endSkierID - startSkierID + 1);
            // [0, numLifts) -> [1, numLifts]
            int rdLiftID = 1 + ThreadLocalRandom.current().nextInt(this.numLifts);
            int rdTime = startTime + ThreadLocalRandom.current().nextInt(endTime - startTime + 1);
            int rdWaitTime = ThreadLocalRandom.current().nextInt(waitTime + 1);

            LiftRide ride = new LiftRide();
            ride.setTime(rdTime);
            ride.setLiftID(rdLiftID);
            ride.setWaitTime(rdWaitTime);

            int j = 0;
            boolean success = false;
            for (; j < RETRIES; j++) {
                try {
                    long startTime = System.currentTimeMillis();
                    ApiResponse<Void> res = apiInstance.writeNewLiftRideWithHttpInfo(ride, resortID, seasonID, dayID, rdSkierID);
                    success = true;
                    // stats.incrementSuccessfulPost(1);
                    successfulReqCntLocal++;
                    long endTime = System.currentTimeMillis();
                    this.latencies.add(new LatencyRecord(startTime, REQUEST.POST, endTime - startTime, res.getStatusCode()));
                    break;
                } catch (ApiException e) {
                    // pass
                }
            }
            if (!success) {
                failedReqCntLocal++;
                // stats.incrementFailedPost(1);
            }
        }

        // count down thread latch after processing all requests
        try {
            stats.incrementSuccessfulPost(successfulReqCntLocal);
            stats.incrementFailedPost(failedReqCntLocal);
            threadLatch.countDown();
            reqLatch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}