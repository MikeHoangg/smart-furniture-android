package mikehoang.smartfurniture;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApplyOptions {
    @POST("apply-options")
    Call<ResponseBody> applyOptions(@Header("Authorization") String token, @Query("options") int options, @Query("furniture") int furniture);

    @POST("discard-options")
    Call<ResponseBody> discardOptions(@Header("Authorization") String token, @Query("user") int user, @Query("furniture") int furniture);
}
