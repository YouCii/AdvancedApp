package com.youcii.advanced;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class MyService extends Service {

    private WindowManager wManager;// 窗口管理者
    private WindowManager.LayoutParams mParams;// 窗口的属性

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        // 系统提示window
        if (Build.VERSION.SDK_INT >= 26) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        mParams.format = PixelFormat.TRANSLUCENT;// 支持透明
        mParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// 焦点
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.gravity = Gravity.CENTER;
        mParams.windowAnimations = android.R.style.Animation_Toast;
        // mParams.alpha = 0.8f;//窗口的透明度


        TextView textView = new TextView(MyApplication.context);
        textView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setText("阿诗丹顿多多多多多多多多多多多阿诗丹顿多多多多多多多多多多多阿诗丹顿多多多多多多多多多多多阿诗丹顿多多多多多多多多多多多阿诗丹顿多多多多多多多多多多多");
        wManager.addView(textView, mParams);// 添加窗口

    }

}