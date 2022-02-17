package servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import model.ResponseMsg;
import model.LiftRide;
import model.SkierVertical;


@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

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
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> reqBody = gson.fromJson(request.getReader(), type);

        LiftRide ride = new LiftRide(
                Integer.parseInt(urlPath[1]),
                urlPath[3],
                urlPath[5],
                Integer.parseInt(urlPath[7]),
                Integer.parseInt(reqBody.get("time")),
                Integer.parseInt(reqBody.get("liftID")),
                Integer.parseInt(reqBody.get("waitTime")));
        // TODO: process lift ride

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
                    && (urlPath[4].equals("day"))
                    && ServerUtil.isNumeric((urlPath[5]))
                    && Integer.parseInt(urlPath[5]) >= 1
                    && Integer.parseInt(urlPath[5]) <= 365
                    && (urlPath[6].equals("skier"))
                    && ServerUtil.isNumeric(urlPath[7]);
        }

        return false;
    }
}
