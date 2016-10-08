package birchlabs.co.uk.touhouwalk.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import birchlabs.co.uk.touhouwalk.R;
import birchlabs.co.uk.touhouwalk.views.WalkerView;

/**
 * Created by birch on 08/10/2016.
 */

public class Walker extends Service {
    private View view;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        System.out.println("Walker service created");

        super.onCreate();

        final WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);

        final DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        view = new WalkerView(this);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, // TYPE_SYSTEM_ALERT is denied in apiLevel >=19
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.setTitle("Touhou");

//
//        final LayoutInflater inflater = LayoutInflater.from(this);
//
//        final ViewGroup mTopView = (ViewGroup) inflater.inflate(R.layout.activity_main, null);
//        this.getWindow().setAttributes(params);
        wm.addView(view, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(view);
        view = null;
    }
}
