package servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import model.Message;
import model.Resort;
import model.ResortReport;
import model.Season;

@WebServlet(name = "ResortServlet", value = "/ResortServlet")
public class ResortServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getRequestURI();
        // url = /resorts
        if (url == null || url.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            List<Resort> resorts = new ArrayList<>();
            resorts.add(new Resort(1, "Seattle Resort"));
            resorts.add(new Resort(2, "Olympic Resort"));
            response.getWriter().write(gson.toJson(resorts));
            return;
        }

        String[] urlPath = url.split("/");
        if (!isUrlValid(urlPath)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new Message("Invalid inputs")));
            return;
        }

        // valid
        // urlParts = [, 1, seasons]
        if (urlPath.length == 3) {
            // TODO: process to get the seasons by the input resortID
            Set<Season> seasons = new HashSet<>();
            seasons.add(new Season("2020"));
            seasons.add(new Season("2021"));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(seasons));
        }
        // urlParts = [, 1, seasons, 2019, day, 1, skiers]
        else if (urlPath.length == 7) {
            // TODO: process to get the number of skiers at resort/season/day
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(new ResortReport("Mission Ridge", 78999)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getRequestURI();
        if (url == null || url.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new Message("Invalid inputs")));
            return;
        }

        String[] urlPath = url.split("/");
        if (!isUrlValid(urlPath)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new Message("Invalid inputs")));
            return;
        }

        // valid
        // urlParts = [, 1, seasons]
        // get request body
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> reqBody = gson.fromJson(request.getReader(), type);

        Season season = new Season(reqBody.get("year"));
        // TODO: get the specific resort by resortID, if not found, return 404
        Resort resort = new Resort(1, "Seattle resort");
        resort.addSeason(season);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write(gson.toJson(resort));
    }

    // validate the request url path according to the API spec
    private boolean isUrlValid(String[] urlPath) {
        // urlPath  = "/1/seasons"
        if (urlPath.length == 3) {
            return ServerUtil.isNumeric(urlPath[1]) && urlPath[2].equals("seasons");
        }
        // urlPath  = "/1/seasons/2019/day/1/skiers"
        // urlParts = [, 1, seasons, 2019, day, 1, skiers]
        if (urlPath.length == 7) {
            return ServerUtil.isNumeric(urlPath[1])
                    && (urlPath[2].equals("seasons"))
                    && ServerUtil.isNumeric(urlPath[3])
                    && (urlPath[4].equals("day"))
                    && ServerUtil.isNumeric((urlPath[5]))
                    && Integer.parseInt(urlPath[5]) >= 1
                    && Integer.parseInt(urlPath[5]) <= 365
                    && (urlPath[6].equals("skiers"));
        }

        return false;
    }
}
