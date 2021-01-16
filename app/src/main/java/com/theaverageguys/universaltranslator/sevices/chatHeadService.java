package com.theaverageguys.universaltranslator.sevices;


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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.theaverageguys.universaltranslator.R;
import com.theaverageguys.universaltranslator.api.apiInterface;
import com.theaverageguys.universaltranslator.crop_slider.CropImageView;
import com.theaverageguys.universaltranslator.modelClasses.AppSharePreference;
import com.theaverageguys.universaltranslator.modelClasses.translateModel.Model;
import com.theaverageguys.universaltranslator.modelClasses.visionModel.VisionModel;

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
import static com.theaverageguys.universaltranslator.sevices.utils.APIkey;

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
    AppSharePreference appSharePreference;
    Context context = chatHeadService.this;
    DisplayMetrics metrics;
    String sourceLocale;
    ImageView crossArrow, tickArrow;
    Bitmap icon;
    CropImageView cropImageView;
    NotificationManager mNotificationManager;
    String timeStamp;
    AppOpsManager appOps;
    UsageStatsManager usm;
    String defaultHomePackageName, currentForegroundPackageName;
    View view2;
    String translatedText;
    String extractText;
    int count;
    int Ccounter;
    TextToSpeech tssTranslated, tssSource;
    int langSource, langTrans;
    myProgressView progressView;
    private FloatingViewManager mFloatingViewManager;
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    private Handler handler;
    private MediaProjectionManager mgr;
    private ImageTransmogrifier it;

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
        progressView = new myProgressView(context);
        mgr = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
        usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);


        metrics = new DisplayMetrics();
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        appSharePreference = new AppSharePreference(this);


    }

    private void talkToSpeechInit() {
        tssTranslated = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                langTrans = tssTranslated.setLanguage(Locale.forLanguageTag(appSharePreference.getTargetLanguageCode())/*Locale.getDefault()*/);/*Locale.getDefault()*//*Locale.forLanguageTag("hi")*/
                tssTranslated.setVoice(tssTranslated.getVoice());
                tssTranslated.setPitch(1);
                tssTranslated.setSpeechRate(0.9f);
                if (langTrans == TextToSpeech.LANG_MISSING_DATA
                        || langTrans == TextToSpeech.LANG_NOT_SUPPORTED) {
                    myToast.showRed(context, "Sorry, Foreign Language not Supported By Text To Speech!!");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
        tssSource = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                langSource = tssSource.setLanguage(Locale.forLanguageTag(sourceLocale.trim())/*Locale.getDefault()*/);/*Locale.getDefault()*//*Locale.forLanguageTag("hi")*/
                tssSource.setVoice(tssSource.getVoice());
                tssSource.setPitch(1);
                tssSource.setSpeechRate(0.9f);
                if (langSource == TextToSpeech.LANG_MISSING_DATA
                        || langSource == TextToSpeech.LANG_NOT_SUPPORTED) {
                    myToast.showRed(context, "Sorry, Your Mother Tongue not Supported By Text To Speech!!");
                }
            } else {
                Log.e("TTS", "Initialization failed");
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

        iconView.setOnClickListener(v -> {

            resultVisibilityOff();
            count = 0;
            Ccounter = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (usageAccessGranted(context)) {

                    getCurrentAppForegound();

                    if (defaultHomePackageName.equalsIgnoreCase(currentForegroundPackageName)) {
                        Toast.makeText(context, "Open App To Read", Toast.LENGTH_LONG).show();
                    } else {


                        createLayoutForServiceClass();
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
        mFloatingViewManager.setActionTrashIconImage(R.drawable.recycle_bin);
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

        crossArrow.setOnClickListener(v -> {
            view.setVisibility(View.GONE);
            iconView.setVisibility(View.VISIBLE);
        });
        tickArrow.setOnClickListener(v -> {

            crossArrow.setVisibility(View.GONE);
            tickArrow.setVisibility(View.GONE);
            startCapture();
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
        String NOTIFICATION_CHANNEL_ID = "com.theaverageguys.universaltranslator";
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
                }
                hideViews();
            }
        });

        stopCapture();

        loadImageFromStorage(getCacheDir());
    }

    private void hideViews() {
        if (windowManager != null) {
            windowManager.removeView(view);
            windowManager = null;
        }

        iconView.setVisibility(View.VISIBLE);
    }

    private void loadImageFromStorage(File path) {
        Toast.makeText(context, "Wait, While we get something for You. ", Toast.LENGTH_LONG).show();
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
                if (utils.networkIsOnline(this)) {
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
                                        myToast.showRed(chatHeadService.this, "Try Again " + e.getLocalizedMessage());
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VisionModel> call, Throwable t) {
                            myToast.showRed(chatHeadService.this, "Try Again " + t.getLocalizedMessage());
                        }
                    });
                } else {
                    myToast.showRed(this, "No internet Connection");
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            //myToast.showRed(this, e.getLocalizedMessage());
        }


    }

    private void translateAPI(String extractText) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://translation.googleapis.com/language/translate/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        apiInterface apiInterface = retrofit.create(apiInterface.class);
        Call<Model> call = apiInterface.translateApi(APIkey, extractText, appSharePreference.getTargetLanguage());
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("REST API", "onResponse: TRANSLATE" + response.body().getData().getTranslations().get(0).getTranslatedText());
                        if (response.body().getData().getTranslations().get(0).getTranslatedText() != null) {
                            sourceLocale = response.body().getData().getTranslations().get(0).getDetectedSourceLanguage();
                            translatedText = response.body().getData().getTranslations().get(0).getTranslatedText();
                            talkToSpeechInit();
                            createViewForResult();
                        }
                    } else {
                        myToast.showRed(getApplicationContext(), "Try Again");
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

                    ImageView closeBT = view.findViewById(R.id.closeBT);
                    TextView sourceText = view.findViewById(R.id.sourceLanguageTv);
                    TextView translateText = view.findViewById(R.id.translatedLanguageTV);
                    TextView targetLanguage = view.findViewById(R.id.targetLanguage);
                    TextView sourceLanguage = view.findViewById(R.id.sourceLanguage);
                    CardView sourceLanguageSpeak = view.findViewById(R.id.extractedSourceLanguageSpeaker);
                    CardView translatedLanguageSpeaker = view.findViewById(R.id.translatedLanguageSpeaker);
                    sourceLanguage.setText("Auto : " + sourceLocale);
                    targetLanguage.setText(appSharePreference.getSelectedLanguage());
                    sourceText.setText(extractText.trim());

                    translateText.setText(translatedText.trim());

                    closeBT.setOnClickListener(this);
                    sourceLanguageSpeak.setOnClickListener(this);
                    translatedLanguageSpeaker.setOnClickListener(this);


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
        vdisplay = projection.createVirtualDisplay("com.theaverageguys.universaltranslator",
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
            case R.id.closeBT:
                resultVisibilityOff();
                break;
            case R.id.translatedLanguageSpeaker:
                if (tssTranslated != null) {
                    if (tssTranslated.isSpeaking()) {
                        tssTranslated.stop();
                    } else {
                        tssTranslated.speak(translatedText.trim(), TextToSpeech.QUEUE_FLUSH, null);

                    }
                }
                break;
            case R.id.extractedSourceLanguageSpeaker:
                if (tssSource != null) {
                    if (tssSource.isSpeaking()) {
                        tssSource.stop();
                    } else {
                        tssSource.speak(extractText.trim(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                break;
        }
    }

    private void resultVisibilityOff() {
        if (view2 != null) {
            if (view2.getVisibility() == View.VISIBLE) {
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
