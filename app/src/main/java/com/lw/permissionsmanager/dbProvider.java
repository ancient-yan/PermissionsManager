package com.lw.permissionsmanager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class dbProvider extends ContentProvider {
	private static final String TAG = "my_log";
	private static final String DATABASE_NAME = "PermissionsManager.db";
    private static final int DATABASE_VERSION = 1;
	private static final String strTable_Name = "permissions_manager";

	private static final int PACKAGE_PERMISSION 				= 100;
	private static final int PACKAGE_NAME      				= 101;
	private static final int PERMISSION_NAME     				= 102;

    private static final UriMatcher sUriMatcher;
    private static final String AUTHORITY = "com.lw.permissionsmanager.provider";

	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.e(TAG, "public void onCreate(SQLiteDatabase db)");

			String CREATE_TABLE_PERMISSIONS_MANAGER =
					"CREATE TABLE " + strTable_Name +
							"(packageName STRING NOT NULL, " +
							"permissionName STRING NOT NULL, " +
							"granted INTEGER NOT NULL, " +
							"create_time INTEGER NOT NULL, " +
							"update_time INTEGER NOT NULL, " +
							"PRIMARY KEY(packageName, permissionName) ON CONFLICT REPLACE)";

 	        try
	        {
				db.execSQL(CREATE_TABLE_PERMISSIONS_MANAGER);
	        } catch (SQLException e)
	        {
	        }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
	
	private DatabaseHelper mOpenHelper;

	@Override
    public boolean onCreate() {
		Log.e(TAG, "onCreate");
		
		mOpenHelper = new DatabaseHelper(getContext() );

		return true;
    }

	@Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder)
	{
		Log.e(TAG, " uri : " + uri.toString() );
		Log.e(TAG, " selection : " + selection);
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		switch(sUriMatcher.match(uri) )
		{
			case PACKAGE_PERMISSION:
			{
				String strSelect = "select granted from " + strTable_Name + " where " + selection;

				Log.e(TAG, " strSelect : " + strSelect);

				Cursor cur = db.rawQuery(strSelect, null);
				cur.setNotificationUri(getContext().getContentResolver(), uri);

				return cur;
			}
			case PERMISSION_NAME:
			{
				String strSelect = "select rowid, packageName, granted from " + strTable_Name + " where " + selection;

				Log.e(TAG, " strSelect : " + strSelect);

				Cursor cur = db.rawQuery(strSelect, null);
				cur.setNotificationUri(getContext().getContentResolver(), uri);

				return cur;
			}
		}

		return null;
    }
	
    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
	{
		Log.e(TAG, " uri : " + uri.toString() );
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String strSourceID = initialValues.getAsString("sourceid");
		String strInsert = "insert into " + strTable_Name +
				"(packageName, permissionName, granted, create_time, update_time) " +
				"values(" +
				"'" + initialValues.getAsString("packageName") + "', " +
				"'" + initialValues.getAsString("permissionName") + "', " +
				"'" + initialValues.getAsString("granted") + "', " +
				"'" + initialValues.getAsString("create_time") + "', " +
				"'" + initialValues.getAsString("update_time") + "')";

		Log.e(TAG, " strInsert : " + strInsert);

		try
		{
			db.execSQL(strInsert);
		} catch (SQLException e)
		{
		}

    	return uri;
    }
    
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
		Log.e(TAG, " uri : " + uri.toString() );
		Log.e(TAG, " where : " + where);

    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	
    	final String strDelete;    	
    	switch (sUriMatcher.match(uri)) {
	    	case PACKAGE_NAME:
	    	{
	    		strDelete = "delete from " + strTable_Name + " where " + where;

				Log.e(TAG, " strDelete : " + strDelete);

	    		try 
    	        {
	    			db.execSQL(strDelete);
    	        } catch (SQLException e) 
    	        {
    	        }
	    	}
	    	break;
    	}
    	
    	getContext().getContentResolver().notifyChange(uri, null);
    	
    	return 0;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
	{
		Log.e(TAG, " uri : " + uri.toString() );
		Log.e(TAG, " where : " + where);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch(sUriMatcher.match(uri) )
		{
			case PACKAGE_PERMISSION:
			{
				final String strUpdate = "update " + strTable_Name +
						" set granted = '" + values.getAsString("granted") +
						"' where " + where;

				Log.e(TAG, " strUpdate : " + strUpdate);

				db.execSQL(strUpdate);

				getContext().getContentResolver().notifyChange(uri, null);

				return 0;
			}
		}

		return -1;
    }
    
    @Override
    public String getType(Uri uri) {
    	return null;
    }
    
    static {
    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "package_permission", PACKAGE_PERMISSION);
		sUriMatcher.addURI(AUTHORITY, "package", PACKAGE_NAME);
		sUriMatcher.addURI(AUTHORITY, "permission", PERMISSION_NAME);
    }
}
