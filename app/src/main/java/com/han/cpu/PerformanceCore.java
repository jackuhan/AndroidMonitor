package com.han.cpu;

public class PerformanceCore {
  //private SamplerThread mSamplerThread;
  //private SamplerIntercepter mSamplerIntercepter;
  //private Context mContext;
  //private boolean mIsForeground;//APP是否位于前台
  //private boolean mStart;//是否已开启
  //
  //public PerformanceCore(Context context) {
  //    this.mContext = context;
  //}
  //
  //public void start() {
  //    mStart = true;
  //    mIsForeground = true;
  //
  //
  //    if (mMonitorViewWrapper == null) {
  //        mMonitorViewWrapper = new MonitorView(mContext);
  //    }
  //    mMonitorViewWrapper.show();
  //
  //    if (mMonitorViewWrapper instanceof IMonitorRecord) {
  //        mSamplerIntercepter = new SamplerIntercepter((IMonitorRecord) mMonitorViewWrapper);
  //    }
  //    mSamplerThread = new SamplerThread(mSamplerIntercepter, 300f);
  //    mSamplerThread.startSampling();
  //
  //    if (mContext instanceof Application) {
  //        ((Application) mContext).registerActivityLifecycleCallbacks(this);
  //    }
  //}
  //
  //public void stop() {
  //    mStart = false;
  //    mSamplerThread.stopSampling();
  //    mMonitorViewWrapper.close();
  //}
}
