package com.han.activitytracker;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import de.greenrobot.event.EventBus;

public class TrackerService extends AccessibilityService {
  public static final String TAG = "TrackerService";
  public static final String Tracker_COMMAND = "Tracker_COMMAND";
  public static final String Tracker_COMMAND_OPEN = "Tracker_COMMAND_OPEN";
  public static final String Tracker_COMMAND_CLOSE = "Tracker_COMMAND_CLOSE";
  TrackerWindowManager mTrackerWindowManager;

  private void initTrackerWindowManager() {
    if (mTrackerWindowManager == null) mTrackerWindowManager = new TrackerWindowManager(this);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");
    initTrackerWindowManager();

    String command = intent.getStringExtra(Tracker_COMMAND);
    if (command != null) {
      if (command.equals(Tracker_COMMAND_OPEN)) {
        mTrackerWindowManager.addView();
      } else if (command.equals(Tracker_COMMAND_CLOSE)) mTrackerWindowManager.removeView();
    }

    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onInterrupt() {

  }

  @Override public void onAccessibilityEvent(AccessibilityEvent event) {
    Log.d(TAG, "onAccessibilityEvent: " + event.getPackageName());
    if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

      EventBus.getDefault().post(new ActivityChangedEvent(event.getPackageName().toString(), event.getClassName().toString()));
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy");
  }

  public static class ActivityChangedEvent {
    private final String mPackageName;
    private final String mClassName;

    public ActivityChangedEvent(String packageName, String className) {
      mPackageName = packageName;
      mClassName = className;
    }

    public String getPackageName() {
      return mPackageName;
    }

    public String getClassName() {
      return mClassName;
    }
  }
}
