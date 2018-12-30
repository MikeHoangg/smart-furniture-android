package mikehoang.smartfurniture;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface Types {
    @GET("furniture-types/")
    Call<List<ResponseBody>> getFurnitureTypes();

    @GET("massage-rigidity-types/")
    Call<List<ResponseBody>> getRigidityMassageTypes();
}
