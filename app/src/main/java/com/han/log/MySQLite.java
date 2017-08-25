package com.han.log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

class MySQLite extends SQLiteOpenHelper {
  private Context mContext;

  public static final String DB_NAME = "diaplay_time";
  public static final String KEY_logOutput = "log_output";
  public static final String KEY_timestamp = "log_timestamp";

  MySQLite(Context context) {
    super(context, DB_NAME + ".db", null, 1);
    this.mContext = context;
  }

  @Override public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL("create table "
        + DB_NAME
        + "(_id integer primary key autoincrement, "
        + KEY_logOutput
        + " varchar, "
        + KEY_timestamp
        + " varchar)");
  }

  @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    Toast.makeText(mContext, "onUpgrade", Toast.LENGTH_SHORT).show();
  }

  public static void drop(Context context) {
    context.deleteDatabase(DB_NAME + ".db");
  }
}
