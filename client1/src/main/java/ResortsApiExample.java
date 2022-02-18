import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;
import java.util.ArrayList;

public class ResortsApiExample {

    public static void main(String[] args) {

        ResortsApi apiInstance = new ResortsApi();
        //SkiersApi apiInstance = new SkiersApi();
        ApiClient client = apiInstance.getApiClient();
        client.setBasePath("http://localhost:8080/server");

        //ResortIDSeasonsBody body = new ResortIDSeasonsBody(); // ResortIDSeasonsBody | Specify new Season value

        Integer resortID = 56; // Integer | ID of the resort of interest
        try {
            //apiInstance.addSeason(body, resortID);

            //ResortSkiers resortSkiers = apiInstance.getResortSkiersDay(1,1, 255);
            //System.out.println(resortSkiers);

            SeasonsList seasonsList = apiInstance.getResortSeasons(1);
            System.out.println(seasonsList);

            //ResortsList resortsList = apiInstance.getResorts();
            //System.out.println(resortsList);
        } catch (ApiException e) {
            System.err.println("Exception when calling ResortsApi#addSeason");
            e.printStackTrace();
        }
    }
}