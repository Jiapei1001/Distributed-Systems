public class Stats {

    private int successfulPosts;
    private int failedPosts;

    public Stats() {
        this.successfulPosts = 0;
        this.failedPosts = 0;
    }

    public synchronized void incrementSuccessfulPost(int i) {
        this.successfulPosts += i;
    }

    public synchronized void incrementFailedPost(int i) {
        this.failedPosts += i;
    }

    public int getSuccessfulPosts() {
        return successfulPosts;
    }

    public int getFailedPosts() {
        return failedPosts;
    }
}

//import java.util.concurrent.atomic.AtomicInteger;
//
//public class Stats {
//
//    private AtomicInteger successfulPosts;
//    private AtomicInteger failedPosts;
//
//    public Stats() {
//        this.successfulPosts = new AtomicInteger(0);
//        this.failedPosts = new AtomicInteger(0);
//    }
//
//
//    public AtomicInteger getSuccessfulPosts() {
//        return successfulPosts;
//    }
//
//    public AtomicInteger getFailedPosts() {
//        return failedPosts;
//    }
//}
//
