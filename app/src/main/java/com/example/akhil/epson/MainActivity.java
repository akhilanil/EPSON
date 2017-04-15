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
    Button next;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        if(!sharedPreferences.getString(PREF_IP,"").equals("")){
            Intent loading = new Intent(MainActivity.this, ConnectionActivity.class);
            startActivity(loading);
            finish();
        }
        else {

            next = (Button) findViewById(R.id.to_next_security);

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent loading = new Intent(MainActivity.this, SecurityDescriptionActiviy.class);
                    startActivity(loading);
                    finish();

                }
            });



        }






        AssetManager assetManager = getAssets();
    }
    @Override
    protected void onDestroy() {
        Log.d("DONE","MAIN");
        super.onDestroy();
    }
}
