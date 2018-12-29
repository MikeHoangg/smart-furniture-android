package mikehoang.smartfurniture;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApplyOptions {
    @POST("api/v1/apply-options")
    Call<Response> applyOptions(@Header("Authorization") String token, @Query("options") int options, @Query("furniture") int furniture);

    @POST("api/v1/discard-options")
    Call<Response> discardOptions(@Header("Authorization") String token, @Query("user") int user, @Query("furniture") int furniture);
}
