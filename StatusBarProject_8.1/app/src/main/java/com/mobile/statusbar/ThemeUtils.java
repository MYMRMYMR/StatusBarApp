package com.mobile.statusbar;

import android.graphics.Color;

public class ThemeUtils {
    
    public static int getOptimalTextColor(boolean isDarkBackground) {
        return isDarkBackground ? Color.WHITE : Color.BLACK;
    }
    
    public static int getBackgroundColor(boolean isDarkMode) {
        if (isDarkMode) {
            return 0xCC000000;  // 黑色半透明
        } else {
            return 0xCCFFFFFF;  // 白色半透明
        }
    }
    
    public static boolean isNightMode() {
        // 简单实现：根据时间判断
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        return hour < 6 || hour > 18;
    }
}