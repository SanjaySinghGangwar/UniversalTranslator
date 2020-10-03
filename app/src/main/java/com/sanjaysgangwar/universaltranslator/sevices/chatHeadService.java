package com.sanjaysgangwar.universaltranslator.sevices;


import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sanjaysgangwar.universaltranslator.R;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.sanjaysgangwar.universaltranslator.sevices.Storage.saveToDataDir;

public class chatHeadService extends Service implements FloatingViewListener {
    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";
    public static final int NOTIFICATION_ID = 9083150;
    public static CircleImageView iconView = null;
    public static Intent resultData = null;
    public static int resultCode = 0;
    final private HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
    public LayoutInflater inflater;
    public WindowManager windowManager;
    public View view;
    Context context = chatHeadService.this;
    DisplayMetrics metrics;
    ImageView crossArrow, tickArrow;
    NotificationManager mNotificationManager;
    AppOpsManager appOps;
    UsageStatsManager usm;
    String defaultHomePackageName, currentForegroundPackageName;
    String extractText;


    private FloatingViewManager mFloatingViewManager;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    private Handler handler;
    private MediaProjectionManager mgr;

    public chatHeadService() {
    }

    public int getStatusBarHeight() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        int result = 0;
        int resourceId = cw.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = cw.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mgr = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
            usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);


        metrics = new DisplayMetrics();
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloatingViewManager != null) {
            return START_REDELIVER_INTENT;
        }

       /* if (intent.getAction() == null) {
            resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 1337);
            resultData = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
        }*/
        windowManager.getDefaultDisplay().getMetrics(metrics);
        inflater = LayoutInflater.from(this);
        iconView = (CircleImageView) inflater.inflate(R.layout.widget_chathead, null, false);

        iconView.setOnClickListener(v -> {
            extractText = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (usageAccessGranted(context)) {

                    getCurrentAppForegound();

                    if (defaultHomePackageName.equalsIgnoreCase(currentForegroundPackageName)) {
                        Toast.makeText(context, "Open app where I have to read", Toast.LENGTH_LONG).show();
                    } else {
                        Bitmap b = Screenshot.takescreenshotOfRootView(v);
                        Uri uri = saveToDataDir(this, b, "Screenshot");
                        Toast.makeText(context, "" + uri, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent1 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    intent1.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                }
            }
        });

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.recycle_bin);
        //mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
        mFloatingViewManager.setSafeInsetRect(intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.overMargin = (int) (16 * metrics.density);
        mFloatingViewManager.addViewToWindow(iconView, options);
        startForeground(NOTIFICATION_ID, createNotification(this));

        return START_REDELIVER_INTENT;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean usageAccessGranted(Context context) {
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }


    public void getCurrentAppForegound() {

        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        defaultHomePackageName = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        Log.e("hgnis", defaultHomePackageName);

        if (Build.VERSION.SDK_INT >= 21) {
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentForegroundPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {

            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            currentForegroundPackageName = (manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
        }
        Log.e("hgnis", "Current App in foreground is: " + currentForegroundPackageName);

    }

    @Override
    public void onDestroy() {
        destroy();
        stopCapture();
        Log.e("onDestroyService", "onDestroyService");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void destroy() {

        if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
            mFloatingViewManager = null;
        }
    }


    private Notification createNotification(Context context) {
        String NOTIFICATION_CHANNEL_ID = "com.sanjaysgangwar.universaltranslator";
        String channelName = "My Background Service";
        NotificationChannel chan = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("App is running in background")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        return notification;
    }

    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {

    }


    public void stopCapture() {
        if (projection != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                projection.stop();
            }
            vdisplay.release();
            projection = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("onTaskRemoved", "onTaskRemoved");

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(chatHeadService.this, chatHeadService.class));
        } else {
            context.startService(new Intent(chatHeadService.this, chatHeadService.class));
        }
        super.onTaskRemoved(rootIntent);
    }


}
