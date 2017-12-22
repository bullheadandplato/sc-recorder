/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private static final int REQ_CODE_OVERLAY = 909;
    private PermissionResultListener mPermissionResultListener;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private FloatingActionButton fab;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SharedPreferences prefs;
    private BillingClient mBillingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String theme = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.preference_theme_key), Const.PREFS_LIGHT_THEME);
        int toolBarColor = 0;
        switch (theme) {
            case Const.PREFS_DARK_THEME:
                setTheme(R.style.AppTheme_Dark_NoActionBar);
                toolBarColor = ContextCompat.getColor(this, R.color.colorPrimary_dark);
                break;
            case Const.PREFS_BLACK_THEME:
                setTheme(R.style.AppTheme_Black_NoActionBar);
                toolBarColor = ContextCompat.getColor(this, R.color.colorPrimary_black);
                break;
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadAd();
        //initBilling
        setupBilling();

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        //set tab layout icons
        setTabIcons(tabLayout);
        tabLayout.setBackgroundColor(toolBarColor);



        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Arbitrary "Write to external storage" permission since this permission is most important for the app
        requestPermissionStorage();

        //Acquiring media projection service to start screen mirroring
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        //Respond to app shortcut
        if (getIntent().getAction() != null && getIntent().getAction().equals(getString(R.string.app_shortcut_action))) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), Const.SCREEN_RECORD_REQUEST_CODE);
            return;
        }

        fab = findViewById(R.id.fab);

        fab.hide();
        if (isServiceRunning(RecorderService.class)) {
            Log.d(Const.TAG, "service is running");
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaProjection == null && !isServiceRunning(RecorderService.class)) {
                    //Request Screen recording permission
                    startActivityForResult(mProjectionManager.createScreenCaptureIntent(), Const.SCREEN_RECORD_REQUEST_CODE);
                } else if (isServiceRunning(RecorderService.class)) {
                    //stop recording if the service is already active and recording
                    Toast.makeText(MainActivity.this, "Screen already recording", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(MainActivity.this, R.string.fab_record_hint, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        /* Enable analytics only for release builds */
        if (!BuildConfig.DEBUG) {
            Log.d(Const.TAG, "Is a release build. Setting up analytics");
            setupAnalytics();
        } else {
            Log.d(Const.TAG, "Debug build. Analytics is disabled");
        }

    }

    private boolean billingReady = false;

    private void setupBilling() {
        mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                billingReady = true;
                queryPurchase();
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingReady = false;
            }
        });

    }

    private void loadAd() {
        final InterstitialAd ad = new InterstitialAd(this);
        ad.setAdUnitId(getString(R.string.ad_interstitial_id));

        ad.loadAd(new AdRequest.Builder().addTestDevice("D03818F754E63AD6FE3E15572190E40B").build());
        ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                ad.show();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        });
    }
    private boolean purchasedTouch = false;

    private void queryPurchase() {
        Purchase.PurchasesResult result = mBillingClient.queryPurchases(Const.SKU_TARGET_ENABLE);
        if (result != null && result.getPurchasesList() != null && result.getPurchasesList().size() > 0) {
            for (Purchase purchase : result.getPurchasesList()) {
                if (Const.SKU_TARGET_ENABLE.equals(purchase.getSku())) {
                    purchasedTouch = true;
                }
            }
        }
    }

    public void makePurchase(String skuId) {
        if (billingReady) {
            BillingFlowParams params = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(BillingClient.SkuType.INAPP)
                    .build();
            mBillingClient.launchBillingFlow(this, params);
        } else {
            Toast.makeText(this, "Unable to make purchase.", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isPurchasedTarget() {
        return purchasedTouch;
    }

    private void setTabIcons(TabLayout tabLayout) {
            tabLayout.getTabAt(0).setIcon(R.drawable.ic_videocam_black_24dp);
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_settings_applications_black_24dp);
    }

    public void setupAnalytics() {
        //add fabric here
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        adapter.addFragment(new VideosListFragment(), getString(R.string.tab_videos_title));
        adapter.addFragment(new SettingsPreferenceFragment(), getString(R.string.tab_settings_title));
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 1:
                        fab.show();
                        break;
                    case 0:
                        fab.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //Method to check if the service is running
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //Overriding onActivityResult to capture screen mirroring permission request result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Result for system windows permission required to show floating controls
        if (requestCode == Const.SYSTEM_WINDOWS_CODE) {
            setSystemWindowsPermissionResult();
            return;
        }

        //The user has denied permission for screen mirroring. Let's notify the user
        if (resultCode == RESULT_CANCELED && requestCode == Const.SCREEN_RECORD_REQUEST_CODE) {
            Toast.makeText(this,
                    getString(R.string.screen_recording_permission_denied), Toast.LENGTH_SHORT).show();
            //Return to home screen if the app was started from app shortcut
            if (getIntent().getAction().equals(getString(R.string.app_shortcut_action)))
                this.finish();
            return;

        }

        /*If code reaches this point, congratulations! The user has granted screen mirroring permission
        * Let us set the recorderservice intent with relevant data and start service*/
        Intent recorderService = new Intent(this, RecorderService.class);
        recorderService.setAction(Const.SCREEN_RECORDING_START);
        recorderService.putExtra(Const.RECORDER_INTENT_DATA, data);
        recorderService.putExtra(Const.RECORDER_INTENT_RESULT, resultCode);
        startService(recorderService);
        this.finish();
    }

    //Method to create app directory which is default directory for storing recorded videos
    public static void createDir() {
        File appDir = new File(Environment.getExternalStorageDirectory() + File.separator + Const.APPDIR);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && !appDir.isDirectory()) {
            appDir.mkdirs();
        }
    }

    //Update video list fragment once save directory has been changed
    public void onDirectoryChanged() {
        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        ((VideosListFragment) adapter.getItem(1)).removeVideosList();
        Log.d(Const.TAG, "reached main act");
    }

    /* Marshmallow style permission request.
     * We also present the user with a dialog to notify why storage permission is required */
    public boolean requestPermissionStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.storage_permission_request_title))
                    .setMessage(getString(R.string.storage_permission_request_summary))
                    .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    Const.EXTDIR_REQUEST_CODE);
                        }
                    })
                    .setCancelable(false);

            alert.create().show();
            return false;
        }
        return true;
    }


    public void startOverly() {
        Intent intent = new Intent(this, OverlyPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    //Pass the system windows permission result to settings fragment
    @TargetApi(23)
    private void setSystemWindowsPermissionResult() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                mPermissionResultListener.onPermissionResult(Const.SYSTEM_WINDOWS_CODE,
                        new String[]{"System Windows Permission"},
                        new int[]{PackageManager.PERMISSION_GRANTED});
            } else {
                mPermissionResultListener.onPermissionResult(Const.SYSTEM_WINDOWS_CODE,
                        new String[]{"System Windows Permission"},
                        new int[]{PackageManager.PERMISSION_DENIED});
            }
        } else {
            mPermissionResultListener.onPermissionResult(Const.SYSTEM_WINDOWS_CODE,
                    new String[]{"System Windows Permission"},
                    new int[]{PackageManager.PERMISSION_GRANTED});
        }
    }

    // Marshmallow style permission request for audio recording
    public void requestPermissionAudio() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    Const.AUDIO_REQUEST_CODE);
        }
    }

    // Overriding onRequestPermissionsResult method to receive results of marshmallow style permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Const.EXTDIR_REQUEST_CODE:
                if ((grantResults.length > 0) &&
                        (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    Log.d(Const.TAG, "write storage Permission Denied");
                    /* Disable floating action Button in case write storage permission is denied.
                     * There is no use in recording screen when the video is unable to be saved */
                    fab.setEnabled(false);
                } else {
                    /* Since we have write storage permission now, lets create the app directory
                    * in external storage*/
                    Log.d(Const.TAG, "write storage Permission granted");
                    createDir();
                }
        }

        // Let's also pass the result data to SettingsPreferenceFragment using the callback interface
        if (mPermissionResultListener != null) {
            mPermissionResultListener.onPermissionResult(requestCode, permissions, grantResults);
        }
    }


    //Set the callback interface for permission result from SettingsPreferenceFragment
    public void setPermissionResultListener(PermissionResultListener mPermissionResultListener) {
        this.mPermissionResultListener = mPermissionResultListener;
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (!BuildConfig.DEBUG) {
            if (Countly.sharedInstance().hasBeenCalledOnStart()) {
                Countly.sharedInstance().onStop();
                Log.d(Const.TAG, "Countly stopped");
            }
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.privacy_policy:
                startActivity(new Intent(this, PrivacyPolicy.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            if (purchases != null && purchases.size() != 0) {
                purchasedTouch = true;
                Toast.makeText(MainActivity.this, "Thanks for purchasing your item.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void exitOverly() {
        Intent overlayIntent = new Intent(this, FloatingControlService.class);
        overlayIntent.setAction("stop");
        startService(overlayIntent);
    }

    //ViewPager class for tab view
    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

    }

}
