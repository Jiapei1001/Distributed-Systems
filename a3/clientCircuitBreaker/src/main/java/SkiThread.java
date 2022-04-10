import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

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
    private final CountDownLatch threadLatch;
    private final CountDownLatch reqLatch;
    private final Stats stats;
    private final Random rand;
    private String seasonID;
    private String dayID;
    private List<LatencyRecord> latencies;
    private int successfulReqCntLocal = 0;
    private int failedReqCntLocal = 0;
    private EventCountCircuitBreaker breaker;

    public SkiThread(Integer startSkierID, Integer endSkierID, Integer startTime,
            Integer endTime, Integer numLifts, Integer totalRequest,
            Integer waitTime, String basePath, Integer resortID, String seasonID,
            String dayID, CountDownLatch currLatch, CountDownLatch reqLatch, Stats stats,
            List<LatencyRecord> latencies) {
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

        this.rand = new Random();

        // openingThreshold -   the threshold for opening the circuit breaker; if this number of events
        //                      is received in the time span determined by the check interval, the circuit breaker is opened
        // checkInterval -      the check interval for opening or closing the circuit breaker
        // checkUnit -          the TimeUnit defining the check interval
        // closingThreshold -   the threshold for closing the circuit breaker;
        //                      if the number of events received in the time span determined by the check interval goes below this threshold, the circuit breaker is closed again
        this.breaker = new EventCountCircuitBreaker(1000, 1,
                TimeUnit.SECONDS, 600);
    }

    @Override
    public void run() {
        // POST
        SkiersApi apiInstance = new SkiersApi();
        ApiClient client = apiInstance.getApiClient();

        client.setBasePath(basePath);

        for (int i = 0; i < totalRequest; i++) {
            if (breaker.checkState()) {
                // [s, e - s + 1]
                int rdSkierID = startSkierID +
                        ThreadLocalRandom.current().nextInt(endSkierID - startSkierID + 1);
                // [0, numLifts) -> [1, numLifts]
                int rdLiftID = 1 + ThreadLocalRandom.current().nextInt(this.numLifts);
                int rdTime =
                        startTime + ThreadLocalRandom.current().nextInt(endTime - startTime + 1);
                int rdWaitTime = ThreadLocalRandom.current().nextInt(waitTime + 1);

                LiftRide ride = new LiftRide();
                ride.setTime(rdTime);
                ride.setLiftID(rdLiftID);
                ride.setWaitTime(rdWaitTime);

                // randomize they ski seasonID and dayID
                // this.seasonID = String.valueOf(2020 + rand.nextInt(3)); // 2020-2022
                this.seasonID = String.valueOf(2022);
                this.dayID = String.valueOf(1 + rand.nextInt(365));

                int j = 0;
                boolean success = false;
                for (; j < RETRIES; j++) {
                    try {
                        long startTime = System.currentTimeMillis();
                        ApiResponse<Void> res = apiInstance.writeNewLiftRideWithHttpInfo(ride,
                                resortID, seasonID, dayID, rdSkierID);

                        if (res.getStatusCode() == HttpStatus.SC_CREATED) {
                            success = true;
                            // stats.incrementSuccessfulPost(1);
                            successfulReqCntLocal++;

                            long endTime = System.currentTimeMillis();
                            this.latencies.add(
                                    new LatencyRecord(startTime, REQUEST.POST, endTime - startTime,
                                            res.getStatusCode()));
                            break;
                        } else {
                            this.breaker.incrementAndCheckState();
                            System.out.println("FAILURE");
                        }
                    } catch (ApiException e) {
                        // pass
                    }
                }
                if (!success) {
                    failedReqCntLocal++;
                    // stats.incrementFailedPost(1);
                }
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