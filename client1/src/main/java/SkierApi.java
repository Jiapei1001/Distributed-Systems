import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;
import java.util.ArrayList;

public class SkierApi {

    public static void main(String[] args) {

        SkiersApi apiInstance = new SkiersApi();
        ApiClient client = apiInstance.getApiClient();
        client.setBasePath("http://localhost:8080/server");

        //ResortIDSeasonsBody body = new ResortIDSeasonsBody(); // ResortIDSeasonsBody | Specify new Season value
        LiftRide ride = new LiftRide();
        ride.setTime(217);
        ride.setLiftID(21);
        ride.setWaitTime(3);

        Integer resortID = 56; // Integer | ID of the resort of interest
        try {
            //Integer res = apiInstance.getSkierDayVertical(1, "2020", "255", 4);
            //System.out.println(res);
            //
            //SkierVertical vertical = apiInstance.getSkierResortTotals(1,new ArrayList<>(), new ArrayList<>());
            //System.out.println(vertical);

            apiInstance.writeNewLiftRide(ride, 1, "2022", "255", 2);

        } catch (ApiException e) {
            System.err.println("Exception when calling ResortsApi#addSeason");
            e.printStackTrace();
        }
    }
}