package com.example.akhil.epson;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

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


    private String ipAddress, portNumber;
    private Context context;

    private ServerResponse requestReply;


    private RequestMode requestType;
    private ArrayList<String> parameterValue;

    public HttpRequestAsyncTask(Context context, ArrayList<String> parameterValue, String ipAddress,
                                String portNumber, RequestMode requestType) {

        this.context = context;
        this.ipAddress = ipAddress;
        this.parameterValue = parameterValue;
        this.portNumber = portNumber;
        this.requestType = requestType;

        this.requestReply = ServerResponse.UNREACHABLE_HOST;



    }


    @Override
    protected Void doInBackground(Void... params) {
        InputStream inputStream = null;
        this.requestReply = sendRequest(parameterValue, ipAddress, portNumber, requestType);
        return null;
    }
    @Override
    protected void  onPostExecute(Void avoid) {



        if(this.requestType.equals(RequestMode.INIT)) {


            if(this.requestReply.equals(ServerResponse.UNREACHABLE_HOST)) {

            /*
            * CALL THE ERROR LOADING INTENT
            *
            * */
              /*  Intent loading = new Intent(this.context, RemoteActivity.class);
                loading.putExtra("status","faliure");
                context.startActivity(loading);*/
                //context.fini
                Log.d("STEP","UNREACHABLE_HOST");

                LoadingActivity.requestStatus = ConnectionStatus.FAIL;
            }
            else {
                /*Intent loading = new Intent(this.context, RemoteActivity.class);
                loading.putExtra("status","success");
                context.startActivity(loading);*/
                LoadingActivity.requestStatus = ConnectionStatus.SUCCESS;
            }
            LoadingActivity.changeOnReply = true;


        }
        else if(this.requestType.equals(RequestMode.INIT_AGAIN)) {

            if (this.requestReply.equals(ServerResponse.UNREACHABLE_HOST)) {//reply from ESP
                //RemoteActivity.status = "failure";
                RemoteActivity.connectionStatus = ConnectionStatus.FAIL;


            }
            else {

            }

            //TODO: Move this code to the else part.

            RemoteActivity.connectionStatus = ConnectionStatus.SUCCESS;

            RemoteActivity.signal = 1;
            /*fragmentTransaction.detach(currentFragment);
            fragmentTransaction.attach(currentFragment);
            fragmentTransaction.commit();*/


        }

        else if(this.requestType.equals(RequestMode.RC4KEY)) {

            if (this.requestReply.equals(ServerResponse.AUTH_FAIL)) { //reply from ESP

                //RemoteActivity.status = "AUTH_FAIL";
                RemoteActivity.connectionStatus = ConnectionStatus.AUTH_FAIL;


            }
            else {

            }

            //TODO: Move this code to the else part.
            RemoteActivity.connectionStatus = ConnectionStatus.AUTH_SUCCESS;
            RemoteActivity.signal = 1;


        }
        else if(this.requestType.equals(RequestMode.NORMAL)) {


            if(this.requestReply.equals(ServerResponse.FAIL)) {
                //RemoteActivity.status = "FAIL";

            }
            else {

            }
            //TODO: Move this code to the else part.

            RemoteActivity.connectionStatus = ConnectionStatus.SUCCESS;
            RemoteActivity.signal = 1;


        }





    }


    public ServerResponse sendRequest(ArrayList<String> parameterValue, String ipAddress,
                              String portNumber,
                              RequestMode requestType) {


            /*
            The request will be of the form :
            http://192.168.X.X:80/?mode=xxxxx&unit=xx&code=xxxx
            */
        /*
        * Request Type defines the type of request That is send
        * Request Type Can Be Of The Following Forms :
          * init/initagain : Initial Message Which Checks The Connection
          * rc4key: Initialises The RSA KEY
          * normal: Normal Messages
        * */

        InputStream inputStream;
        String response;
        int responseCode;
        ServerResponse serverResponse = ServerResponse.UNREACHABLE_HOST;

        String link = "";


        if(requestType.equals(RequestMode.INIT) || requestType.equals(RequestMode.INIT_AGAIN))
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0);
        else if(requestType.equals(RequestMode.RC4KEY))
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0)
                    +"&unit="+parameterValue.get(1);
        else if(requestType.equals(RequestMode.NORMAL))
            link = "http://"+ipAddress+":"+portNumber+"/?"+"mode="+parameterValue.get(0)
                    +"&code="+parameterValue.get(1);

        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(false);
            conn.setRequestMethod("GET");

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            inputStream = new BufferedInputStream(conn.getInputStream());
            response = org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");
            responseCode = Integer.parseInt(response);
            serverResponse = ParameterFactory.getServerResponse(responseCode);


            inputStream.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return serverResponse;
    }
}
