package com.han.cpu;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

public class CPUService extends Service {
  public static final String TAG = "CPUService";
  public static final String CPU_COMMAND = "CPU_COMMAND";
  public static final String CPU_COMMAND_OPEN = "CPU_COMMAND_OPEN";
  public static final String CPU_COMMAND_CLOSE = "CPU_COMMAND_CLOSE";
  private WindowManager mWindowManager;

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");

    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    String command = intent.getStringExtra(CPU_COMMAND);
    if (command != null) {
      if (command.equals(CPU_COMMAND_OPEN)) {
        openCPU();
      } else if (command.equals(CPU_COMMAND_CLOSE)) {
        closeCPU();
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

  private FloatCurveView mFloatingView;
  private static final WindowManager.LayoutParams LAYOUT_PARAMS;

  static {
    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    params.x = 0;
    params.y = 0;
    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
    params.gravity = Gravity.CENTER | Gravity.TOP;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      params.type = WindowManager.LayoutParams.TYPE_PHONE;
    }
    params.format = PixelFormat.RGBA_8888;
    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

    LAYOUT_PARAMS = params;
  }

  public void openCPU() {
    if (mFloatingView == null) {
      mFloatingView = new FloatCurveView(this);
      mFloatingView.setLayoutParams(LAYOUT_PARAMS);

      mWindowManager.addView(mFloatingView, LAYOUT_PARAMS);
      MemoryMonitor.getInstance().init(this);
      MemoryMonitor.getInstance().start(FloatCurveView.MEMORY_TYPE_PSS, mFloatingView);
    }
  }

  public void closeCPU() {
    if (mFloatingView != null) {
      mWindowManager.removeView(mFloatingView);
      mFloatingView = null;
      MemoryMonitor.getInstance().stop();
    }
  }
}
