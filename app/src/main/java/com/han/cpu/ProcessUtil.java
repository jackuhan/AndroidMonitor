package com.han.cpu;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 进程相关工具类
 * Created by hui.zhao on 2016/6/6.
 */
public class ProcessUtil {
  /**
   * 根据包名获取pid
   *
   * @return pid
   */
  public static int getPidByPackageName(Context context, String packageName) {
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      try {
        Process p = Runtime.getRuntime().exec("top -m 100 -n 1");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
          if (line.contains(packageName)) {
            line = line.trim();
            String[] splitLine = line.split("\\s+");
            if (packageName.equals(splitLine[splitLine.length - 1])) {
              return Integer.parseInt(splitLine[0]);
            }
          }
        }
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    } else {
      List<ActivityManager.RunningAppProcessInfo> run = am.getRunningAppProcesses();
      for (ActivityManager.RunningAppProcessInfo runningProcess : run) {
        if ((runningProcess.processName != null) && runningProcess.processName.equals(packageName)) {
          return runningProcess.pid;
        }
      }
    }
    return 0;
  }

  /**
   * 获取当前应用进程的pid
   */
  public static int getCurrentPid() {
    return android.os.Process.myPid();
  }
}
