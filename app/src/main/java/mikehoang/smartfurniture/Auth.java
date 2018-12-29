package mikehoang.smartfurniture;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Auth {
    @POST("api/v1/login")
    Call<Response> login(@Query("username") String username, @Query("password") String password);

    @POST("api/v1/logout")
    Call<Response> logout();

    @POST("api/v1/register")
    Call<Response> register(@Query("username") String username, @Query("email") String email, @Query("password1") String password1, @Query("password2") String password2);
}
