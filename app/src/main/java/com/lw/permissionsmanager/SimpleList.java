package com.lw.permissionsmanager;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
            }
        });

        super.onCreate(savedInstanceState);
    }
}
