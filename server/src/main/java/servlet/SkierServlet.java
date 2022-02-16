package servlet;

import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import model.Ride;
import model.SkierEntity;


@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getRequestURI();
        if (url == null || url.length() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Invalid inputs"));
            return;
        }

        String[] urlPath = url.split("/");
        if (!isUrlValid(urlPath)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Invalid inputs"));
            return;
        }

        // valid
        if (urlPath[2].equals("vertical")) {
            SkierEntity skier = new SkierEntity(2, -1, "Winter", 102);
            response.getWriter().write(gson.toJson(skier));
        } else {
            // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
            Ride ride = new Ride(Integer.parseInt(urlPath[1]), urlPath[3], urlPath[5], Integer.parseInt(urlPath[7]), 202);
            response.getWriter().write(gson.toJson(ride));
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        return true;
    }
}
