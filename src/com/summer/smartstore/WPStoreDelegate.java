package com.summer.smartstore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public interface WPStoreDelegate {
	
	public Context getContext();
	
	//这里可以判断登录用户，不用的用户返回不同的数据库名字
	public String getDBName();
	
	//这里可以做一些创建数据库和升级的操作
	public void onCreateTables(SQLiteDatabase db);
	
	//使用了新的数据库，旧的数据库不再使用
	public void onDBDestroy(String oldDBPath); 
}
