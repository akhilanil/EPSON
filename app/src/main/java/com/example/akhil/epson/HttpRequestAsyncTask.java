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
import java.sql.Connection;
import java.util.ArrayList;

/**
 * Created by akhil on 15/3/17.
 */

class HttpRequestAsyncTask extends AsyncTask<Void, Void, Void> {


    private String ipAddress, portNumber;

    private ServerResponse requestReply;


    private RequestMode requestType;
    private ArrayList<String> parameterValue;

    public HttpRequestAsyncTask(ArrayList<String> parameterValue, String ipAddress,
                                String portNumber, RequestMode requestType) {

        this.ipAddress = ipAddress;
        this.parameterValue = parameterValue;
        this.portNumber = portNumber;
        this.requestType = requestType;

        this.requestReply = ServerResponse.UNREACHABLE_HOST;



    }


    @Override
    protected Void doInBackground(Void... params) {
        InputStream inputStream = null;
        RemoteActivity.connectionStatus = ConnectionStatus.FAIL;
        this.requestReply = sendRequest(parameterValue, ipAddress, portNumber, requestType);
        return null;
    }
    @Override
    protected void  onPostExecute(Void avoid) {


        if(this.requestType.equals(RequestMode.INIT)) {
            if(this.requestReply.equals(ServerResponse.UNREACHABLE_HOST)) {
                Log.d("STEP","UNREACHABLE_HOST");
                ConnectionActivity.requestStatus = ConnectionStatus.FAIL;
            }
            else {
                ConnectionActivity.requestStatus = ConnectionStatus.SUCCESS;
            }
            ConnectionActivity.changeOnReply = true;
        }
        else if(this.requestType.equals(RequestMode.RELOAD)) {

            if(this.requestReply.equals(ServerResponse.UNREACHABLE_HOST)) {
                RemoteActivity.connectionStatus = ConnectionStatus.FAIL;
            }
            else {
                RemoteActivity.connectionStatus = ConnectionStatus.SUCCESS;
            }
            RemoteActivity.signal = 1;

        }
        else if(this.requestType.equals(RequestMode.INIT_AGAIN)) {

            if (this.requestReply.equals(ServerResponse.UNREACHABLE_HOST)) {//reply from ESP
                RemoteActivity.connectionStatus = ConnectionStatus.FAIL;
            }
            else {
                RemoteActivity.connectionStatus = ConnectionStatus.SUCCESS;
            }

            RemoteActivity.signal = 1;
        }

        else if(this.requestType.equals(RequestMode.RC4KEY)) {

            if (this.requestReply.equals(ServerResponse.AUTH_FAIL)) { //reply from ESP
                RemoteActivity.connectionStatus = ConnectionStatus.AUTH_FAIL;
            }
            else {
                RemoteActivity.connectionStatus = ConnectionStatus.AUTH_SUCCESS;
            }
            RemoteActivity.signal = 1;
        }
        else if(this.requestType.equals(RequestMode.NORMAL)) {


            if(this.requestReply.equals(ServerResponse.FAIL)) {
                RemoteActivity.connectionStatus = ConnectionStatus.FAIL;
            }
            else {
                RemoteActivity.connectionStatus = ConnectionStatus.SUCCESS;
            }
            RemoteActivity.signal = 1;
        }
        else if(this.requestType.equals(RequestMode.LIGHT)) {

            if(this.requestReply.equals(ServerResponse.FAIL)) {
                RemoteActivity.connectionStatus = ConnectionStatus.FAIL;
            }
            else {
                RemoteActivity.connectionStatus = ConnectionStatus.SUCCESS;
            }
            RemoteActivity.signal = 1;

        }

        else if(this.requestType.equals(RequestMode.FINISH)) {

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
          * finish: To disconnect
        * */


        InputStream inputStream;
        String response;
        int responseCode;
        ServerResponse serverResponse = ServerResponse.UNREACHABLE_HOST;

        String link = "";



        if(requestType.equals(RequestMode.INIT) || requestType.equals(RequestMode.INIT_AGAIN)
                || requestType.equals(RequestMode.RELOAD)) {
            link = "http://" + ipAddress + ":" + portNumber + "/?" + "mode=" + parameterValue.get(0);
        }
        else if(requestType.equals(RequestMode.RC4KEY)) {
            link = "http://" + ipAddress + ":" + portNumber + "/?" + "mode=" + parameterValue.get(0)
                    + "&value=" + parameterValue.get(1);
        }
        else if(requestType.equals(RequestMode.NORMAL)) {
            link = "http://" + ipAddress + ":" + portNumber + "/?" + "mode=" + parameterValue.get(0)
                    + "&value=" + parameterValue.get(1);
        }
        else if(this.requestType.equals(RequestMode.LIGHT)) {
            link = "http://" + ipAddress + ":" + portNumber + "/?" + "mode=" + parameterValue.get(0)
                    + "&value=" + parameterValue.get(1);
            Log.d("LINK: ", link);
        }
        else if(this.requestType.equals(RequestMode.FINISH)) {
            link = "http://" + ipAddress + ":" + portNumber + "/?" + "mode=" + parameterValue.get(0);
        }
        try {

            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(false);
            conn.setRequestMethod("GET");

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            inputStream = new BufferedInputStream(conn.getInputStream());
            response = org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");

            if(response.length() == 1) {
                responseCode = Integer.parseInt(response);
                serverResponse = ParameterFactory.getServerResponse(responseCode);
                inputStream.close();
                Log.d("Steps", response);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Link",link);

        return serverResponse;
    }
}
