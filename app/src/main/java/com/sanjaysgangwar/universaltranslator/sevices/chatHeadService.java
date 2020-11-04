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
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sanjaysgangwar.universaltranslator.R;
import com.sanjaysgangwar.universaltranslator.api.apiInterface;
import com.sanjaysgangwar.universaltranslator.crop_slider.CropImageView;
import com.sanjaysgangwar.universaltranslator.modelClasses.translateModel.Model;
import com.sanjaysgangwar.universaltranslator.modelClasses.translateModel.Translation;
import com.sanjaysgangwar.universaltranslator.modelClasses.visionModel.VisionModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class chatHeadService extends Service implements FloatingViewListener, View.OnClickListener {

    public static final String EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area";
    public static final int NOTIFICATION_ID = 9083150;
    /*Screenshot Variables */
    static final int VIRT_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
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
    Bitmap icon;
    CropImageView cropImageView;
    NotificationManager mNotificationManager;
    String timeStamp;
    AppOpsManager appOps;
    UsageStatsManager usm;
    String defaultHomePackageName, currentForegroundPackageName;
    View view2;
    Translation translation;
    String translatedText;
    TextView resultTV;
    String extractText;
    TextToSpeech tss;
    ImageView textToSpeak;
    int count;
    int Ccounter;

    String key = "AIzaSyA7hQ5A_MnRf2TM2yf0nIO61wdqNKPWgyQ";

    private FloatingViewManager mFloatingViewManager;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    private Handler handler;
    private MediaProjectionManager mgr;
    private ImageTransmogrifier it;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String APP_SHARED_PREFS;

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
        sharedPreferences = this.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        APP_SHARED_PREFS = "targetLanguage";


        mgr = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
        usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);


        metrics = new DisplayMetrics();
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        tss = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int lang = tss.setLanguage(Locale.getDefault());
                    tss.setVoice(tss.getVoice());
                    tss.setPitch(1);
                    tss.setSpeechRate(0.9f);
                    if (lang == TextToSpeech.LANG_MISSING_DATA
                            || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(chatHeadService.this, "Not supported", Toast.LENGTH_SHORT).show();
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloatingViewManager != null) {
            return START_REDELIVER_INTENT;
        }
        windowManager.getDefaultDisplay().getMetrics(metrics);
        inflater = LayoutInflater.from(this);
        iconView = (CircleImageView) inflater.inflate(R.layout.widget_chathead, null, false);

        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resultVisibilityOff();
                count = 0;
                Ccounter = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    if (usageAccessGranted(context)) {

                        getCurrentAppForegound();

                        if (defaultHomePackageName.equalsIgnoreCase(currentForegroundPackageName)) {
                            Toast.makeText(context, "No App is in Foreground", Toast.LENGTH_LONG).show();
                        } else {


                            createLayoutForServiceClass();
                        }
                    } else {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.recycle_bin);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.recycle_bin);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
        mFloatingViewManager.setSafeInsetRect((Rect) intent.getParcelableExtra(EXTRA_CUTOUT_SAFE_AREA));
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

    private void createLayoutForServiceClass() {

        iconView.setVisibility(View.GONE);

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        params.gravity = Gravity.END | Gravity.TOP;
        view = inflater.inflate(R.layout.area_selection, null);

        crossArrow = view.findViewById(R.id.iv_cross);
        tickArrow = view.findViewById(R.id.iv_tick);
        cropImageView = view.findViewById(R.id.CropImageView);

        icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.transparent_image);
        cropImageView.setImageBitmap(icon);

        crossArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
                iconView.setVisibility(View.VISIBLE);
            }
        });
        tickArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crossArrow.setVisibility(View.GONE);
                tickArrow.setVisibility(View.GONE);
                startCapture();
            }
        });
        windowManager.addView(view, params);

    }

    public void getCurrentAppForegound() {

        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        defaultHomePackageName = localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;

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
            resultVisibilityOff();
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

    /*Screenshot Code*/
    public void processImage(final byte[] png) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                timeStamp = new SimpleDateFormat("yyyyMMddHHmmssmsms").format(new Date()) + random.nextInt(400);

                File outputImage = new File(getCacheDir()/*getExternalFilesDir(null)*/, "image" + ".png");

                FileOutputStream fos = null;

                try {
                    fos = new FileOutputStream(outputImage);
                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();


                    MediaScannerConnection.scanFile(chatHeadService.this,
                            new String[]{outputImage.getAbsolutePath()},
                            new String[]{"image/png"},
                            null);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                    Toast.makeText(context, "WRITE ERROR " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                hideViews();
            }
        });

        stopCapture();

        loadImageFromStorage(getCacheDir(), "image");
    }

    private void hideViews() {
        if (windowManager != null) {
            windowManager.removeView(view);
            windowManager = null;
        }

        iconView.setVisibility(View.VISIBLE);
    }

    private void loadImageFromStorage(File path, String imageName) {

        try {
            File f = new File(path, "image" + ".png");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));

            b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            //vision Api
            JsonObject type = new JsonObject();
            JsonObject content = new JsonObject();
            JsonObject requests = new JsonObject();
            JsonArray jsonArray = new JsonArray();

            content.addProperty("content", imageString);
            type.addProperty("type", "DOCUMENT_TEXT_DETECTION");/*TEXT_DETECTION*/
            requests.add("image", content);
            JsonArray array = new JsonArray();
            array.add(type);
            requests.add("features", array);
            jsonArray.add(requests);
            JsonObject request = new JsonObject();
            request.add("requests", jsonArray);
            if (count == 0) {
                count = count + 1;
                boolean stat = utils.networkIsOnline(this);
                if (stat == true) {
                    Retrofit.Builder builder = new Retrofit.Builder()
                            .baseUrl("https://vision.googleapis.com/v1/images:annotate/")
                            .addConverterFactory(GsonConverterFactory.create());
                    Retrofit retrofit = builder.build();
                    apiInterface apiInterface = retrofit.create(apiInterface.class);
                    Call<VisionModel> call = apiInterface.visionApi(request);
                    call.enqueue(new Callback<VisionModel>() {
                        @Override
                        public void onResponse(Call<VisionModel> call, Response<VisionModel> response) {
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    try {
                                        Log.i("RestAPI", "VISION " + response.body().getResponses().get(0).getFullTextAnnotation().getText());
                                        extractText = response.body().getResponses().get(0).getFullTextAnnotation().getText();
                                        translateAPI(extractText);
                                    } catch (Exception e) {
                                        Toast.makeText(chatHeadService.this, "Try Again " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                                    }

                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<VisionModel> call, Throwable t) {
                            Log.i("TAG", "onFailure: " + t.getLocalizedMessage());
                            Toast.makeText(chatHeadService.this, "Try Again " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "No internet Connection", Toast.LENGTH_SHORT).show();
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void translateAPI(String extractText) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://translation.googleapis.com/language/translate/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        apiInterface apiInterface = retrofit.create(apiInterface.class);
        Call<Model> call = apiInterface.translateApi(key, extractText, sharedPreferences.getString("targetLanguage", ""));
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("REST API", "onResponse: TRANSLATE" + response.body().getData().getTranslations().get(0).getTranslatedText());
                        if (response.body().getData().getTranslations().get(0).getTranslatedText() != null) {
                            translatedText = response.body().getData().getTranslations().get(0).getTranslatedText();

                            createViewForResult();

                        }


                    }

                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                Log.i("API TRANSLATE", "onFailure: " + t);
                Toast.makeText(chatHeadService.this, "Try again " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void createViewForResult() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (usageAccessGranted(context)) {
                getCurrentAppForegound();
                if (defaultHomePackageName.equalsIgnoreCase(currentForegroundPackageName)) {
                    Toast.makeText(context, "No App is in Foreground", Toast.LENGTH_LONG).show();
                } else {
                    inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

                    int LAYOUT_FLAG;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    } else {
                        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
                    }

                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                            LAYOUT_FLAG,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSPARENT);

                    params.gravity = Gravity.END;
                    view = inflater.inflate(R.layout.result_layout, null);
                    view2 = view.getRootView();

                    LinearLayout top = view.findViewById(R.id.TOP);
                    LinearLayout bottom = view.findViewById(R.id.BOTTOM);
                    ImageView closeBT = view.findViewById(R.id.closeBT);
                    resultTV = view.findViewById(R.id.resultTV);
                    ImageView swap = view.findViewById(R.id.swap);
                    textToSpeak = view.findViewById(R.id.textToSpeak);
                    ImageView addToFav = view.findViewById(R.id.addToFav);
                    ImageView share = view.findViewById(R.id.share);


                    bottom.setOnClickListener(this);
                    top.setOnClickListener(this);
                    closeBT.setOnClickListener(this);
                    swap.setOnClickListener(this);
                    textToSpeak.setOnClickListener(this);
                    addToFav.setOnClickListener(this);
                    share.setOnClickListener(this);

                    //translatedText to be shown to user
                    resultTV.setText(translatedText);
                    windowManager.addView(view, params);

                }
            } else {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
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

    public void startCapture() {

        projection = mgr.getMediaProjection(resultCode, resultData);

        it = new ImageTransmogrifier(this);
        MediaProjection.Callback cb = null;

        cb = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                vdisplay.release();
            }
        };
        vdisplay = projection.createVirtualDisplay("com.sanjaysgangwar.universaltranslator",
                it.getWidth(), it.getHeight(),
                getResources().getDisplayMetrics().densityDpi
                , VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
        projection.registerCallback(cb, handler);


    }

    public WindowManager getWindowManager() {
        return (windowManager);
    }

    public Handler getHandler() {
        return (handler);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.TOP:
            case R.id.BOTTOM:
            case R.id.closeBT:
                resultVisibilityOff();
                break;
            case R.id.swap:
                Ccounter = Ccounter + 1;
                if (Ccounter % 2 == 0) {
                    resultTV.setText(extractText);

                } else if (Ccounter % 2 == 1) {
                    resultTV.setText(translatedText);
                }

               /* String get=resultTV.getText().toString();
                if(get.equals(translatedText)){
                    resultTV.setText(extractText);
                }
                else if (get.equals(extractText)){
                    resultTV.setText(translatedText);
                }
                else{

                    Toast.makeText(this, ""+get, Toast.LENGTH_SHORT).show();
                }*/
               /* if (translatedText.contentEquals(resultTV.getText())) {

                } else if (extractText.contentEquals(resultTV.getText())) {
                    resultTV.setText(translatedText);
                }*/

                break;
            case R.id.textToSpeak:
                if (tss != null) {
                    if (tss.isSpeaking()) {
                        tss.stop();
                    } else {
                        tss.speak(resultTV.getText().toString().trim(), TextToSpeech.QUEUE_FLUSH, null);

                    }
                }

                break;
            case R.id.addToFav:
                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(resultTV.getText());
                Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share:
                String url = "http://mysuperwebsite";
                try {
                    Intent i = new Intent("android.intent.action.MAIN");
                    i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
                    i.addCategory("android.intent.category.LAUNCHER");
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    // Chrome is not installed
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
               /* String data = resultTV.getText().toString().trim();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, data);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                this.getApplication().getApplicationContext().startActivity(shareIntent);
                resultVisibilityOff();*/
                break;
        }
    }

    private void resultVisibilityOff() {
        if (view2 != null) {
            if (view2.getVisibility() == View.VISIBLE) {
                if (tss != null) {
                    if (tss.isSpeaking()) {
                        tss.stop();
                    }
                }
                hideViews2();
            }
        }
    }

    private void hideViews2() {
        if (windowManager != null) {
            if (view2.isAttachedToWindow()) {
                windowManager.removeView(view2);
                windowManager = null;

            }
        }
    }
}
