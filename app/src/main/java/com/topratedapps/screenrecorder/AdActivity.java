/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by bullhead on 12/22/17.
 *
 */

public class AdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#212121"));
        setContentView(R.layout.splash_layout);
        //insert anti-pattern
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startNextActivity();
            }
        }, 100);
    }



    private void startNextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
