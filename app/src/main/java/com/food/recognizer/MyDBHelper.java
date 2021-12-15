package com.food.recognizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyDBHelper extends SQLiteOpenHelper {

    private Context context;

    private static final String TAG = "Food Recognizer :: " + MyDBHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "foodrecognizer.db";

    private static final String TABLE_NAME_MAIN = "user";
    private static final String TABLE_MAIN_COLUMN_ID = "uid";
    private static final String TABLE_MAIN_COLUMN_NAME = "name";
    private static final String TABLE_MAIN_COLUMN_USER_IMAGE = "image";
    private static final String TABLE_MAIN_COLUMN_AGE = "age";
    private static final String TABLE_MAIN_COLUMN_GENDER = "gender";
    private static final String TABLE_MAIN_COLUMN_HEIGHT = "height";
    private static final String TABLE_MAIN_COLUMN_WEIGHT = "weight";
    private static final String TABLE_MAIN_COLUMN_GOAL = "goal";

    private static final String TABLE_NAME_SECONDARY = "records";
    private static final String TABLE_SECONDARY_COLUMN_ID = "uid";
    private static final String TABLE_SECONDARY_COLUMN_DATE = "date";
    private static final String TABLE_SECONDARY_COLUMN_FOOD = "food";
    private static final String TABLE_SECONDARY_COLUMN_FOOD_IMAGE = "image";
    private static final String TABLE_SECONDARY_COLUMN_CALORIE = "calorie";
    private static final String TABLE_SECONDARY_COLUMN_CARBS = "carbs";
    private static final String TABLE_SECONDARY_COLUMN_PROTEIN = "protein";



    public MyDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME_MAIN +
                " (" + TABLE_MAIN_COLUMN_ID + " TEXT PRIMARY KEY, " +
                TABLE_MAIN_COLUMN_NAME + " TEXT, " +
                TABLE_MAIN_COLUMN_USER_IMAGE + " BLOB, " +
                TABLE_MAIN_COLUMN_AGE + " INTEGER, " +
                TABLE_MAIN_COLUMN_GENDER + " TEXT, " +
                TABLE_MAIN_COLUMN_HEIGHT + " INTEGER, " +
                TABLE_MAIN_COLUMN_WEIGHT + " INTEGER, " +
                TABLE_MAIN_COLUMN_GOAL + " INTEGER);";
        db.execSQL(query);

        String query1 = "CREATE TABLE " + TABLE_NAME_SECONDARY +
                " (" + TABLE_SECONDARY_COLUMN_ID + " TEXT, " +
                TABLE_SECONDARY_COLUMN_DATE + " TEXT, " +
                TABLE_SECONDARY_COLUMN_FOOD + " TEXT, " +
                TABLE_SECONDARY_COLUMN_FOOD_IMAGE + " BLOB, " +
                TABLE_SECONDARY_COLUMN_CALORIE + " INTEGER, " +
                TABLE_SECONDARY_COLUMN_CARBS + " INTEGER, " +
                TABLE_SECONDARY_COLUMN_PROTEIN + " INTEGER, " +
                " FOREIGN KEY ("+TABLE_SECONDARY_COLUMN_ID+") REFERENCES "+TABLE_NAME_MAIN+"("+TABLE_MAIN_COLUMN_ID+"));";
        db.execSQL(query1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SECONDARY);
        onCreate(db);
    }

    public boolean checkIfUserExists(String uid) {

        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT UID FROM " + TABLE_NAME_MAIN + " WHERE UID='" + uid + "';";

        if(db != null){
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return false;
        }

        if(cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }

    }

    public void addUserRecord(String uid, String name, byte[] image, int age, String gender, int height, int weight, int goal) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(TABLE_MAIN_COLUMN_ID, uid);
        cv.put(TABLE_MAIN_COLUMN_NAME, name);
        cv.put(TABLE_MAIN_COLUMN_USER_IMAGE, image);
        cv.put(TABLE_MAIN_COLUMN_AGE, age);
        cv.put(TABLE_MAIN_COLUMN_GENDER, gender);
        cv.put(TABLE_MAIN_COLUMN_HEIGHT, height);
        cv.put(TABLE_MAIN_COLUMN_WEIGHT, weight);
        cv.put(TABLE_MAIN_COLUMN_GOAL, goal);

        long result = db.insert(TABLE_NAME_MAIN, null, cv);
        if(result == -1) {
            Log.e(TAG, "Error while inserting record");
        } else {
            Log.d(TAG, "Record added successfully");
        }
    }

    public void addMealRecord(String uid, String date, String food, byte[] img,int calorie, int carb, int protein) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(TABLE_SECONDARY_COLUMN_ID, uid);
        cv.put(TABLE_SECONDARY_COLUMN_DATE, date);
        cv.put(TABLE_SECONDARY_COLUMN_FOOD, food);
        cv.put(TABLE_SECONDARY_COLUMN_FOOD_IMAGE, img);
        cv.put(TABLE_SECONDARY_COLUMN_CALORIE, calorie);
        cv.put(TABLE_SECONDARY_COLUMN_CARBS, carb);
        cv.put(TABLE_SECONDARY_COLUMN_PROTEIN, protein);

        long result = db.insert(TABLE_NAME_SECONDARY, null, cv);
        if(result == -1) {
            Log.e(TAG, "Error while inserting record");
        } else {
            Log.d(TAG, "Record added successfully");
        }
    }

    public ArrayList<FoodDataModel> getMealRecord(String uid, String date) {
        ArrayList<FoodDataModel> foodDataModels = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME_SECONDARY + " WHERE UID='" + uid + "' AND DATE='"+ date +"';";

        if(db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return null;
        }

        if((cursor.getCount() > 0) && cursor.moveToFirst()) {

            do {
                String name = cursor.getString(cursor.getColumnIndex(TABLE_SECONDARY_COLUMN_FOOD));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(TABLE_SECONDARY_COLUMN_FOOD_IMAGE));
                int calorie = cursor.getInt(cursor.getColumnIndex(TABLE_SECONDARY_COLUMN_CALORIE));
                int carbs = cursor.getInt(cursor.getColumnIndex(TABLE_SECONDARY_COLUMN_CARBS));
                int proteins = cursor.getInt(cursor.getColumnIndex(TABLE_SECONDARY_COLUMN_PROTEIN));
                foodDataModels.add(new FoodDataModel(name, image, calorie, carbs, proteins));
            }while (cursor.moveToNext());

            cursor.close();
        } else {
            Log.d(TAG, "No records");
        }

        return foodDataModels;
    }

    public String getUserName(String uid) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT "+TABLE_MAIN_COLUMN_NAME+" FROM " + TABLE_NAME_MAIN + " WHERE UID='" + uid + "';";

        if(db != null){
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return null;
        }

        if(cursor.getCount() == 1) {

            if(cursor != null && cursor.moveToFirst()) {

                String uname = cursor.getString(0);
                cursor.close();
                return uname;
            } else {
                return null;
            }

        } else {
            cursor.close();
            return null;
        }
    }

    public byte[] getUserImage(String uid) {
        byte[] uimage = null;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT "+TABLE_MAIN_COLUMN_USER_IMAGE+" FROM " + TABLE_NAME_MAIN + " WHERE UID='" + uid + "';";

        if(db != null){
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return null;
        }

        if(cursor.getCount() == 1) {

            if(cursor != null && cursor.moveToFirst()) {

                uimage = cursor.getBlob(0);
                cursor.close();
                return uimage;
            } else {
                return null;
            }

        } else {
            cursor.close();
            return null;
        }
    }

    public int getUserAge(String uid) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT "+TABLE_MAIN_COLUMN_AGE+" FROM " + TABLE_NAME_MAIN + " WHERE UID='" + uid + "';";

        if(db != null){
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return -1;
        }

        if(cursor.getCount() == 1) {

            if(cursor!=null && cursor.moveToFirst()) {
                int uage = cursor.getInt(0);
                cursor.close();
                return uage;
            }

            return -1;

        } else {
            cursor.close();
            return -1;
        }
    }

    public String getUserGender(String uid) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT "+TABLE_MAIN_COLUMN_GENDER+" FROM " + TABLE_NAME_MAIN + " WHERE UID='" + uid + "';";

        if(db != null){
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return null;
        }

        if(cursor.getCount() == 1) {

            if(cursor != null && cursor.moveToFirst()) {

                String ugender = cursor.getString(0);
                cursor.close();
                return ugender;
            } else {
                return null;
            }

        } else {
            cursor.close();
            return null;
        }
    }

    public int getUserHeight(String uid) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT "+TABLE_MAIN_COLUMN_HEIGHT+" FROM " + TABLE_NAME_MAIN + " WHERE UID='" + uid + "';";

        if(db != null){
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return -1;
        }

        if(cursor.getCount() == 1) {

            if(cursor!=null && cursor.moveToFirst()) {
                int uheight = cursor.getInt(0);
                cursor.close();
                return uheight;
            }

            return -1;

        } else {
            cursor.close();
            return -1;
        }
    }

    public int getUserWeight(String uid) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT "+TABLE_MAIN_COLUMN_WEIGHT+" FROM " + TABLE_NAME_MAIN + " WHERE UID='" + uid + "';";

        if(db != null){
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return -1;
        }

        if(cursor.getCount() == 1) {

            if(cursor!=null && cursor.moveToFirst()) {
                int uweight = cursor.getInt(0);
                cursor.close();
                return uweight;
            }

            return -1;

        } else {
            cursor.close();
            return -1;
        }
    }

    public int getUserGoal(String uid) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT GOAL FROM " + TABLE_NAME_MAIN + " WHERE UID='" + uid + "';";

        if(db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return -1;
        }

        if(cursor.getCount() == 1) {

            if(cursor!=null && cursor.moveToFirst()) {
                int ugoal = Integer.parseInt(cursor.getString(0));
                cursor.close();
                return ugoal;
            } else {
                return -1;
            }

        } else {
            cursor.close();
            return -1;
        }

    }

    public int getCompletedGoal(String uid, String date) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM("+TABLE_SECONDARY_COLUMN_CALORIE+") as TOTAL FROM '"+TABLE_NAME_SECONDARY+"' WHERE UID='"+uid+"' AND DATE='"+date+"';";

        if(db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return -1;
        }

        if(cursor.getCount() > 0) {

            if(cursor != null && cursor.moveToFirst()) {
                int completedGoal = cursor.getInt(cursor.getColumnIndex("TOTAL"));
                cursor.close();
                return completedGoal;
            } else {
                return -1;
            }

        } else {
            cursor.close();
            return -1;
        }
    }

    public int getDailyCalorie(String uid, String date) {
        int calorie = getCompletedGoal(uid, date);

        return calorie;
    }

    public int getDailyCarbs(String uid, String date){
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM("+TABLE_SECONDARY_COLUMN_CARBS+") as TOTAL FROM '"+TABLE_NAME_SECONDARY+"' WHERE UID='"+uid+"' AND DATE='"+date+"';";

        if(db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return -1;
        }

        if(cursor.getCount() > 0) {

            if(cursor != null && cursor.moveToFirst()) {
                int carbs = cursor.getInt(cursor.getColumnIndex("TOTAL"));
                cursor.close();
                return carbs;
            } else {
                return -1;
            }

        } else {
            cursor.close();
            return -1;
        }
    }

    public int getDailyProtein(String uid, String date){
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM("+TABLE_SECONDARY_COLUMN_PROTEIN+") as TOTAL FROM '"+TABLE_NAME_SECONDARY+"' WHERE UID='"+uid+"' AND DATE='"+date+"';";

        if(db != null) {
            cursor = db.rawQuery(query, null);
        } else {
            Log.e(TAG, "Error while accessing database");
            return -1;
        }

        if(cursor.getCount() > 0) {

            if(cursor != null && cursor.moveToFirst()) {
                int protein = cursor.getInt(cursor.getColumnIndex("TOTAL"));
                cursor.close();
                return protein;
            } else {
                return -1;
            }

        } else {
            cursor.close();
            return -1;
        }
    }


}
