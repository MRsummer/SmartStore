# SmartStore
SmartStore是一个android的数据库帮助库，几乎不用编数据库操作代码就可以实现数据库到java对象的映射
使用SmartStore的方法很简单，比如现在需要创建并使用一个User的表，只需简单三步：<br>

1. 编写User对象，并在User对象中标记出 数据库字段 和 主键字段<br> 
<code>
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
</code> 

2. 在应用初始化的时候，设置数据库的代理<br>
<code>
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
</code> 

3. 在需要的地反使用数据库<br>
<code>
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
</code>