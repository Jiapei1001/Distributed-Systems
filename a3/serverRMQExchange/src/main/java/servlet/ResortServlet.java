package servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.ResortDao;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Resort;
import model.ResortSkiers;
import model.ResortsList;
import model.ResponseMsg;
import model.SeasonsList;

@WebServlet(name = "ResortServlet", value = "/ResortServlet")
public class ResortServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String url = request.getPathInfo();
        // url = /resorts
        if (url == null || url.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            List<Resort> resorts = new ArrayList<>();
            resorts.add(new Resort(1, "Seattle Resort"));
            resorts.add(new Resort(2, "Olympic Resort"));
            ResortsList resortsList = new ResortsList(resorts);
            response.getWriter().write(gson.toJson(resortsList, ResortsList.class));
            return;
        }

        String[] urlPath = url.split("/");
        if (!isUrlValid(urlPath)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter()
                    .write(gson.toJson(new ResponseMsg("Invalid inputs"), ResponseMsg.class));
            return;
        }

        // valid
        ResortDao resortDao;
        try {
            resortDao = new ResortDao();
        } catch (Exception e) {
            throw new ServletException(e);
        }

        // urlParts = [, 1, seasons]
        if (urlPath.length == 3) {
            // TODO: process to get the seasons by the input resortID
            List<String> seasons = new ArrayList<>();
            seasons.add("2019");
            seasons.add("2020");
            seasons.add("2021");
            seasons.add("2022");
            SeasonsList seasonsList = new SeasonsList(seasons);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(seasonsList, SeasonsList.class));
        }
        // urlParts = [, 1, seasons, 2019, day, 1, skiers]
        else if (urlPath.length == 7) {
            String resortID = urlPath[1];
            String seasonID = urlPath[3];
            String dayID = urlPath[5];

            // get number of unique skiers at resort/season/day
            int uniqueCnt = resortDao.getUniqueSkiersPerDay(resortID, seasonID, dayID);

            if (uniqueCnt == 0) {
                response.getWriter().write(gson.toJson(new ResponseMsg("Data not found.")));
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                response.getWriter().write(gson.toJson(new ResortSkiers(resortID, uniqueCnt), ResortSkiers.class));
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
        if (url == null || url.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter()
                    .write(gson.toJson(new ResponseMsg("Invalid inputs"), ResponseMsg.class));
            return;
        }

        String[] urlPath = url.split("/");
        if (!isUrlValid(urlPath)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter()
                    .write(gson.toJson(new ResponseMsg("Invalid inputs"), ResponseMsg.class));
            return;
        }

        // valid
        // urlParts = [, 1, seasons]
        // get request body
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> reqBody = gson.fromJson(request.getReader(), type);

        String season = reqBody.get("year");
        // TODO: get the specific resort by resortID, if not found, return 404
        Resort resort = new Resort(1, "Seattle resort");
        resort.addSeason(season);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write(gson.toJson(resort, Resort.class));
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
