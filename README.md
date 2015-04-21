# SmartStore
SmartStore是一个android的数据库帮助库，几乎不用编数据库操作代码就可以实现数据库到java对象的映射，使用SmartStore的方法很简单，比如现在需要创建一个User的表并使用User对象与之关联，只需简单三步：<br>

1. 编写User对象，并在User对象中标记出 数据库字段 和 主键字段（注意：变量的名称和数据库中的字段名称一样）<br> 
<pre>
public class User extends SmartModel {

	    @DBPrimaryKey
	    @DBField
	    public int user_id;
	
	    @DBField
	    public String user_name;
	
	    @DBField
	    public int user_age;
	
	    //not db field
	    public int user_sex;
}
</pre> 

2. 在应用初始化的时候，设置数据库的代理<br>
<pre>
WPStore.setWPStoreDelegate(new WPStoreDelegate() {
			@Override
			public void onCreateTables(SQLiteDatabase db) {
				//需要创建的表（getCreateTableSql在SmartModel中实现）
				db.execSQL(new User().getCreateTableSql());
			}
			
			@Override
			public String getTableName() {
				//数据库的名称
				return "test.db";
			}
			
			@Override
			public Context getContext() {
				//应用程序的Context
				return getApplicationContext();
			}
		});
</pre> 

3. 在需要的地反使用数据库<br>
<pre>
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
	
	StringBuilder ret = new StringBuilder();
	String sql = "select * from " + user.getTableName();
	List<SmartModel> modelList = WPStore.getInstance().fetchListSync(user, sql);
	for(SmartModel model : modelList){
	    User u = (User) model;
	    ret.append("id -->"+u.user_id+"  name-->"+u.user_name+"  age-->"+u.user_age+"  sex-->"+u.user_sex+"\n");
	}
	Log.d("test", ret.toString());
</pre>

#数据库操作Api
数据库操作Api都封装到WPStore类中，包括
* removeSync(SmartModel) //同步删除
* removeAsync(SmartModel, Callback)  //异步删除
* saveSync(SmartModel)  //同步存储
* saveAsync(SmartModel)  //异步存储
* saveListByTransaction(List<SmartModel>)  //同步存储列表
* saveListAsyncByTransaction(List<SmartModel>, Callback)  //异步存储列表
* fetchListSync(SmartModel, sql)  //同步获取列表
* fetchListAsync(SmartModel, sql, Callback)   //异步获取列表
* execSql(sql)  //同步执行sql
* execSqlAsync(sql)  //异步执行sql

#关于 smart store 的原理 和 注意事项
* smart store 自动通过在类中标记数据库的字段，主键字段，能够自动生成数据库的创建语句（以类名为表明，字段名为数据库字段名称）
* smart store 通过主键进行存储和删除，所有如果需要调用save 方法 或者remove方法， 则需要加入主键标记， 否则会报错
* smart store 写的比较简单， 只支持 int long String 三种类型的数据， 但作者认为这三种在sqlite中也够用了， 所以需要注意java类中的字段类型

