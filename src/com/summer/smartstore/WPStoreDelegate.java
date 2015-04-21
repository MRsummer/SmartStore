package com.summer.smartstore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public interface WPStoreDelegate {

	public abstract String getTableName();
	
	public abstract Context getContext();
	
	public abstract void onCreateTables(SQLiteDatabase db);
	
}
