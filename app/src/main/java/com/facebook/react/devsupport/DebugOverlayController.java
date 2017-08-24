/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.devsupport;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Helper class for controlling overlay view with FPS and JS FPS info
 * that gets added directly to @{link WindowManager} instance.
 */
/* package */ public class DebugOverlayController {

  private final WindowManager mWindowManager;
  private final Context mReactContext;

  private @Nullable FrameLayout mFPSDebugViewContainer;

  public DebugOverlayController(Context reactContext) {
    mReactContext = reactContext;
    mWindowManager = (WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE);
  }

  public void setFpsDebugViewVisible() {
    if (mFPSDebugViewContainer != null) {
      stopFps();
    }
    mFPSDebugViewContainer = new FpsView(mReactContext);
    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    params.x = 0;
    params.y = 0;
    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
    params.gravity = Gravity.RIGHT | Gravity.TOP;
    params.type = WindowManager.LayoutParams.TYPE_PHONE;
    params.format = PixelFormat.RGBA_8888;
    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    mWindowManager.addView(mFPSDebugViewContainer, params);
  }

  public void stopFps(){
    if(null==mFPSDebugViewContainer){
      return;
    }
    mFPSDebugViewContainer.removeAllViews();
    mWindowManager.removeView(mFPSDebugViewContainer);
    mFPSDebugViewContainer = null;
  }
}
