/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String theme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.preference_theme_key), Const.PREFS_LIGHT_THEME);
        switch (theme) {
            case Const.PREFS_DARK_THEME:
                setTheme(R.style.AppTheme_Dark);
                break;
            case Const.PREFS_BLACK_THEME:
                setTheme(R.style.AppTheme_Black);
                break;
        }

        super.onCreate(savedInstanceState);

        // Set up arrow to close the activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_about);

        //Let's set the copyright and app version dynamically
        TextView appVersion = findViewById(R.id.versionTxt);
        TextView videoEditorCredit = findViewById(R.id.video_editor_lib_credit_tv);

        videoEditorCredit.setText(getString(R.string.video_editor_library_credit, "knowledge4life",
                "https://github.com/knowledge4life/k4l-video-trimmer",
                "MIT Opensource License"));


        appVersion.setText(BuildConfig.VERSION_NAME);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //finish this activity and return to parent activity
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
