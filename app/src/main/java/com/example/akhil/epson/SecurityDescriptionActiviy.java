package com.example.akhil.epson;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class SecurityDescriptionActiviy extends AppCompatActivity {


    Button next;
    ImageView lockImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_description_activiy);

        lockImage = (ImageView) findViewById(R.id.titleImage);
        next = (Button) findViewById(R.id.to_next_connection);

        GlideDrawableImageViewTarget logoImageViewTarget =
                new GlideDrawableImageViewTarget(lockImage);
        Glide.with(this)
             .load(R.mipmap.ic_launcher)
             .into(lockImage);

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
