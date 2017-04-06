package com.example.akhil.epson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.util.ArrayList;

public class ConnectionActivity extends Activity {


    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
    public SharedPreferences sharedPreferences;
    public static ConnectionStatus requestStatus = null;
    public static boolean changeOnReply = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_connection);

        /*Loading Animated GIF*/
        ImageView imageGifView = (ImageView) findViewById(R.id.loading_animation);
        GlideDrawableImageViewTarget gifImageViewTarget = new GlideDrawableImageViewTarget(imageGifView);
        Glide.with(this)
             .load(R.raw.loading)
             .into(gifImageViewTarget);

        /*Loading EPSON Image */
        ImageView imageLogoView = (ImageView) findViewById(R.id.loading_image);
        GlideDrawableImageViewTarget logoImageViewTarget = new GlideDrawableImageViewTarget(imageLogoView);
        Glide.with(this)
                .load(R.mipmap.epson_logo)
                .into(logoImageViewTarget);


        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        ArrayList<String> parameterValue = new ArrayList<String>();
        parameterValue.add(0,ParameterFactory.getMode(RequestMode.INIT));

        String ipAddress = sharedPreferences.getString(PREF_IP,"NULL");
        String portNumber = sharedPreferences.getString(PREF_PORT,"NULL");

        RequestMode requestType = RequestMode.INIT;

        new RequestHandler(findViewById(android.R.id.content).getContext(), parameterValue, ipAddress,
                portNumber, requestType);
    }

    private class RequestHandler implements Runnable {


        private Context context;
        private RequestMode requestType;
        private ArrayList<String> parameterValue;
        String ipAddress, portNumber;

        public RequestHandler(Context context, ArrayList<String> parameterValue, String ipAddress,
                              String portNumber, RequestMode requestType) {

            this.context = context;
            this.parameterValue = parameterValue;
            this.ipAddress = ipAddress;
            this.portNumber = portNumber;
            this.requestType = requestType;

            Thread t = new Thread(this, "REQUEST");
            t.start();

        }

        @Override
        public void run() {


            new HttpRequestAsyncTask(
                    this.parameterValue, this.ipAddress, this.portNumber,
                    this.requestType).execute();
            while(!changeOnReply) {

                try{Thread.sleep(1000);}catch (Exception e){e.printStackTrace();}
            }
            Intent loading = new Intent(this.context, RemoteActivity.class);
            loading.putExtra("status",requestStatus);
            context.startActivity(loading);
            finish();
        }

    }
}
