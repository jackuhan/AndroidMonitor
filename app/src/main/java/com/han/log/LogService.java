package com.han.log;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.react.devsupport.DebugOverlayController;

public class LogService extends Service {
  public static final String TAG = "LogService";
  public static final String COMMAND = "COMMAND";
  public static final String COMMAND_OPEN = "COMMAND_OPEN";
  public static final String COMMAND_CLOSE = "COMMAND_CLOSE";

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");

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

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy");
  }

  public void openFPS() {

  }

  public void closeFPS() {
  }
}
