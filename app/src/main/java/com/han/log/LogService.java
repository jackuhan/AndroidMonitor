package com.han.log;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Filter;
import com.han.devtool.R;
import com.nolanlawson.logcat.data.LogLine;
import com.nolanlawson.logcat.data.SearchCriteria;
import com.nolanlawson.logcat.helper.PreferenceHelper;
import com.nolanlawson.logcat.reader.LogcatReader;
import com.nolanlawson.logcat.reader.LogcatReaderLoader;
import com.nolanlawson.logcat.util.ArrayUtil;
import com.nolanlawson.logcat.util.LogLineAdapterUtil;
import de.greenrobot.event.EventBus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LogService extends Service {
  public static final String TAG = "LogService";
  public static final String COMMAND = "COMMAND";
  public static final String COMMAND_OPEN = "COMMAND_OPEN";
  public static final String COMMAND_CLOSE = "COMMAND_CLOSE";
  private WindowManager mWindowManager;

  private SimpleCursorAdapter mSimpleCursorAdapter;
  private SQLiteDatabase mDbWriter;
  private SQLiteDatabase mDbReader;
  private MySQLite mMySQLite;

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");

    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    mMySQLite = new MySQLite(this);
    mDbWriter = mMySQLite.getWritableDatabase();
    mDbReader = mMySQLite.getReadableDatabase();

    mSimpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.listview_sql_item, null,
        new String[] { "songname", "singer" }, new int[] { R.id.logoutput, R.id.timestamp },
        CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

    String command = intent.getStringExtra(COMMAND);
    if (command != null) {
      if (command.equals(COMMAND_OPEN)) {
        openFPS();
      } else if (command.equals(COMMAND_CLOSE)) {
        closeFPS();
      }
    }

    return super.onStartCommand(intent, flags, startId);
  }

  public void insertData(String logContent,String timestamp) {
    ContentValues mContentValues = new ContentValues();
    mContentValues.put(MySQLite.KEY_logOutput, logContent);
    mContentValues.put(MySQLite.KEY_timestamp, timestamp);
    mDbWriter.insert(MySQLite.DB_NAME, null, mContentValues);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  public static class LogdEvent {
    public LogLine mLogLine;

    public LogdEvent(LogLine logLine) {
      mLogLine = logLine;
    }
  }

  private ActivityTimingFloatingView mFloatingView;
  private static final WindowManager.LayoutParams LAYOUT_PARAMS;

  static {
    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    params.x = 0;
    params.y = 0;
    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
    params.gravity = Gravity.CENTER | Gravity.TOP;
    params.type = WindowManager.LayoutParams.TYPE_PHONE;
    params.format = PixelFormat.RGBA_8888;
    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

    LAYOUT_PARAMS = params;
  }

  public void openFPS() {
    //logLineList = new ArrayList<LogLine>();
    startUpMainLog();

    if (mFloatingView == null) {
      mFloatingView = new ActivityTimingFloatingView(this);
      mFloatingView.setLayoutParams(LAYOUT_PARAMS);

      mWindowManager.addView(mFloatingView, LAYOUT_PARAMS);
    }
  }

  public void closeFPS() {
    if (task != null) {
      task.unpause();
      task.killReader();
      task = null;
    }

    if (mFloatingView != null) {
      mWindowManager.removeView(mFloatingView);
      mFloatingView = null;
    }
  }

  private void startUpMainLog() {

    Runnable mainLogRunnable = new Runnable() {

      @Override public void run() {
        task = new LogReaderAsyncTask();
        task.execute((Void) null);
      }
    };

    if (task != null) {
      // do only after current log is depleted, to avoid splicing the streams together
      // (Don't cross the streams!)
      task.unpause();
      task.setOnFinished(mainLogRunnable);
      task.killReader();
      task = null;
    } else {
      // no main log currently running; just start up the main log now
      mainLogRunnable.run();
    }
  }

  private LogReaderAsyncTask task;

  private boolean collapsedMode = false;
  private final Object mLock = new Object();
  //private List<LogLine> logLineList;
  private ArrayFilter mFilter;

  public void addWithFilter(LogLine object, CharSequence text) {

    List<LogLine> inputList = Arrays.asList(object);

    if (mFilter == null) {
      mFilter = new ArrayFilter();
    }

    List<LogLine> filteredObjects = mFilter.performFilteringOnList(inputList, text);

    //&& filteredObjects.get(0).getTag().equalsIgnoreCase("ActivityManager")
    if (null != filteredObjects && filteredObjects.size() > 0 && filteredObjects.get(0).getTag().equalsIgnoreCase("ActivityManager")) {
      synchronized (mLock) {
        //logLineList.addAll(filteredObjects);//TODO 每次添加数组数量为1
        EventBus.getDefault().post(new LogdEvent(filteredObjects.get(0)));
        insertData(filteredObjects.get(0).getLogOutput(),filteredObjects.get(0).getTimestamp());
      }
    }
  }

  private class LogReaderAsyncTask extends AsyncTask<Void, LogLine, Void> {

    private int counter = 0;
    private volatile boolean paused;
    private final Object lock = new Object();
    private boolean firstLineReceived;
    private boolean killed;
    private LogcatReader reader;
    private Runnable onFinished;

    @Override protected Void doInBackground(Void... params) {

      try {
        // use "recordingMode" because we want to load all the existing lines at once
        // for a performance boost
        LogcatReaderLoader loader = LogcatReaderLoader.create(LogService.this, true);
        reader = loader.loadReader();

        int maxLines = PreferenceHelper.getDisplayLimitPreference(LogService.this);

        String line;
        LinkedList<LogLine> initialLines = new LinkedList<LogLine>();
        while ((line = reader.readLine()) != null) {
          if (paused) {
            synchronized (lock) {
              if (paused) {
                lock.wait();
              }
            }
          }
          LogLine logLine = LogLine.newLogLine(line, !collapsedMode);
          if (!reader.readyToRecord()) {
            // "ready to record" in this case means all the initial lines have been flushed from the reader
            initialLines.add(logLine);
            if (initialLines.size() > maxLines) {
              initialLines.removeFirst();
            }
          } else if (!initialLines.isEmpty()) {
            // flush all the initial lines we've loaded
            initialLines.add(logLine);
            publishProgress(ArrayUtil.toArray(initialLines, LogLine.class));
            initialLines.clear();
          } else {
            // just proceed as normal
            publishProgress(logLine);
          }
        }
      } catch (InterruptedException e) {
        Log.d(TAG, "expected error");
      } catch (Exception e) {
        Log.d(TAG, "unexpected error");
      } finally {
        killReader();
        Log.d(TAG, "AsyncTask has died");
      }

      return null;
    }

    public void killReader() {
      if (!killed) {
        synchronized (lock) {
          if (!killed && reader != null) {
            reader.killQuietly();
            killed = true;
          }
        }
      }
    }

    @Override protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      Log.d(TAG, "onPostExecute()");
      doWhenFinished();
    }

    @Override protected void onPreExecute() {
      super.onPreExecute();
      Log.d(TAG, "onPreExecute()");
    }

    @Override protected void onProgressUpdate(LogLine... values) {
      super.onProgressUpdate(values);

      if (!firstLineReceived) {
        firstLineReceived = true;
      }
      for (LogLine logLine : values) {
        addWithFilter(logLine, "Displayed");
      }
    }

    private void doWhenFinished() {
      if (paused) {
        unpause();
      }
      if (onFinished != null) {
        onFinished.run();
      }
    }

    public void pause() {
      synchronized (lock) {
        paused = true;
      }
    }

    public void unpause() {
      synchronized (lock) {
        paused = false;
        lock.notify();
      }
    }

    public boolean isPaused() {
      return paused;
    }

    public void setOnFinished(Runnable onFinished) {
      this.onFinished = onFinished;
    }

  }

  /**
   * <p>An array filter constrains the content of the array adapter with
   * a prefix. Each item that does not start with the supplied prefix
   * is removed from the list.</p>
   */
  private class ArrayFilter extends Filter {

    private int logLevelLimit = 0;

    public int getLogLevelLimit() {
      return logLevelLimit;
    }

    public void setLogLevelLimit(int logLevelLimit) {
      this.logLevelLimit = logLevelLimit;
    }

    @Override protected FilterResults performFiltering(CharSequence prefix) {
      FilterResults results = new FilterResults();

      ArrayList<LogLine> mOriginalValues = new ArrayList<LogLine>();
      ArrayList<LogLine> allValues = performFilteringOnList(mOriginalValues, prefix);

      results.values = allValues;
      results.count = allValues.size();

      return results;
    }

    public ArrayList<LogLine> performFilteringOnList(List<LogLine> inputList, CharSequence query) {

      SearchCriteria searchCriteria = new SearchCriteria(query);

      // search by log level
      ArrayList<LogLine> allValues = new ArrayList<LogLine>();

      ArrayList<LogLine> logLines;
      synchronized (mLock) {
        logLines = new ArrayList<LogLine>(inputList);
      }

      for (LogLine logLine : logLines) {
        if (logLine != null && LogLineAdapterUtil.logLevelIsAcceptableGivenLogLevelLimit(logLine.getLogLevel(),
            logLevelLimit)) {
          allValues.add(logLine);
        }
      }
      ArrayList<LogLine> finalValues = allValues;

      // search by criteria
      if (!searchCriteria.isEmpty()) {

        final ArrayList<LogLine> values = allValues;
        final int count = values.size();

        final ArrayList<LogLine> newValues = new ArrayList<LogLine>(count);

        for (int i = 0; i < count; i++) {
          final LogLine value = values.get(i);
          // search the logline based on the criteria
          if (searchCriteria.matches(value)) {
            newValues.add(value);
          }
        }

        finalValues = newValues;
      }

      return finalValues;
    }

    @SuppressWarnings("unchecked") @Override protected void publishResults(CharSequence constraint, FilterResults results) {
      //noinspection unchecked
      //logLineList = (List<LogLine>) results.values;
    }
  }
}
