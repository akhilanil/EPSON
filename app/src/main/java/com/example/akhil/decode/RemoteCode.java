package com.example.akhil.decode;

import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by akhil on 11/3/17.
 */

public class RemoteCode {


    AssetManager assetManager;
    JSONObject epson;

    RemoteCode(AssetManager assetManager){

        Log.d("Constructor","REMOTECODE");
        this.assetManager = assetManager;
        initialiseCode();


    }

    long getRemoteCode (String buttonPressed) {
        long buttonCode = 0;

        try {
            String code = epson.getString(buttonPressed);
            if(!code.equals("XX"))
                buttonCode = Long.parseLong(code);
            Log.d("Code",String.valueOf(buttonCode));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return buttonCode;
    }

    private void initialiseCode() {

        try {
            JSONObject jsonObject = new JSONObject(loadJSON());
            epson = jsonObject.getJSONObject("EPSONPROJECTOR");


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String loadJSON() {

        String json = "";

        try {

            InputStream inputStream = assetManager.open("RemoteCode.json");

            int size = inputStream.available();
            byte[] buffer = new byte[size];

            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer,"UTF-8");

        }catch (IOException e) {
            Log.d("ERROR",e.getMessage());
            return null;
        }

        if(json.equals(""))
            Log.d("JSON","EMPTY");
        return json;

    }


}
