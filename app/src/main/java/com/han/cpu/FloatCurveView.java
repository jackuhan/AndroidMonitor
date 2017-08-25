package com.han.cpu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.StringDef;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.han.devtool.R;
import com.tt.curvechartlib.CurveChartView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description: <悬浮内存显示曲线图，M为内存单位>
 */

public class FloatCurveView extends RelativeLayout {

  private static final String VALUE_FORMAT = "%.1fM";
  private static final String VALUE_FORMAT_TXT = "%1$s:%2$.1fM";

  @Retention(RetentionPolicy.SOURCE) @StringDef({ MEMORY_TYPE_PSS, MEMORY_TYPE_HEAP }) public @interface MemoryType {
  }

  public static final String MEMORY_TYPE_PSS = "pss";
  public static final String MEMORY_TYPE_HEAP = "heap";

  public static class Config {
    public int height = WindowManager.LayoutParams.MATCH_PARENT;
    public int width = WindowManager.LayoutParams.MATCH_PARENT;
    public int padding = 0;
    public int x = 0;
    public int y = 0;
    public int dataSize = 10;
    public int yPartCount = 5;
    public @MemoryType String type;
  }

  private final WindowManager mWindowManager;
  private CurveChartView mCurveChartView;

  private TextView mNameAndValueTv;

  public FloatCurveView(Context context) {
    super(context);
    mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    initView();
  }

  private void initView() {
    inflate(getContext(), R.layout.mem_floatview, this);
    mCurveChartView = (CurveChartView) this.findViewById(R.id.mem_monitor_view_floatcurveview);
    mNameAndValueTv = (TextView) this.findViewById(R.id.mem_monitor_view_namevalue);

    findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        getContext().startService(
            new Intent(getContext(), CPUService.class).putExtra(CPUService.CPU_COMMAND, CPUService.CPU_COMMAND_CLOSE));
      }
    });
  }

  public void attachToWindow(Config config) {
    mPrefix = config.type;
    com.tt.curvechartlib.Config.Builder builder = new com.tt.curvechartlib.Config.Builder();
    builder.setYFormat(VALUE_FORMAT)
        .setDataSize(config.dataSize)
        .setMaxValueMulti(1.2f)
        .setMinValueMulti(0.8f)
        .setXTextPadding(70)
        .setYPartCount(config.yPartCount)
        .setYLabelSize(20f);
    mCurveChartView.setUp(builder.create());
    mCurveChartView.setPadding(config.padding, config.padding, config.padding, config.padding);
  }

  private String mPrefix;

  public void setText(float value) {
    mNameAndValueTv.setText(String.format(VALUE_FORMAT_TXT, mPrefix, value));
  }

  public void addData(float data) {
    mCurveChartView.addData(data);
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
