package servlet;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelPool extends BasePooledObjectFactory<Channel> {

    private static final String HOST = "18.233.158.204";
    // private static final String HOST = "localhost";
    private static final int PORT = 5672;
    private final ConnectionFactory connFactory = new ConnectionFactory();

    @Override
    public Channel create() throws Exception {
        // Java Client - https://www.rabbitmq.com/api-guide.html
        this.connFactory.setHost(HOST);
        this.connFactory.setPort(PORT);
        // don't set username and password
        this.connFactory.setUsername("admin123456");
        this.connFactory.setPassword("123456");

        Connection conn = this.connFactory.newConnection();
        return conn.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
        p.getObject().close();
    }
}
