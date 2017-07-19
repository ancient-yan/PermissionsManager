package com.lw.permissionsmanager;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/4.
 */

public class TitleList extends ListActivity {
    private final static String TAG = "my_log";

    ListView mListView = null;
    ArrayList<Map<String,Object>> mData= new ArrayList<Map<String,Object>>();;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Intent intent = this.getIntent();

        Log.e(TAG, " permission : " + intent.getStringExtra("permission") );

        mListView = getListView();

        {
            Uri uri = Uri.parse("content://com.lw.permissionsmanager.provider/permission");
            Cursor cursor = null;

            cursor = getContentResolver().query(uri, null, " permissionName = '" + intent.getStringExtra("permission") + "' ", null, null);
            if(cursor == null || cursor.getCount() == 0)
            {
                super.onCreate(savedInstanceState);

                return;
            }

            String packageName = "";
            int granted = -1;
            int rowid = -1;
            boolean bRet = cursor.moveToFirst();
            while(bRet)
            {
                packageName = cursor.getString(cursor.getColumnIndex("packageName") );
                granted = cursor.getInt(cursor.getColumnIndex("granted") );
                rowid = cursor.getInt(cursor.getColumnIndex("rowid") );

                Log.e(TAG, "rowid : " + rowid);
                Log.e(TAG, "packageName : " + packageName);
                Log.e(TAG, "granted : " + granted);

                {
                    Map<String,Object> item = new HashMap<String,Object>();
                    item.put("rowid", rowid);
                    item.put("packageName", packageName);
                    item.put("granted", granted);
                    mData.add(item);
                }

                bRet = cursor.moveToNext();
            }
        }

        final SimpleAdapter adapter = new SimpleAdapter(this,mData,android.R.layout.simple_list_item_2,
                new String[]{"packageName","granted"},new int[]{android.R.id.text1,android.R.id.text2});
        setListAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                    long id)
            {
                Log.e(TAG, "position : " + position);
                Map<String,Object> item = mData.get(position);

                Log.e(TAG, "packageName : " + item.get("packageName") );
                Log.e(TAG, "granted : " + item.get("granted") );
                Log.e(TAG, "rowid : " + item.get("rowid") );

                {
                    Uri uri = Uri.parse("content://com.lw.permissionsmanager.provider/package_permission");
                    ContentValues values = new ContentValues();
                    int granted = (int) item.get("granted");

                    boolean bGranted = true;

                    if(1 == granted) {
                        values.put("granted", "0");

                        item.remove("granted");
                        item.put("granted", 0);

                        bGranted = false;
                    }
                    else {
                        values.put("granted", "1");

                        item.remove("granted");
                        item.put("granted", 1);

                        bGranted = true;
                    }

                    getContentResolver().update(uri, values,
                            " rowid = " + item.get("rowid"), null);

                    {
                        PackageManager packageManager = getPackageManager();
                        boolean bRet = false;

                        Intent intent = TitleList.this.getIntent();

                        Log.e(TAG, " permission : " + intent.getStringExtra("permission") );

                        bRet = packageManager.LwSetPermission((String) item.get("packageName"), intent.getStringExtra("permission"), bGranted);

                        Log.e(TAG,  "bRet : " + bRet);
                    }

                    adapter.notifyDataSetChanged();
                }
            }
        });

        super.onCreate(savedInstanceState);
    }

}
