package com.example.akhil.epson;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by akhil on 15/3/17.
 */

class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {


    private String requestReply,ipAddress, portNumber;
    private Context context;


    private String requestType;
    private ArrayList<String> parameterValue;
    private FragmentTransaction fragmentTransaction;
    private Fragment currentFragment;

    public HttpRequestAsyncTask(Context context, ArrayList<String> parameterValue, String ipAddress,
                                String portNumber, String requestType) {

        this.context = context;
        this.ipAddress = ipAddress;
        this.parameterValue = parameterValue;
        this.portNumber = portNumber;
        this.requestType = requestType;

        this.requestReply = "ERROR";



    }

    public HttpRequestAsyncTask(Context context, ArrayList<String> parameterValue, String ipAddress,
                                String portNumber, String requestType,
                                FragmentTransaction fragmentTransaction, Fragment currentFragment) {

        this.context = context;
        this.ipAddress = ipAddress;
        this.parameterValue = parameterValue;
        this.portNumber = portNumber;
        this.requestType = requestType;
        this.fragmentTransaction =fragmentTransaction;
        this.currentFragment = currentFragment;

        this.requestReply = "ERROR";



    }

    @Override
    protected Void doInBackground(Void... params) {
        InputStream inputStream = null;

            /*alertDialog.setMessage("Data sent, waiting for reply from device...");
            alertDialog.setCancelable(false);
            if(!alertDialog.isShowing()) {
                alertDialog.show();
            }*/
        this.requestReply = sendRequest(parameterValue, ipAddress, context,
                portNumber, inputStream, requestType);
        return null;
    }
    @Override
    protected void  onPostExecute(Void avoid) {

        Toast.makeText(this.context, this.requestReply, Toast.LENGTH_SHORT).show();

        if(this.requestType.equals("init")) {

            if(this.requestReply.equals("ERROR CANNOT FIND HOST")) {

            /*
            * CALL THE ERROR LOADING INTENT
            *
            * */
                Intent loading = new Intent(this.context, RemoteActivity.class);
                loading.putExtra("status","faliure");
                context.startActivity(loading);
            }
            else {
                Intent loading = new Intent(this.context, RemoteActivity.class);
                loading.putExtra("status","success");
                context.startActivity(loading);
            }

        }
        else if(this.requestType.equals("initagain")) {

            if (this.requestReply.equals("ERROR CANNOT FIND HOST")) {//reply from ESP
                //RemoteActivity.status = "failure";



            }
            else {

            }

            //TODO: Move this code to the else part.
            RemoteActivity.status = "success";
            RemoteActivity.signal = 1;
            /*fragmentTransaction.detach(currentFragment);
            fragmentTransaction.attach(currentFragment);
            fragmentTransaction.commit();*/
            Log.d("NEW",RemoteActivity.status);

        }

        else if(this.requestType.equals("rc4key")) {

            if (this.requestReply.equals("AUTH_FAIL")) { //reply from ESP

                //RemoteActivity.status = "AUTH_FAIL";


            }
            else {

            }

            //TODO: Move this code to the else part.
            RemoteActivity.status = "AUTH_SUCCESS";

            Log.d("NEW",RemoteActivity.status);

        }




        //Log.d("CONNECTION","DONE");
    }


    public String sendRequest(ArrayList<String> parameterValue, String ipAddress, Context context,
                              String portNumber,InputStream inputStream,
                              String requestType) {


            /*
            The request will be of the form :
            http://192.168.X.X:80/?mode=xxxxx&unit=xx&code=xxxx
            */
        /*
        * Request Type defines the type of request That is send
        * Request Type Can Be Of The Following Forms :
          * init : Initial Message Which Checks The Connection
          * rc4key: Initialises The RSA KEY
          * normal: Normal Messages
        * */

        String serverResponse = "ERROR CANNOT FIND HOST";

        String link = "";


        if(requestType.equals("init") || requestType.equals("initagain"))
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0);
        else if(requestType.equals("rc4key"))
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0)
                    +"&unit="+parameterValue.get(1);
        else
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0)
                    +"&unit="+parameterValue.get(1)+"&code="+parameterValue.get(2);

        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            inputStream = new BufferedInputStream(conn.getInputStream());
            serverResponse = org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");
            inputStream.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return serverResponse;
    }
}
