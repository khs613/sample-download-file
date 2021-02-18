package com.khs.sample_download_file;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
    static final public int TYPE_MOBILE = 1;
    static final public int TYPE_WIFI = 2;
    static final public int TYPE_NOT_CONNECTED = 3;

    /**
     * 네트워크 종류 반환
     * @param context
     * @return
     */
    public static int getActiveNetworkStatus(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo != null){
            int type = networkInfo.getType();
            if(type == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE;
            } else if (type == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI;
            }
        }
        return TYPE_NOT_CONNECTED;
    }
}
