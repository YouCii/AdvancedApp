package com.youcii.advanced;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

/**
 * Created by jingdongwei on 2022/6/8.
 */
public class Utils {
    public static boolean isZenModeGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.isNotificationPolicyAccessGranted();
        }
        return true;
    }

    public static boolean isZenMode(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int mode = notificationManager.getCurrentInterruptionFilter();
            return mode > NotificationManager.INTERRUPTION_FILTER_ALL;
        } else {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int mode = audioManager.getRingerMode();
            return mode > AudioManager.RINGER_MODE_SILENT;
        }
    }

    public static void setZenMode(Context context, boolean isZen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // TODO 使用NotificationManager.Policy切换勿扰模式
            // NotificationManager.Policy policy = new NotificationManager.Policy();
            // notificationManager.setNotificationPolicy();
            int mode = isZen ? NotificationManager.INTERRUPTION_FILTER_PRIORITY : NotificationManager.INTERRUPTION_FILTER_ALL;
            notificationManager.setInterruptionFilter(mode);
        } else {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int newMode = isZen ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_NORMAL;
            audioManager.setRingerMode(newMode);
        }
    }
}
