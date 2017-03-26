package com.example.akhil.epson;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.akhil.decode.InitRC4;

public class MainActivity extends AppCompatActivity {

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";

    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    Button connect;
    EditText ip, port;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        ip = (EditText)findViewById(R.id.ip);
        port = (EditText)findViewById(R.id.port);

        editor.putString(PREF_IP,"192.168.2.102");
        editor.putString(PREF_PORT,"80");
        editor.commit();


        if(!sharedPreferences.getString(PREF_IP,"").equals("")){

            Intent loading = new Intent(MainActivity.this, LoadingActivity.class);
            startActivity(loading);
            finish();

        }

        connect = (Button) findViewById(R.id.connect);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ipAddress =  ip.getText().toString().trim();
                String portNumber = port.getText().toString().trim();

                Log.d("ONclick",ipAddress + portNumber);

                if(!ipAddress.equals("") && !portNumber.equals("")) {
                    editor.putString(PREF_IP, ipAddress);
                    editor.putString(PREF_PORT, portNumber);
                    editor.commit();
                    Log.d("ONclick","in");

                    Intent loading = new Intent(MainActivity.this, LoadingActivity.class);
                    startActivity(loading);
                    finish();

                }
            }
        });




        AssetManager assetManager = getAssets();
    }
}
