package com.han.devtool;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.facebook.react.devsupport.DebugOverlayController;
import com.han.activitytracker.AccessibilityUtil;
import com.han.activitytracker.TrackerService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  private DebugOverlayController mDebugOverlayController;
  private static final int REQUEST_CODE = 1;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void openFPS() {
    if (mDebugOverlayController == null) {
      mDebugOverlayController = new DebugOverlayController(this);
    }
    mDebugOverlayController.setFpsDebugViewVisible();
  }

  public void closeFPS() {
    if (mDebugOverlayController != null) {
      mDebugOverlayController.stopFps();
    }
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button1: {
        openFPS();
        break;
      }
      case R.id.button2: {
        closeFPS();
        break;
      }
      case R.id.button3: {
        if (AccessibilityUtil.checkAccessibility(this)) {
          startService(new Intent(this, TrackerService.class).putExtra(TrackerService.COMMAND, TrackerService.COMMAND_OPEN));
        }
        break;
      }
    }
  }

  @Override protected void onDestroy() {
    //closeFPS();
    super.onDestroy();
  }

  private void checkOverlayPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!Settings.canDrawOverlays(this)) {
        startActivityForResult(
            new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK), REQUEST_CODE);
        Toast.makeText(this, "请先授予 \"devtool\" 悬浮窗权限", Toast.LENGTH_LONG).show();
      }
    }
  }
}
