package com.han.cpu;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存相关工具类
 * 所有结果以KB为单位
 * Created by hui.zhao on 2016/6/6.
 */
public class MemoryUtil {

  /**
   * 获取总体内存使用情况
   */
  public static void getMemoryInfo(final Context context, final OnGetMemoryInfoCallback onGetMemoryInfoCallback) {
    new Thread(new Runnable() {
      @Override public void run() {
        final String pkgName = context.getPackageName();
        final int pid = ProcessUtil.getCurrentPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //1. ram
        final RamMemoryInfo ramMemoryInfo = new RamMemoryInfo();
        ramMemoryInfo.availMem = mi.availMem / 1024;
        ramMemoryInfo.isLowMemory = mi.lowMemory;
        ramMemoryInfo.lowMemThreshold = mi.threshold / 1024;
        ramMemoryInfo.totalMem = MemoryUtil.getRamTotalMemSync(context);
        //2. pss
        final PssInfo pssInfo = MemoryUtil.getAppPssInfo(context, pid);
        //3. dalvik heap
        final DalvikHeapMem dalvikHeapMem = MemoryUtil.getAppDalvikHeapMem();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override public void run() {
            onGetMemoryInfoCallback.onGetMemoryInfo(pkgName, pid, ramMemoryInfo, pssInfo, dalvikHeapMem);
          }
        });
      }
    }).start();
  }

  /**
   * 获取手机RAM的存储情况
   */
  public static void getSystemRam(final Context context, final OnGetRamMemoryInfoCallback onGetRamMemoryInfoCallback) {
    getRamTotalMem(context, new OnGetRamTotalMemCallback() {
      @Override public void onGetRamTotalMem(long totalMem) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        RamMemoryInfo ramMemoryInfo = new RamMemoryInfo();
        ramMemoryInfo.availMem = mi.availMem / 1024;
        ramMemoryInfo.isLowMemory = mi.lowMemory;
        ramMemoryInfo.lowMemThreshold = mi.threshold / 1024;
        ramMemoryInfo.totalMem = totalMem;
        onGetRamMemoryInfoCallback.onGetRamMemoryInfo(ramMemoryInfo);
      }
    });
  }

  /**
   * 获取应用实际占用内存
   *
   * @return 应用pss信息KB
   */
  public static PssInfo getAppPssInfo(Context context, int pid) {
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    Debug.MemoryInfo memoryInfo = am.getProcessMemoryInfo(new int[] { pid })[0];
    PssInfo pssInfo = new PssInfo();
    pssInfo.totalPss = memoryInfo.getTotalPss();
    pssInfo.dalvikPss = memoryInfo.dalvikPss;
    pssInfo.nativePss = memoryInfo.nativePss;
    pssInfo.otherPss = memoryInfo.otherPss;
    return pssInfo;
  }

  /**
   * 获取应用dalvik内存信息
   *
   * @return dalvik堆内存KB
   */
  public static DalvikHeapMem getAppDalvikHeapMem() {
    Runtime runtime = Runtime.getRuntime();
    DalvikHeapMem dalvikHeapMem = new DalvikHeapMem();
    dalvikHeapMem.freeMem = runtime.freeMemory() / 1024;
    dalvikHeapMem.maxMem = Runtime.getRuntime().maxMemory() / 1024;
    dalvikHeapMem.allocated = (Runtime.getRuntime().totalMemory() - runtime.freeMemory()) / 1024;
    return dalvikHeapMem;
  }

  /**
   * 获取应用能够获取的max dalvik堆内存大小
   * 和Runtime.getRuntime().maxMemory()一样
   *
   * @return 单位M
   */
  public static long getAppTotalDalvikHeapSize(Context context) {
    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    return manager.getMemoryClass();
  }

  /**
   * Dalvik堆内存，只要App用到的内存都算（包括共享内存）
   */
  public static class DalvikHeapMem {
    public long freeMem;
    public long maxMem;
    public long allocated;
  }

  /**
   * 应用实际占用内存（共享按比例分配）
   */
  public static class PssInfo {
    public int totalPss;
    public int dalvikPss;
    public int nativePss;
    public int otherPss;
  }

  /**
   * 手机RAM内存信息
   * 物理内存信息
   */
  public static class RamMemoryInfo {
    //可用RAM
    public long availMem;
    //手机总RAM
    public long totalMem;
    //内存占用满的阀值，超过即认为低内存运行状态，可能会Kill process
    public long lowMemThreshold;
    //是否低内存状态运行
    public boolean isLowMemory;
  }

  /**
   * 内存相关的所有数据
   */
  public interface OnGetMemoryInfoCallback {
    void onGetMemoryInfo(String pkgName, int pid, RamMemoryInfo ramMemoryInfo, PssInfo pssInfo, DalvikHeapMem dalvikHeapMem);
  }

  public interface OnGetRamMemoryInfoCallback {
    void onGetRamMemoryInfo(RamMemoryInfo ramMemoryInfo);
  }

  private interface OnGetRamTotalMemCallback {
    //手机总RAM容量/KB
    void onGetRamTotalMem(long totalMem);
  }

  /**
   * 获取手机RAM容量/手机实际内存
   * 单位
   */
  private static void getRamTotalMem(final Context context, final OnGetRamTotalMemCallback onGetRamTotalMemCallback) {
    new Thread(new Runnable() {
      @Override public void run() {
        final long totalRam = getRamTotalMemSync(context);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override public void run() {
            onGetRamTotalMemCallback.onGetRamTotalMem(totalRam);
          }
        });
      }
    }).start();
  }

  /**
   * 同步获取系统的总ram大小
   */
  private static long getRamTotalMemSync(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
      am.getMemoryInfo(mi);
      return mi.totalMem / 1024;
    } else if (sTotalMem.get() > 0L) {//如果已经从文件获取过值，则不需要再次获取
      return sTotalMem.get();
    } else {
      final long tm = getRamTotalMemByFile();
      sTotalMem.set(tm);
      return tm;
    }
  }

  private static AtomicLong sTotalMem = new AtomicLong(0L);

  /**
   * 获取手机的RAM容量，其实和activityManager.getMemoryInfo(mi).totalMem效果一样，也就是说，在API16以上使用系统API获取，低版本采用这个文件读取方式
   *
   * @return 容量KB
   */
  private static long getRamTotalMemByFile() {
    final String dir = "/proc/meminfo";
    try {
      FileReader fr = new FileReader(dir);
      BufferedReader br = new BufferedReader(fr, 2048);
      String memoryLine = br.readLine();
      String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
      br.close();
      long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
      return totalMemorySize;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return 0L;
  }
}
