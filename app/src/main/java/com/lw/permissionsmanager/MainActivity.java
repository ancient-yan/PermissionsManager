package com.lw.permissionsmanager;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

        int nCmd = 5;

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
                Uri uri = Uri.parse("content://com.lw.permissionsmanager.provider/package_permission");
                ContentValues values = new ContentValues();
                values.put("granted", "0");

                getContentResolver().update(uri, values,
                        "packageName = 'com.lw.permissionsmanager' and permissionName = 'android.permission.READ_CONTACTS'", null);
            }
            break;

            case 3:
            {
                Uri uri = Uri.parse("content://com.lw.permissionsmanager.provider");

                ContentValues values = new ContentValues();
                values.put("packageName", "com.lw.permissionsmanager");
                values.put("permissionName", "android.permission.READ_CONTACTS");
                values.put("granted", "0");
                values.put("create_time", "0");
                values.put("update_time", "0");

                getContentResolver().insert(uri, values);
            }
            break;

            case 4:
            {
                Uri uri = Uri.parse("content://com.lw.permissionsmanager.provider/package_permission");
                Cursor cursor = null;

                cursor = getContentResolver().query(uri, null, "packageName = 'com.lw.permissionsmanager' and permissionName = 'android.permission.READ_CONTACTS'", null, null);
                if(cursor == null || cursor.getCount() == 0)
                {
                    break;
                }
                if(cursor.moveToFirst() )
                {
                    int granted = -1;
                    granted = cursor.getInt(cursor.getColumnIndex("granted") );

                    Log.e(TAG, "granted : " + granted);
                }
            }
            break;

            case 5:
            {
                Uri uri = Uri.parse("content://com.lw.permissionsmanager.provider/package");

                getContentResolver().delete(uri, " packageName = 'com.lw.permissionsmanager' ", null);
            }
            break;
        }
    }
}
