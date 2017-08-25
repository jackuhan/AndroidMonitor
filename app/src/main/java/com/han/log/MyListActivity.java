package com.han.log;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import com.han.devtool.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyListActivity extends AppCompatActivity implements View.OnClickListener {

  private List<Map<String, String>> mStringList = new ArrayList<>();
  private ListView mListView;
  private SimpleCursorAdapter mSimpleCursorAdapter;
  private SQLiteDatabase mDbReader;
  private MySQLite mMySQLite;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_listview);

    mListView = (ListView) findViewById(R.id.myListview);
    initEvent();
  }

  private void initEvent() {

    mMySQLite = new MySQLite(this);
    mDbReader = mMySQLite.getReadableDatabase();

    mSimpleCursorAdapter = new SimpleCursorAdapter(MyListActivity.this, R.layout.listview_sql_item, null,
        new String[] { MySQLite.KEY_logOutput, MySQLite.KEY_timestamp }, new int[] { R.id.logoutput, R.id.timestamp },
        CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

    mListView.setAdapter(mSimpleCursorAdapter);

    refresh();
  }

  private void refresh() {
    Cursor mCursor = mDbReader.query(MySQLite.DB_NAME, null, null, null, null, null, "_id desc");
    mSimpleCursorAdapter.changeCursor(mCursor);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.clear: {
        MySQLite.drop(this);
        refresh();
        break;
      }
      case R.id.refresh:{
        refresh();
        break;
      }
    }
  }
}
