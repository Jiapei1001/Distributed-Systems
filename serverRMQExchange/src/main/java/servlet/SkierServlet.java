package servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.LiftRide;
import model.ResponseMsg;
import model.SkierVertical;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;


@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

    private static final String SKIER_QUEUE_NAME = "skier_message_queue";
    private static final String RESORT_QUEUE_NAME = "resort_message_queue";

    private static final String EXCHANGE_NAME = "lift_ride";

    private ObjectPool<Channel> channelPool;

    public void init() {
        this.channelPool = new GenericObjectPool<>(new ChannelPool());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getPathInfo();
        if (url == null || url.length() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new ResponseMsg("Invalid inputs")));
            return;
        }

        String[] urlPath = url.split("/");
        if (!isUrlValid(urlPath)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new ResponseMsg("Invalid inputs")));
            return;
        }

        // valid
        if (urlPath[2].equals("vertical")) {
            // TODO: business logic to calculate total vertical for the skier for specified seasons at the specified resort
            SkierVertical skier1 = new SkierVertical(1, "2022-Winter", "23", 102, 202);
            SkierVertical skier2 = new SkierVertical(1, "2021-Winter", "86", 102, 306);

            Map<String, List<SkierVertical>> verticals = new HashMap<>();
            verticals.computeIfAbsent("resorts", a -> new ArrayList<SkierVertical>()).add(skier1);
            verticals.computeIfAbsent("resorts", a -> new ArrayList<SkierVertical>()).add(skier2);

            response.getWriter().write(gson.toJson(verticals));
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
            // TODO: get the total vertical for the skier for the specified ski day
            response.getWriter().write("34507");
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getPathInfo();
        if (url == null || url.length() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new ResponseMsg("Invalid inputs")));
            return;
        }

        String[] urlPath = url.split("/");
        if (!isUrlValid(urlPath) || !urlPath[2].equals("seasons")) {
            response.getWriter().write(gson.toJson(new ResponseMsg("Invalid inputs")));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // valid ride
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        // convert request body to a map
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> reqBody = gson.fromJson(request.getReader(), type);

        LiftRide ride = new LiftRide(
                Integer.parseInt(urlPath[1]),
                urlPath[3],
                urlPath[5],
                Integer.parseInt(urlPath[7]),
                Integer.parseInt(reqBody.get("time")),
                Integer.parseInt(reqBody.get("liftID")),
                Integer.parseInt(reqBody.get("waitTime")));

        // Gson loads request's payload into a separate class
        /*
        LiftRidePayload payload = gson.fromJson(request.getReader(), LiftRidePayload.class);
        LiftRide ride = new LiftRide(
                Integer.parseInt(urlPath[1]),
                urlPath[3],
                urlPath[5],
                Integer.parseInt(urlPath[7]),
                payload.getTime(),
                payload.getLiftID(),
                payload.getWaitTime());
         */

        // if lift ride is valid, format the incoming data and send it as a message to rabbit message queue
        Channel channel = null;
        try {
            String json = gson.toJson(ride);

            // https://commons.apache.org/proper/commons-pool/guide/index.html
            // The default behavior is for the pool to act as a LIFO queue.
            // When there are idle objects available in the pool, borrowObject returns the most recently returned ("last in") instance.
            channel = this.channelPool.borrowObject();
            // https://www.rabbitmq.com/tutorials/tutorial-two-java.html
            channel.queueDeclare(RESORT_QUEUE_NAME, true, false, false, null);
            channel.basicPublish("",
                    RESORT_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, json.getBytes(
                            StandardCharsets.UTF_8));

            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            // Channels and error handling - https://www.rabbitmq.com/channels.html
            // TODO: confirm if throw exception is needed, or print stack trace is ok. If so, would it block sending messages?
            try {
                throw new Exception(e.getCause().getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (channel != null) {
                    this.channelPool.returnObject(channel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // From RabbitMQ tutorials
        /*
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        // factory.setPort(5672);
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            channel.queueDeclare(SKIER_QUEUE_NAME, true, false, false, null);
            String message = "1234";

            channel.basicPublish("", SKIER_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(
                    StandardCharsets.UTF_8));
            System.out.println("Sent: " + message);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        */

        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    // validate the request url path according to the API spec
    private boolean isUrlValid(String[] urlPath) {
        // urlPath  = "/1/vertical"
        if (urlPath.length == 3) {
            return ServerUtil.isNumeric(urlPath[1]) && urlPath[2].equals("vertical");
        }
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlPath.length == 8) {
            return ServerUtil.isNumeric(urlPath[1])
                    && (urlPath[2].equals("seasons"))
                    && ServerUtil.isNumeric(urlPath[3])
                    && (urlPath[4].equals("days"))
                    && ServerUtil.isNumeric((urlPath[5]))
                    && Integer.parseInt(urlPath[5]) >= 1
                    && Integer.parseInt(urlPath[5]) <= 365
                    && (urlPath[6].equals("skiers"))
                    && ServerUtil.isNumeric(urlPath[7]);
        }

        return false;
    }
}