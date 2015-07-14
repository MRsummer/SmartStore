package com.summer.wpstore;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.summer.smartstore.SmartModel;
import com.summer.smartstore.WPStore;
import com.summer.smartstore.WPStoreDelegate;

public class MainActivity extends Activity{

	private int currentLoginUid = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		WPStore.setWPStoreDelegate(new WPStoreDelegate() {
			@Override
			public void onCreateTables(SQLiteDatabase db) {
				//这里可以做一些创建数据库和升级的操作
				db.execSQL(new User().getCreateTableSql());
			}
			
			@Override
			public String getDBName() {
				//这里可以判断登录用户，不用的用户返回不同的数据库名字
				return "test" + currentLoginUid + ".db";
			}
			
			@Override
			public Context getContext() {
				return getApplicationContext();
			}

			@Override
			public void onDBDestroy(String oldDBPath) {
				Log.d("MainActivity", "onDBDestroy");
			}			
		});
		
		TextView content = (TextView) findViewById(R.id.content);
		content.setText(getTestString());
	}

	public static String getTestString(){
        User user = new User();
        user.user_id = 1001;
        user.user_name = "Summer";
        user.user_age = 10;
        user.user_sex = 1;
        WPStore.getInstance().saveSync(user);

        User user1 = new User();
        user1.user_id = 1002;
        user1.user_name = "Hehe";
        user1.user_age = 11;
        user.user_sex = 2;
        WPStore.getInstance().saveSync(user1);

        User user2 = new User();
        user2.user_id = 1003;
        user2.user_name = "123456";
        user2.user_age = 12;
        user.user_sex = 3;
        WPStore.getInstance().saveSync(user2);

        StringBuilder ret = new StringBuilder();
        String sql = "select * from " + user.getTableName();
        List<SmartModel> modelList = WPStore.getInstance().fetchListSync(user, sql);
        for(SmartModel model : modelList){
            User u = (User) model;
            ret.append("id -->"+u.user_id+"  name-->"+u.user_name+"  age-->"+u.user_age+"  sex-->"+u.user_sex+"\n");
        }
        return ret.toString();
    }
	
}
