package com.kartik.updatingdialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView textViewCurrentVersion;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCurrentVersion=findViewById(R.id.textViewCurrentVersion);
        textViewCurrentVersion.setText("Current Version Code: "+getVersionCode());


        HashMap<String, Object> defaultRate=new HashMap<>();
        defaultRate.put("new_version_code",String.valueOf(getVersionCode()));


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultRate);

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()){
                    final String new_version_code=mFirebaseRemoteConfig.getString("new_version_code");
                    if (Integer.parseInt(new_version_code)>getVersionCode())
                        showDialog("com.facebook.lite",new_version_code);
                }
            }
        });





    }


    private void showDialog(final String appPackageName,String versionFromRemoteConfig){
        final AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle("Update now")
                .setMessage("This version is old. Update to the latest version of "+ R.string.app_name+versionFromRemoteConfig)
                .setPositiveButton("Update",null)
                .show();
        dialog.setCancelable(false);


        Button positiveButton=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" +appPackageName)));
                }catch (android.content.ActivityNotFoundException activityNotFoundException){
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?details?id=" +appPackageName)));
                }
            }
        });


    }


    private PackageInfo packageInfo;
    public int getVersionCode(){
        packageInfo=null;
        try {
            packageInfo=getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e){
            Log.i("MY_LOG","NameNotFoundException: "+e.getMessage());
        }
        return packageInfo.versionCode;
    }
}