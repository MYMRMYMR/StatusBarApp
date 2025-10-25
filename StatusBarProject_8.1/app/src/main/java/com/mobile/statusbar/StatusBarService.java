package com.mobile.statusbar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatusBarService extends Service {
    private WindowManager windowManager;
    private View statusBarView;
    private Handler handler = new Handler();
    
    private TextView trafficText, networkText, batteryText, timeText;
    private LinearLayout statusLayout;
    
    private long lastRxBytes = 0;
    private long lastTimeStamp = 0;
    private int batteryLevel = 0;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        createStatusBar();
        startUpdates();
        registerBatteryReceiver();
    }
    
    private void createStatusBar() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        statusBarView = inflater.inflate(R.layout.status_bar, null);
        
        trafficText = statusBarView.findViewById(R.id.trafficText);
        networkText = statusBarView.findViewById(R.id.networkText);
        batteryText = statusBarView.findViewById(R.id.batteryText);
        timeText = statusBarView.findViewById(R.id.timeText);
        statusLayout = statusBarView.findViewById(R.id.statusLayout);
        
        // Android 8.1 使用 TYPE_SYSTEM_ALERT
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            320, // 宽度
            36,  // 高度
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(statusBarView, params);
        
        updateTheme();
    }
    
    private void startUpdates() {
        handler.post(updateTask);
    }
    
    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            updateAllInfo();
            handler.postDelayed(this, 1000); // 每秒更新
        }
    };
    
    private void updateAllInfo() {
        // 更新时间
        updateTime();
        
        // 每5秒更新一次其他信息
        if (System.currentTimeMillis() % 5000 < 1000) {
            updateNetworkAndTraffic();
            updateTheme();
        }
    }
    
    private void updateTime() {
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        timeText.setText(time);
    }
    
    private void registerBatteryReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }
    
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryLevel = (int) ((level / (float) scale) * 100);
            String batteryIcon = getBatteryIcon(batteryLevel);
            batteryText.setText(batteryIcon + batteryLevel + "%");
        }
    };
    
    private String getBatteryIcon(int level) {
        if (level >= 90) return "🔋";
        if (level >= 70) return "🔋";
        if (level >= 50) return "🔋";
        if (level >= 20) return "🔋";
        return "🪫";
    }
    
    private void updateNetworkAndTraffic() {
        // 网络类型
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        String networkType = "❌";
        if (info != null && info.isConnected()) {
            switch (info.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    networkType = "📡";
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    networkType = "📶";
                    break;
                default:
                    networkType = "🌐";
            }
        }
        networkText.setText(networkType);
        
        // 网速计算
        updateNetworkSpeed();
    }
    
    private void updateNetworkSpeed() {
        long currentRxBytes = TrafficStats.getTotalRxBytes();
        long currentTime = System.currentTimeMillis();
        long speed = 0;
        
        if (lastTimeStamp > 0 && currentTime > lastTimeStamp) {
            long timeDiff = currentTime - lastTimeStamp;
            long bytesDiff = currentRxBytes - lastRxBytes;
            speed = bytesDiff * 1000 / timeDiff;
        }
        
        lastRxBytes = currentRxBytes;
        lastTimeStamp = currentTime;
        
        String speedText = formatSpeed(speed);
        trafficText.setText(speedText);
    }
    
    private String formatSpeed(long speed) {
        if (speed < 1024) return speed + "B";
        if (speed < 1024 * 1024) return (speed / 1024) + "K";
        return (speed / (1024 * 1024)) + "M";
    }
    
    private void updateTheme() {
        // 简单主题适配 - 根据时间判断日夜模式
        int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date()));
        boolean isNight = hour < 6 || hour > 18;
        
        int textColor = isNight ? 0xFFFFFFFF : 0xFF000000;
        int bgColor = isNight ? 0xCC000000 : 0xCCFFFFFF;
        
        trafficText.setTextColor(textColor);
        networkText.setTextColor(textColor);
        batteryText.setTextColor(textColor);
        timeText.setTextColor(textColor);
        statusLayout.setBackgroundColor(bgColor);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTask);
        if (windowManager != null && statusBarView != null) {
            windowManager.removeView(statusBarView);
        }
        try {
            unregisterReceiver(batteryReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}