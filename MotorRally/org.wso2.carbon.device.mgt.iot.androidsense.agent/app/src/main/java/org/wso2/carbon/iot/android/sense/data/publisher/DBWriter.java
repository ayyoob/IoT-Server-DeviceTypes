package org.wso2.carbon.iot.android.sense.data.publisher;

import java.util.List;
import java.util.Vector;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.data.publisher.Attributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class DBWriter extends SQLiteOpenHelper{

    private static final String DB_NAME = "senseData.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase senseDb;

    public DBWriter(Context context) {
        super(context,  DB_NAME,null, DATABASE_VERSION);
        senseDb = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //executeSQLScript(database, "db.sql");
        String query="";
        query=createTableFromClass("org.wso2.carbon.iot.android.sense.event.streams.Location.LocationData");
        database.execSQL(query);
    }

    public String createTableFromClass(String className) {

        Field[] fields = null;

        StringBuilder queryBuilder = new StringBuilder();
        String primaryKey=", PRIMARY KEY  (";
        try {
            Class<?> clazz = Class.forName(className);
            fields = clazz.getDeclaredFields();
            String name = clazz.getSimpleName().replace("Data", "");

            queryBuilder.append("CREATE TABLE " + name + " (");
            boolean firstField = true;
            for (Field field : fields) {
                if (!firstField) {
                    queryBuilder.append(", ");
                }

                queryBuilder.append(field.getName() + " ");

                if (String.class.isAssignableFrom(field.getType())) {
                    queryBuilder.append("TEXT");
                }

                if (field.getType() == Integer.TYPE) {
                    queryBuilder.append("INTEGER");
                }

                if ((field.getType() == Float.TYPE )||( field.getType() == Double.TYPE)) {
                    queryBuilder.append("REAL");
                }

                Annotation annotation = field.getAnnotation(Attributes.class);
                if (annotation != null) {
                    if (annotation instanceof Attributes) {
                        Attributes attr = (Attributes) annotation;
                        if (attr.primaryKey())
                            //queryBuilder.append(" PRIMARY KEY");
                            primaryKey=primaryKey+" "+field.getName()+ ",";
                    }
                }
                firstField = false;
            }
            primaryKey=primaryKey.substring(0, primaryKey.length()-1);
            primaryKey=primaryKey+"));";
            queryBuilder.append(primaryKey);

            String query = queryBuilder.toString();
            return query;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        onCreate(database);
    }

    public void insertData(List queryValues){
        if(queryValues.size()<=0) {
            return;
        }
        //SenseLog.i(" testing "+ queryValues.get(0).getClass().getSimpleName());
        Class<?> cls=queryValues.get(0).getClass();
        String className=queryValues.get(0).getClass().getSimpleName().replace("Data", "");

        Field[] fields = cls.getDeclaredFields();
        for(int i=0; i <queryValues.size(); i++){

            ContentValues values = new ContentValues();
            for(int j=0; j < fields.length ; j++){
                Field field=fields[j];
                field.setAccessible(true);
                try {
                    if (String.class.isAssignableFrom(field.getType())) {
                        values.put(fields[j].getName(),field.get(queryValues.get(i)).toString());
                    }else if (field.getType() == Integer.TYPE) {
                        values.put(fields[j].getName(),field.getInt(queryValues.get(i)));
                    }else if ((field.getType() == Float.TYPE )||( field.getType() == Double.TYPE)) {
                        values.put(fields[j].getName(),field.getDouble(queryValues.get(i)));
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("DBWriter","error");
                } catch (IllegalAccessException e) {
                    Log.e("DBWriter", "error");
                }
            }
            senseDb.insert(className, null, values);
        }
        senseDb.close();
        Log.i("DBWriter", className + " data inserted");
    }
}