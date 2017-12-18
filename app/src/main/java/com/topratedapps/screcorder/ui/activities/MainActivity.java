package com.topratedapps.screcorder.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.topratedapps.screcorder.R;
import com.topratedapps.screcorder.services.RecordService;

public class MainActivity extends AppCompatActivity {
    private Intent mServiceIntent;
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_main);
        initToolbar();
        initService();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initService() {
        mServiceIntent = new Intent(activity, RecordService.class);
        startService(mServiceIntent);
    }
}
