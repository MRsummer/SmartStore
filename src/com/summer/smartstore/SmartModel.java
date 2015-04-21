package com.summer.smartstore;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuguangwen on 15/4/21.
 * email 979343670@qq.com
 */
public abstract class SmartModel{

    private static List<Field> dbFieldList;

    public String getTableName() {
        return this.getClass().getSimpleName();
    }

    public String getCreateTableSql(){
        getDBFields();
        StringBuilder fieldSB = new StringBuilder();
        for(int i = 0;i < dbFieldList.size();i ++){
            Field field = dbFieldList.get(i);
            boolean isPrimaryKey = false;
            Annotation[] annotations = field.getAnnotations();
            for(Annotation a : annotations){
                if(a instanceof DBPrimaryKey){
                    isPrimaryKey = true;
                    break;
                }
            }

            String type = "";
            Class fieldType = field.getType();
            if(fieldType == int.class || fieldType == Integer.class || fieldType == long.class || fieldType == Long.class){
                type = "integer";
            }else if(fieldType == String.class){
                type = "varchar(12)";
            }else{
                throw new RuntimeException("db field type can only be int, long, String");
            }

            fieldSB.append(field.getName());
            fieldSB.append(" ");
            fieldSB.append(type);
            if(isPrimaryKey){
                fieldSB.append(" primary key");
            }
            if(i != dbFieldList.size() - 1){
                fieldSB.append(", ");
            }
        }
        String tableName = this.getClass().getSimpleName();
        return "create table if not exists "+tableName+" ( " + fieldSB.toString() + " )";
    }

    public String getPrimaryKey() {
        getDBFields();
        for(Field field : dbFieldList){
            Annotation[] annotations = field.getAnnotations();
            for(Annotation a : annotations){
                if(a instanceof DBPrimaryKey){
                    return field.getName();
                }
            }
        }
        return null;
    }

    public String getPrimaryKeyName() {
        getDBFields();
        for(Field field : dbFieldList){
            Annotation[] annotations = field.getAnnotations();
            for(Annotation a : annotations){
                if(a instanceof DBPrimaryKey){
                    return getStringValueFromField(field, this);
                }
            }
        }
        return null;
    }

    public SmartModel fromCursor(Cursor cursor){
        getDBFields();
        try {
            if(cursor.moveToNext()){
                Class thisClass = this.getClass();
                Object model = thisClass.newInstance();
                for(Field f : dbFieldList){
                    setValueFromCursor(f, model, cursor);
                }
                return (SmartModel) model;
            }
        }catch (Exception e){
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    public ContentValues toContentValues(){
        getDBFields();
        try {
            ContentValues values = new ContentValues();
            for (Field f : dbFieldList) {
                values.put(f.getName(), getStringValueFromField(f, this));
            }
            return values;
        }catch (Exception e){
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private void getDBFields(){
        if(dbFieldList == null){
            dbFieldList = new ArrayList<Field>();
            Field[] fields = this.getClass().getDeclaredFields();
            for(Field field : fields){
                Annotation[] annotations = field.getAnnotations();
                for(Annotation a : annotations){
                    if(a instanceof DBField){
                        dbFieldList.add(field);
                        break;
                    }
                }
            }
            if(dbFieldList.size() == 0){
                throw new RuntimeException("no db field, at least one db field");
            }
        }
    }

    private void setValueFromCursor(Field f, Object obj, Cursor cursor){
        boolean accessible = f.isAccessible();
        if(!accessible) f.setAccessible(true);
        try {
            Class c = f.getType();
            if(c == int.class || c == Integer.class){
                f.set(obj, cursor.getInt( cursor.getColumnIndex(f.getName()) ));

            }else if(c == long.class || c == Long.class){
                f.set(obj, cursor.getLong( cursor.getColumnIndex(f.getName()) ));

            }else if(c == String.class){
                f.set(obj, cursor.getString( cursor.getColumnIndex(f.getName()) ));

            }else{
                throw new RuntimeException("db field type can only be int, long, String");
            }
        }catch (Exception e){
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }finally {
            f.setAccessible(accessible);
        }
    }

    private String getStringValueFromField(Field f, Object obj){
        boolean accessible = f.isAccessible();
        if(!accessible) f.setAccessible(true);
        try {
            Class c = f.getType();
            if(c == int.class || c == Integer.class){
                return f.getInt(obj) + "";

            }else if(c == long.class || c == Long.class){
                return f.getLong(obj) + "";

            }else if(c == String.class){
                return (String) f.get(obj);

            }else{
                throw new RuntimeException("db field type can only be int, long, String");
            }
        }catch (Exception e){
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            return null;
        }finally {
            f.setAccessible(accessible);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface DBField{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface DBPrimaryKey{}

}