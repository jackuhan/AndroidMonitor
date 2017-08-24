/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.devsupport;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import com.han.devtool.R;
import android.graphics.Point;

import com.facebook.react.modules.core.ChoreographerCompat;
import com.facebook.react.modules.debug.FpsDebugFrameCallback;
import java.util.Locale;

/**
 * View that automatically monitors and displays the current app frame rate. Also logs the current
 * FPS to logcat while active.
 *
 * NB: Requires API 16 for use of FpsDebugFrameCallback.
 */
@TargetApi(16)
public class FpsView extends FrameLayout {

  private static final int UPDATE_INTERVAL_MS = 500;

  private final TextView mTextView;
  private final FpsDebugFrameCallback mFrameCallback;
  private final FPSMonitorRunnable mFPSMonitorRunnable;
  private final WindowManager mWindowManager;

  public FpsView(Context reactContext) {
    super(reactContext);
    inflate(reactContext, R.layout.fps_view, this);
    mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    mTextView = (TextView) findViewById(R.id.fps_text);
    mFrameCallback = new FpsDebugFrameCallback(ChoreographerCompat.getInstance(), reactContext);
    mFPSMonitorRunnable = new FPSMonitorRunnable();
    setCurrentFPS(0, 0, 0, 0);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    mFrameCallback.reset();
    mFrameCallback.start();
    mFPSMonitorRunnable.start();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    mFrameCallback.stop();
    mFPSMonitorRunnable.stop();
  }

  Point preP, curP;
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()){
      case MotionEvent.ACTION_DOWN:
        preP = new Point((int)event.getRawX(), (int)event.getRawY());
        break;

      case MotionEvent.ACTION_MOVE:
        curP = new Point((int)event.getRawX(), (int)event.getRawY());
        int dx = curP.x - preP.x;
        int dy = curP.y - preP.y;

        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
          WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
          //layoutParams.x -= dx;
          layoutParams.y += dy;
          mWindowManager.updateViewLayout(this, layoutParams);

          preP = curP;
        }
        break;
    }

    return false;
  }

  private void setCurrentFPS(double currentFPS, double currentJSFPS, int droppedUIFrames, int total4PlusFrameStutters) {
    String fpsString = String.format(
        Locale.US,
        "UI: %.1f fps\n%d dropped so far\n%d stutters (4+) so far\nJS: %.1f fps",
        currentFPS,
        droppedUIFrames,
        total4PlusFrameStutters,
        currentJSFPS);
    mTextView.setText(fpsString);
    Log.d("debugtool", fpsString);
  }

  /**
   * Timer that runs every UPDATE_INTERVAL_MS ms and updates the currently displayed FPS.
   */
  private class FPSMonitorRunnable implements Runnable {

    private boolean mShouldStop = false;
    private int mTotalFramesDropped = 0;
    private int mTotal4PlusFrameStutters = 0;

    @Override
    public void run() {
      if (mShouldStop) {
        return;
      }
      mTotalFramesDropped += mFrameCallback.getExpectedNumFrames() - mFrameCallback.getNumFrames();
      mTotal4PlusFrameStutters += mFrameCallback.get4PlusFrameStutters();
      setCurrentFPS(mFrameCallback.getFPS(), mFrameCallback.getJSFPS(), mTotalFramesDropped, mTotal4PlusFrameStutters);
      mFrameCallback.reset();

      postDelayed(this, UPDATE_INTERVAL_MS);
    }

    public void start() {
      mShouldStop = false;
      post(this);
    }

    public void stop() {
      mShouldStop = true;
    }
  }
}
