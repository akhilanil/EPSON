package com.example.akhil.epson;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by akhil on 3/4/17.
 */

public class NetworkDisconnect extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> parameterValue = new ArrayList<String>();
                String ipAddress = RemoteActivity.sharedPreferences.getString(RemoteActivity.PREF_IP,"");
                String port = RemoteActivity.sharedPreferences.getString(RemoteActivity.PREF_PORT,"");
                parameterValue.add(0,ParameterFactory.getMode(RequestMode.FINISH));
                Log.d("DONE","SERVICES");
                RemoteActivity.signal = 0;
                new HttpRequestAsyncTask(parameterValue, ipAddress, port, RequestMode.FINISH).execute();
                while(RemoteActivity.signal == 0) {
                    try{Thread.sleep(1000);}catch (Exception e){}
                }
                stopSelf();
            }
        }).start();




    }


}
