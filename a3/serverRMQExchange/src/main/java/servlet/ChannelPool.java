package servlet;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

// ChannelPool based on Apache Commons Pool
// https://commons.apache.org/proper/commons-pool/examples.html
public class ChannelPool extends BasePooledObjectFactory<Channel> {

    // private static final String HOST = "127.0.0.1";
    // private static final String HOST = "http://localhost";
    private static final String HOST = "44.201.192.31";
    private static final int PORT = 5672;
    private final ConnectionFactory connFactory = new ConnectionFactory();

    @Override
    public Channel create() throws Exception {
        // Java Client - https://www.rabbitmq.com/api-guide.html
        // this.connFactory.setHost("localhost");

        this.connFactory.setHost(HOST);
        this.connFactory.setPort(PORT);
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