import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class SkiThread extends Thread {

    private static final int RETRIES = 5;

    private Integer startSkierID;
    private Integer endSkierID;
    private Integer startTime;
    private Integer endTime;
    private Integer numLifts;
    private Integer totalRequest;
    private Integer waitTime;

    private String basePath;

    private Integer resortID;
    private String seasonID;
    private String dayID;

    private CountDownLatch currLatch;
    private Stats stats;

    private Random rand;

    public SkiThread(Integer startSkierID, Integer endSkierID, Integer startTime,
            Integer endTime, Integer numLifts, Integer totalRequest,
            Integer waitTime, String basePath, Integer resortID, String seasonID,
            String dayID, CountDownLatch currLatch, Stats stats) {
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
        this.currLatch = currLatch;
        this.stats = stats;

        this.rand = new Random();
    }

    @Override
    public void run() {
        // POST
        SkiersApi apiInstance = new SkiersApi();
        ApiClient client = apiInstance.getApiClient();

        client.setBasePath(basePath);

        for (int i = 0; i < totalRequest; i++) {
            // [s, e - s + 1]
            int rdSkierID = startSkierID + rand.nextInt(endSkierID - startSkierID + 1);
            // [0, numLifts) -> [1, numLifts]
            int rdLiftID = 1 + rand.nextInt(this.numLifts);
            int rdTime = startTime + rand.nextInt(endTime - startTime + 1);
            int rdWaitTime = rand.nextInt(waitTime + 1);

            LiftRide ride = new LiftRide();
            ride.setTime(rdTime);
            ride.setLiftID(rdLiftID);
            ride.setWaitTime(rdWaitTime);

            // try 5 times
            //boolean successful = false;
            //try {
            //    apiInstance.writeNewLiftRide(ride, resortID, seasonID, dayID, rdSkierID);
            //    successful = true;
            //    stats.incrementSuccessfulPost(1);
            //    //stats.getSuccessfulPosts().getAndIncrement();
            //} catch (ApiException e) {
            //    int retry = 0;
            //    while (retry < RETRIES) {
            //        try {
            //            apiInstance.writeNewLiftRide(ride, resortID, seasonID, dayID, rdSkierID);
            //            successful = true;
            //            stats.incrementSuccessfulPost(1);
            //            //stats.getSuccessfulPosts().getAndIncrement();
            //            break;
            //        } catch (ApiException e1) {
            //            // pass
            //        }
            //        retry++;
            //    }
            //}
            //if (!successful) {
            //    stats.incrementFailedPost(1);
            //    //stats.getFailedPosts().getAndDecrement();
            //}


            int j = 0;
            boolean success = false;
            for (; j < RETRIES; j++) {
                try {
                    apiInstance.writeNewLiftRide(ride, resortID, seasonID, dayID, rdSkierID);
                    success = true;
                    //stats.getSuccessfulPosts().getAndIncrement();
                    stats.incrementSuccessfulPost(1);
                    break;
                } catch (ApiException e) {
                    stats.incrementFailedPost(1);

                    System.out.println(e.getCode());
                }
            }
            if (!success) {
                //stats.getFailedPosts().getAndIncrement();
                stats.incrementFailedPost(1);
            }

            // count down latch
            try {
                currLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}