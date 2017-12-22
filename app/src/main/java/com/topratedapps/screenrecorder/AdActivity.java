/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by bullhead on 12/22/17.
 */

public class AdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#212121"));
        setContentView(R.layout.splash_layout);
        loadAd();
    }

    private void loadAd() {
        final InterstitialAd ad = new InterstitialAd(this);
        ad.setAdUnitId(getString(R.string.ad_interstitial_id));
        ad.loadAd(new AdRequest.Builder().build());
        ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                ad.show();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                startNextActivity();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                startNextActivity();
            }
        });
    }

    private void startNextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
