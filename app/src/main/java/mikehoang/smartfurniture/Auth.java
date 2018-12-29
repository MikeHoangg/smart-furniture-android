package mikehoang.smartfurniture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Auth {
    @FormUrlEncoded
    @POST("login/")
    Call<ResponseBody> login(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("logout/")
    Call<ResponseBody> logout();

    @FormUrlEncoded
    @POST("register/")
    Call<ResponseBody> register(@Field("username") String username, @Field("email") String email, @Field("password1") String password1, @Field("password2") String password2);
}
