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


    private static final String strTable_CDMA = "sourceid_unused_cdma";
    private static final String strTable_GSM = "sourceid_unused_gsm";


    
    private static final int SOURCEID_CDMA 				= 1;
    private static final int SOURCEID_GSM 				= 2;
    private static final int SOURCEID_CDMA_INDEX 		= 3;
    private static final int SOURCEID_GSM_INDEX 		= 4;
    
    private static final UriMatcher sUriMatcher;
    private static final String AUTHORITY = "com.lw.permissionsmanager.provider";
    private static final String AUTHORITY2 = "com.txtw.provider.scan.question";
	
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
	
/*	@Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		
		switch (sUriMatcher.match(uri)) {
			case SOURCEID_CDMA:
			{
				String strSelect = "select sourceid from " + strTable_CDMA + " where type = 'CDMA'";
				
				Cursor cur = db.rawQuery(strSelect, null);
				cur.setNotificationUri(getContext().getContentResolver(), uri);
				
				return cur;
			}
			
			case SOURCEID_GSM:
			{
				String strSelect = "select sourceid from " + strTable_GSM + " where type = 'GSM'";
				
				Cursor cur = db.rawQuery(strSelect, null);
				cur.setNotificationUri(getContext().getContentResolver(), uri);
				
				return cur;
			}
		}
		
		return null;
    }
*/	
	
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
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	
    	final String strDelete;    	
    	switch (sUriMatcher.match(uri)) {
	    	case SOURCEID_CDMA:
	    	{
	    		strDelete = "delete from " + strTable_CDMA;
	    		try 
    	        {
	    			db.execSQL(strDelete);
    	        } catch (SQLException e) 
    	        {
    	        }
	    	}
	    	break;
	    	
	    	case SOURCEID_GSM:
	    	{
	    		strDelete = "delete from " + strTable_GSM;
	    		try 
    	        {
	    			db.execSQL(strDelete);
    	        } catch (SQLException e) 
    	        {
    	        }
	    	}
	    	break;
	    	
	    	case SOURCEID_CDMA_INDEX:
	    	{
	    		final String strContactID = uri.getPathSegments().get(1);
	    		
	    		strDelete = "delete from " + strTable_CDMA + " where type = 'CDMA' and sourceid = " + strContactID;
	    		try 
    	        {
	    			db.execSQL(strDelete);
    	        } catch (SQLException e) 
    	        {
    	        }
	    	}
	    	break;
	    	
	    	case SOURCEID_GSM_INDEX:
	    	{
	    		final String strContactID = uri.getPathSegments().get(1);
	    		
	    		strDelete = "delete from " + strTable_GSM + " where type = 'GSM' and sourceid = " + strContactID;
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
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
    	
    	final String strUpdate;
    	
    	strUpdate = "update lw_Tools set mark = '" + values.getAsString("mark") + "' where " + where;
    	
    	Log.e(TAG, " strUpdate : " + strUpdate);
    	
		try 
        {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			db.execSQL(strUpdate);
        } catch (SQLException e) 
        {
        }
		
		getContext().getContentResolver().notifyChange(uri, null);
    	
    	return 0;
    }
    
    @Override
    public String getType(Uri uri) {
    	return null;
    }
    
    static {
    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "package_permission", PACKAGE_PERMISSION);

        sUriMatcher.addURI(AUTHORITY, "sourceid_cdma", SOURCEID_CDMA);
        sUriMatcher.addURI(AUTHORITY, "sourceid_gsm", SOURCEID_GSM);
        sUriMatcher.addURI(AUTHORITY, "sourceid_cdma/#", SOURCEID_CDMA_INDEX);
        sUriMatcher.addURI(AUTHORITY, "sourceid_gsm/#", SOURCEID_GSM_INDEX);
        sUriMatcher.addURI(AUTHORITY2, "sourceid_gsm/#", SOURCEID_GSM_INDEX);
    }
}
