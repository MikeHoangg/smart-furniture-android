package mikehoang.smartfurniture;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface User {
    @GET("api/v1/user")
    Call<Response> getCurrentUser(@Header("Authorization") String token);

    @PUT("api/v1/user")
    Call<Response> editCurrentUser(@Header("Authorization") String token, @Query("username") String username, @Query("email") String email, @Query("first_name") String firstName, @Query("last_name") String lastName, @Query("height") double height);
}
