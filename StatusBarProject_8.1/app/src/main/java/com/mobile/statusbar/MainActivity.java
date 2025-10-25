package com.mobile.statusbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建简单界面
        Button startBtn = new Button(this);
        startBtn.setText("启动状态栏");
        startBtn.setOnClickListener(v -> checkOverlayPermission());
        
        Button stopBtn = new Button(this);
        stopBtn.setText("停止状态栏");
        stopBtn.setOnClickListener(v -> stopStatusBarService());
        
        // 简单布局
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(startBtn);
        layout.addView(stopBtn);
        
        setContentView(layout);
        
        // 检查悬浮窗权限 (Android 6.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission();
            }
        }
    }
    
    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission();
                return;
            }
        }
        startStatusBarService();
    }
    
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    startStatusBarService();
                } else {
                    Toast.makeText(this, "需要悬浮窗权限才能运行", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    private void startStatusBarService() {
        Intent serviceIntent = new Intent(this, StatusBarService.class);
        startService(serviceIntent);
        Toast.makeText(this, "状态栏已启动", Toast.LENGTH_SHORT).show();
    }
    
    private void stopStatusBarService() {
        Intent serviceIntent = new Intent(this, StatusBarService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "状态栏已停止", Toast.LENGTH_SHORT).show();
    }
}