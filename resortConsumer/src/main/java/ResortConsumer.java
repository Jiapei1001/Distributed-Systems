import com.rabbitmq.client.Channel;
import model.LiftRideDaoResort;

public class ResortConsumer implements Runnable {
    private static final String SKIER_QUEUE_NAME = "skier_message_queue";
    private final Channel channel;
    private final LiftRideDaoResort daoResort;

    public ResortConsumer(Channel channel, LiftRideDaoResort daoResort) {
        this.channel = channel;
        this.daoResort = daoResort;
    }

    @Override
    public void run() {

    }
}
