/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

/**
 * Created by bullhead on 12/22/17.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, getString(R.string.ad_app_id));
    }
}
