package uk.co.birchlabs.touhouwalk.services.walker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.util.Arrays;

/**
 * Created by birch on 08/10/2016.
 */

public class WalkerService extends Service {
    private WalkerView view;
    private RenderWorker renderWorker;
    private WorldWorker worldWorker;
    private ServiceEventHandler serviceEventHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onCreate() {

        System.out.println("Walker service created");

        super.onCreate();
        if(isIgnoringBatteryOptimizations() == false){
        requestIgnoreBatteryOptimizations();
        }
        Log.d("WalkerService", "WalkService is create!");
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public int onStartCommand(Intent intent, int flags, int startId) {
        final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        final DisplayMetrics metrics = new DisplayMetrics();

        wm.getDefaultDisplay().getMetrics(metrics);

        view = new WalkerView(this);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, // TYPE_SYSTEM_ALERT is denied in apiLevel >=19
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT
        );
//        view.setFitsSystemWindows(false); // allow us to draw over status bar, navigation bar
//        params.width = params.height = Math.max(metrics.widthPixels, metrics.heightPixels);
        params.setTitle("Touhou");
        Log.d("WalkerService","Hey,I'm Running!");

        wm.addView(view, params);

        final GensoukyouFactory gensoukyouFactory = new GensoukyouFactory(
                metrics.widthPixels,
                metrics.heightPixels,
                getApplicationContext()
        );
        final Gensoukyou gensoukyou = gensoukyouFactory.construct();

        renderWorker = new RenderWorker(view);
        worldWorker = new WorldWorker(gensoukyou);

        serviceEventHandler = new ServiceEventHandlerDelegator(
                Arrays.asList(
                        worldWorker.getServiceEventHandler(),
                        renderWorker.getServiceEventHandler()
                )
        );

        view.init(
                new ViewLifeCycleCallbackDelegator(
                        Arrays.asList(
                                worldWorker.getViewEventHandler(),
                                renderWorker.getViewEventHandler(),
                                gensoukyou.getViewEventHandler()
                        )
                ),
                gensoukyou
        );

//
//        final LayoutInflater inflater = LayoutInflater.from(this);
//
//        final ViewGroup mTopView = (ViewGroup) inflater.inflate(R.layout.activity_main, null);
//        this.getWindow().setAttributes(params);
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceEventHandler.onDestroyed();
        ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(view);
        view = null;
        Log.d("WalkerService","Oops,I'm Destroied.");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        serviceEventHandler.onConfigurationChanged(newConfig);
    }
}
