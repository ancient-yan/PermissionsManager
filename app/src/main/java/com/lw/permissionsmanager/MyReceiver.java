package com.lw.permissionsmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

	private final static String  TAG = "my_log";

	@Override
	 public void onReceive(Context context, Intent intent)
	  {
		  if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) )
		  {
			  Log.e(TAG, "MyReceiver -> ACTION_BOOT_COMPLETED");
		  }
	  }
}