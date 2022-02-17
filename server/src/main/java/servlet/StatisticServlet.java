package servlet;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import model.APIStats;
import model.ResponseMsg;

@WebServlet(name = "StatisticServlet", value = "/StatisticServlet")
public class StatisticServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getPathInfo();
        // valid url should be empty
        if (url == null || url.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            List<APIStats> apiStats = new ArrayList<>();
            apiStats.add(new APIStats("/resorts", "GET", 11, 198));
            response.getWriter().write(gson.toJson(apiStats));
            return;
        }

        // not valid url
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(new ResponseMsg("Invalid inputs")));
    }
}
