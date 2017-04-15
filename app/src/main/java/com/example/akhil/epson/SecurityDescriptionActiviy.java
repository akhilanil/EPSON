package com.example.akhil.epson;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SecurityDescriptionActiviy extends AppCompatActivity {


    Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_description_activiy);

        next = (Button) findViewById(R.id.to_next_connection);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loading = new Intent(SecurityDescriptionActiviy.this,
                        ConnectionDetailsActivity.class);
                startActivity(loading);
                finish();

            }
        });
    }
}
