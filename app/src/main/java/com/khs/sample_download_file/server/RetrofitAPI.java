package com.khs.sample_download_file.server;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RetrofitAPI {
    String	MOCK_SERVER_URL	= "https://f3a07bac-0217-476a-af05-330554d63fda.mock.pstmn.io/";

    @GET("downloads")
    Call<ResponseBody>getURL();
}
