package com.lw.permissionsmanager;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "my_log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int nCmd = 2;

        Log.e(TAG, "nCmd : " + nCmd);

        switch(nCmd)
        {
            case  1:
            {
                final  int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "!= PackageManager.PERMISSION_GRANTED");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                }else{
                }
            }
            break;

            case 2:
            {
                Uri uri = Uri.parse("content://com.lw.permissionsmanager.provider");
                ContentValues values = new ContentValues();
                values.put("mark", "dd");

                getContentResolver().update(uri, values, " item = 1 ", null);
            }
            break;
        }
    }
}
