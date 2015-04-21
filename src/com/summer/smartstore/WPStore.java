package com.summer.smartstore;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WPStore {

	public static final String TAG = WPStore.class.getSimpleName();
	private static WPStore sInstance;
	
	private static WPStoreDelegate sDelegate;
	private SQLiteDatabase db;
	private ExecutorService dbExecutorService = Executors.newSingleThreadExecutor();
	private Handler handler = new Handler(Looper.getMainLooper());

	public static WPStore getInstance() {
		if(sDelegate == null){
			throw new RuntimeException("a WPStoreDelegate must be set before getInstance");
		}
		if (sInstance == null) {
			synchronized (WPStore.class){
				if(sInstance == null){
					sInstance = new WPStore();
				}
			}
		}
		return sInstance;
	}
	
	public static void setWPStoreDelegate(WPStoreDelegate delegate){
		sDelegate = delegate;
	}
	
	private WPStore(){
		String dbName = sDelegate.getTableName();
		Context context = sDelegate.getContext();
		File dbFile = context.getDatabasePath(dbName);
		try {
			if(!dbFile.getParentFile().exists()){
				dbFile.getParentFile().mkdirs();
			}
			if(!dbFile.exists()){
				dbFile.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
		sDelegate.onCreateTables(db);
	}

	public void removeSync(SmartModel model){
		if(model == null) return;
		db.delete(model.getTableName(), model.getPrimaryKeyName() + "=?", new String[]{model.getPrimaryKey()});
	}

	public void removeAsync(final SmartModel model, final SmartModelCallback cb) {
		dbExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				removeSync(model);
				invokeCallback(model, null, cb);
			}
		});
	}

	public void saveSync(SmartModel model) {
		if (model == null) return;
		db.insertWithOnConflict(model.getTableName(), null, model.toContentValues(), SQLiteDatabase.CONFLICT_REPLACE);
	}

	public void saveAsync(final SmartModel model, final SmartModelCallback cb) {
		dbExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				saveSync(model);
				invokeCallback(model, null, cb);
			}
		});
	}

	public void saveListByTransaction(List<SmartModel> models){
		if (models == null) return;
		db.beginTransaction();
		for (int i = 0; i < models.size(); i++) {
			SmartModel model = models.get(i);
			db.insertWithOnConflict(model.getTableName(), null, model.toContentValues(), SQLiteDatabase.CONFLICT_REPLACE);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

    public void saveListAsyncByTransaction(final List<SmartModel> models, final SmartModelResultCallback cb) {
        dbExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				saveListByTransaction(models);
				invokeCallback(cb, true);
			}
		});
    }

	public List<SmartModel> fetchListSync(SmartModel model, String sql) {
		if(model == null) return new ArrayList<SmartModel>();
		List<SmartModel> models = new ArrayList<SmartModel>();
		Cursor cursor = db.rawQuery(sql, null);
		while(!cursor.isAfterLast()) {
			SmartModel SmartModel = model.fromCursor(cursor);
			if(SmartModel != null) models.add(SmartModel);
		}
		cursor.close();
		return models;
	}

	public void fetchListAsync(final SmartModel model, final String sql, final SmartModelListCallback cb) {
		dbExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				List<SmartModel> models = fetchListSync(model, sql);
				invokeCallback(models, null, cb);
			}
		});
	}

	public void execSql(String sql){
		db.execSQL(sql);
	}

	public void execSqlAsync(final String sql){
		dbExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				execSql(sql);
			}
		});
	}

	private void invokeCallback(final SmartModelResultCallback cb, final boolean success){
		if(cb == null) return;
		handler.post(new Runnable() {
			@Override
			public void run() {
				cb.onResult(success);
			}
		});
	}

	private void invokeCallback(final List<SmartModel> modelList, final Exception e, final SmartModelListCallback cb) {
		if(cb == null) return;
		handler.post(new Runnable() {
			@Override
			public void run() {
				cb.onResult(modelList, e);
			}
		});
	}

	private void invokeCallback(final SmartModel model, final Exception e, final SmartModelCallback cb) {
		if(cb == null) return;
		handler.post(new Runnable() {
			@Override
			public void run() {
				cb.onResult(model, e);
			}
		});
	}

    public interface SmartModelListCallback {
        public void onResult(List<SmartModel> model, Exception e);
    }

    public interface SmartModelCallback {
        public void onResult(SmartModel model, Exception e);
    }

    public interface SmartModelResultCallback{
        public void onResult(boolean success);
    }

	public static void clear(){
		sInstance = null;
	}
	
}
