package servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import dao.SkierDao;
import dao.SkierJedisPool;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.LiftRide;
import model.ResponseMsg;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

    private static final String SKIER_QUEUE_NAME = "skier_message_queue";
    private static final String RESORT_QUEUE_NAME = "resort_message_queue";

    private static final String EXCHANGE_NAME = "lift_ride";

    private ObjectPool<Channel> channelPool;
    private SkierJedisPool skierJedisPool;

    public void init() throws ServletException {
        this.channelPool = new GenericObjectPool<>(new ChannelPool());
        try {
            this.skierJedisPool = new SkierJedisPool();
        } catch (Exception e) {
            throw new ServletException(e);
        }
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
        if (!isUrlValid(request, urlPath)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new ResponseMsg("Invalid inputs")));
            return;
        }

        // valid
        SkierDao skierDao;
        try {
            skierDao = new SkierDao(this.skierJedisPool.getJedisPool());
        } catch (Exception e) {
            throw new ServletException(e);
        }

        if (urlPath[2].equals("vertical")) {
            // get the total vertical for the skier for specified seasons at the specified resort
            String skierID = urlPath[1];
            String resortID = request.getParameter("resort");
            // seasonID is optional parameter. Returns the value of a request parameter as a String, or null if the parameter does not exist.
            String seasonID = request.getParameter("season");

            if (seasonID == null) {
                Map<String, Integer> seasonVerticals = skierDao.getVerticalTotalsPerResort(skierID,
                        resortID);
                if (seasonVerticals == null) {
                    response.getWriter().write(gson.toJson(new ResponseMsg("Data not found.")));
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.getWriter().write(gson.toJson(seasonVerticals));
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } else {
                Map<String, Integer> seasonVertical = skierDao.getVerticalTotalPerResortAndSeason(
                        skierID, resortID, seasonID);
                if (seasonVertical == null) {
                    response.getWriter().write(gson.toJson(new ResponseMsg("Data not found.")));
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.getWriter().write(gson.toJson(seasonVertical));
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            }
        } else {
            // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
            String resortID = urlPath[1];
            String seasonID = urlPath[3];
            String dayID = urlPath[5];
            String skierID = urlPath[7];

            Integer verticalTotal = skierDao.getVerticalTotalPerDay(skierID, resortID, seasonID,
                    dayID);

            if (verticalTotal == null) {
                response.getWriter().write(gson.toJson(new ResponseMsg("Data not found.")));
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                response.getWriter().write(String.valueOf(verticalTotal));
                response.setStatus(HttpServletResponse.SC_OK);
            }
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
        if (!isUrlValid(request, urlPath) || !urlPath[2].equals("seasons")) {
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

        LiftRide ride = new LiftRide(urlPath[1], urlPath[3], urlPath[5],
                Integer.parseInt(urlPath[7]), Integer.parseInt(reqBody.get("time")),
                Integer.parseInt(reqBody.get("liftID")), Integer.parseInt(reqBody.get("waitTime")));

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
        // EXCHANGE FANOUT & QUEUE BINDING
        // Instead of sending message directly to a specific queue, here it uses FANOUT method to send the message to an exchange.
        try {
            String json = gson.toJson(ride);

            // Exchange: https://www.rabbitmq.com/tutorials/tutorial-three-java.html
            channel = this.channelPool.borrowObject();

            // Queue declare
            // channel.queueDeclare(RESORT_QUEUE_NAME, true, false, false, null);
            // channel.basicPublish("",
            //         RESORT_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, json.getBytes(
            //                 StandardCharsets.UTF_8));

            // Exchange Fanout
            // channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN,
                    json.getBytes(StandardCharsets.UTF_8));

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
    private boolean isUrlValid(HttpServletRequest request, String[] urlPath) {
        // urlPath  = "/1/vertical"
        if (urlPath.length == 3) {
            // skierID & resort are required, season is optional
            return ServerUtil.isNumeric(urlPath[1]) && urlPath[2].equals("vertical")
                    && request.getParameter("resort") != null;
        }
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlPath.length == 8) {
            return ServerUtil.isNumeric(urlPath[1]) && (urlPath[2].equals("seasons"))
                    && ServerUtil.isNumeric(urlPath[3]) && (urlPath[4].equals("days"))
                    && ServerUtil.isNumeric((urlPath[5])) && Integer.parseInt(urlPath[5]) >= 1
                    && Integer.parseInt(urlPath[5]) <= 365 && (urlPath[6].equals("skiers"))
                    && ServerUtil.isNumeric(urlPath[7]);
        }

        return false;
    }
}