package com.summer.wpstore;

import com.summer.smartstore.SmartModel;

/**
 * Created by zhuguangwen on 15/4/21.
 * email 979343670@qq.com
 */
public class User extends SmartModel {

    @DBPrimaryKey
    @DBField
    public int user_id;

    @DBField
    public String user_name;

    @DBField
    public int user_age;

    //non db field
    public int user_sex;

}
