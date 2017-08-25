package com.han.log;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.han.devtool.R;
import com.nolanlawson.logcat.data.LogLine;
import de.greenrobot.event.EventBus;

public class ActivityTimingFloatingView extends LinearLayout {
  public static final String TAG = "FloatingView";

  private final Context mContext;
  private final WindowManager mWindowManager;
  private TextView mTvPackageName;
  private ImageView mIvClose;

  public ActivityTimingFloatingView(Context context) {
    super(context);
    mContext = context;
    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    initView();
  }

  private void initView() {
    inflate(mContext, R.layout.layout_log_floating, this);
    mTvPackageName = (TextView) findViewById(R.id.tv_package_name);
    mIvClose = (ImageView) findViewById(R.id.iv_close);

    mIvClose.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        mContext.startService(
            new Intent(mContext, LogService.class).putExtra(LogService.COMMAND, LogService.COMMAND_CLOSE));
      }
    });

    findViewById(R.id.more).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        Intent i = new Intent(mContext, MyListActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
      }
    });
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    EventBus.getDefault().register(this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    EventBus.getDefault().unregister(this);
  }

  public void onEventMainThread(LogService.LogdEvent event) {
    LogLine mLogline = event.mLogLine;

    mTvPackageName.setText(mLogline.getLogOutput());
  }

  Point preP, curP;

  @Override public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        preP = new Point((int) event.getRawX(), (int) event.getRawY());
        break;

      case MotionEvent.ACTION_MOVE:
        curP = new Point((int) event.getRawX(), (int) event.getRawY());
        int dx = curP.x - preP.x,
            dy = curP.y - preP.y;

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
        layoutParams.x += dx;
        layoutParams.y += dy;
        mWindowManager.updateViewLayout(this, layoutParams);

        preP = curP;
        break;
    }

    return false;
  }
}
