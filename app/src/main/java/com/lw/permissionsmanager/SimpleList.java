package com.lw.permissionsmanager;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Administrator on 2017/7/3.
 */

public class SimpleList extends ListActivity {
    private final static String TAG = "my_log";

    private String[] mListStr = {"android.permission.INTERNET"};
    ListView mListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mListView = getListView();
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mListStr));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                    long id) {
                Log.e(TAG, " Item : " + mListStr[position]);

                startActivity(new Intent(SimpleList.this, TitleList.class) );
            }
        });
        super.onCreate(savedInstanceState);

        PackageManager packageManager = getPackageManager();
        for (PackageInfo pInfo : packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS) )
        {
            Log.e(TAG, "packageName : " + pInfo.packageName);
            if(null == pInfo.requestedPermissions) continue;
            for (String requestedPerm : pInfo.requestedPermissions)
            {
                if("android.permission.INTERNET".equals(requestedPerm) )
                {
                    Log.e(TAG, "requestedPerm : " + requestedPerm);
                    {
                        Uri uri = Uri.parse("content://com.lw.permissionsmanager.provider");

                        ContentValues values = new ContentValues();
                        values.put("packageName", pInfo.packageName);
                        values.put("permissionName", requestedPerm);
                        values.put("granted", "0");
                        values.put("create_time", "0");
                        values.put("update_time", "0");

                        getContentResolver().insert(uri, values);
                    }
                }
            }
        }
    }
}
