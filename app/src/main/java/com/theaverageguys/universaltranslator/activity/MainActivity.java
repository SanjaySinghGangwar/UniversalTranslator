package com.theaverageguys.universaltranslator.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theaverageguys.universaltranslator.R;
import com.theaverageguys.universaltranslator.imageProcessing.google;
import com.theaverageguys.universaltranslator.modelClasses.AppSharePreference;
import com.theaverageguys.universaltranslator.sevices.chatHeadService;
import com.theaverageguys.universaltranslator.sevices.myProgressView;
import com.theaverageguys.universaltranslator.sevices.myToast;
import com.theaverageguys.universaltranslator.sevices.utils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

import static com.theaverageguys.universaltranslator.imageProcessing.google.sentImageToServer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final int REQUEST_SCREENSHOT = 59706;
    private static String pictureFilePathCheckin = "";
    int viaCameraCode = 100, viaGalleryCode = 101, chatHead = 102;
    @BindView(R.id.cameraButton)
    CardView cameraButton;
    @BindView(R.id.viaText)
    CardView viaText;
    @BindView(R.id.viaGallery)
    CardView viaGallery;
    @BindView(R.id.viaPhone)
    CardView viaPhone;
    @BindView(R.id.setLanguage)
    CardView setLanguage;
    @BindView(R.id.download)
    CardView download;
    String languageSelected;
    String targetLanguage;
    myProgressView myProgressView;
    AppSharePreference appSharePreference;
    private MediaProjectionManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initListener();
        myProgressView = new myProgressView(this);
        appSharePreference = new AppSharePreference(this);


    }

    private void initListener() {
        cameraButton.setOnClickListener(this);
        cameraButton.setOnLongClickListener(this);
        viaText.setOnClickListener(this);
        viaText.setOnLongClickListener(this);
        viaGallery.setOnClickListener(this);
        viaGallery.setOnLongClickListener(this);
        viaPhone.setOnClickListener(this);
        viaPhone.setOnLongClickListener(this);
        download.setOnClickListener(this);
        download.setOnLongClickListener(this);
        setLanguage.setOnClickListener(this);
        setLanguage.setOnLongClickListener(this);


        mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        if (chatHeadService.resultData == null) {
            startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
        }
    }

    private File getPictureFileCheckin() throws IOException {
        String timeStamp = utils.currentTimeStamp();
        File storageDir = this.getExternalFilesDir(null);
        File image = File.createTempFile(timeStamp, ".png", storageDir);
        MainActivity.pictureFilePathCheckin = image.getAbsolutePath();
        return image;
    }

    private void cameraOpen() throws IOException {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File pictureFileCheckin = getPictureFileCheckin();
        Uri photoURI = FileProvider.getUriForFile(this, "com.theaverageguys.universaltranslator.fileprovider", pictureFileCheckin);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(cameraIntent, viaCameraCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == viaCameraCode) {
                File imageFile = new File(pictureFilePathCheckin);
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(imageFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                croper(Uri.fromFile(imageFile));

            } else if (requestCode == viaGalleryCode) {
                croper(data.getData());
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    if (utils.networkIsOnline(this)) {
                        sentImageToServer(new File(result.getUri().getPath()), getApplicationContext());
                    } else {
                        myToast.showRed(this, "No Internet Connection");
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this, "" + result.getError(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == chatHead) {
                openChatHead();
            } else if (requestCode == REQUEST_SCREENSHOT) {
                chatHeadService.resultCode = resultCode;
                chatHeadService.resultData = (Intent) data.clone();
            }
        } else {
            myToast.showRed(this, "Give it a chance");
        }

    }

    private void croper(Uri image) {
        CropImage.activity(image)
                .setOutputCompressQuality(100)
                .start(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setLanguage:
                if (appSharePreference.getSelectedLanguage().isEmpty()) {
                    dialogForLanguage();
                } else {
                    MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
                    View view2 = getLayoutInflater().inflate(R.layout.popup_warning, null);
                    alert.setView(view2);
                    final AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                    Button yes = view2.findViewById(R.id.yesBT);
                    Button no = view2.findViewById(R.id.noBt);

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            appSharePreference.clearPreferences();
                            dialogForLanguage();
                        }
                    });
                    no.setOnClickListener(view1 -> alertDialog.dismiss());
                }
                break;
            case R.id.download:
                installVoiceData();
                break;
            case R.id.cameraButton:
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 10);
                    } else {
                        if (appSharePreference.getSelectedLanguage().isEmpty()) {
                            dialogForLanguage();
                        } else {
                            cameraOpen();
                        }
                    }
                } catch (IOException ex) {
                    myToast.showRed(this, "Photo file can't be saved, please try again");
                }
                break;
            case R.id.viaGallery:
                if (appSharePreference.getSelectedLanguage().isEmpty()) {
                    dialogForLanguage();
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(i, "Select Image"), viaGalleryCode);
                }
                break;
            case R.id.viaPhone:
                if (appSharePreference.getSelectedLanguage().isEmpty()) {
                    dialogForLanguage();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, chatHead);
                    } else {
                        openChatHead();
                    }
                }
                break;
            case R.id.viaText:
                MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(MainActivity.this);
                View Dialogview = getLayoutInflater().inflate(R.layout.popup_translate, null);
                final EditText translateText = Dialogview.findViewById(R.id.translateET);
                Button translateBT = Dialogview.findViewById(R.id.TranslateBT);
                alert.setView(Dialogview);
                final AlertDialog alertDialog = alert.create();
                alertDialog.show();
                SearchableSpinner searchableSpinner = Dialogview.findViewById(R.id.languageSpinner);
                searchableSpinner.setTitle("Select language in which it has to converted");
                searchableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (i) {
                            case 0:
                                targetLanguage = "af";
                                languageSelected = "Afrikaans";
                                break;
                            case 1:
                                targetLanguage = "sq";
                                languageSelected = "Albanian";
                                break;
                            case 2:
                                targetLanguage = "am";
                                languageSelected = "Amharic";
                                break;
                            case 3:
                                targetLanguage = "ar";
                                languageSelected = "Arabic";
                                break;
                            case 4:
                                targetLanguage = "hy";
                                languageSelected = "Armenian";
                                break;
                            case 5:
                                targetLanguage = "az";
                                languageSelected = "Azerbaijani";
                                break;
                            case 6:
                                targetLanguage = "eu";
                                languageSelected = "Basque";
                                break;
                            case 7:
                                targetLanguage = "be";
                                languageSelected = "Belarusian";
                                break;
                            case 8:
                                targetLanguage = "bn";
                                languageSelected = "Bengali";
                                break;
                            case 9:
                                targetLanguage = "bs";
                                languageSelected = "Bosnian";
                                break;
                            case 10:
                                targetLanguage = "bg";
                                languageSelected = "Bulgarian";
                                break;
                            case 11:
                                targetLanguage = "ca";
                                languageSelected = "Catalan";
                                break;
                            case 12:
                                targetLanguage = "ceb";
                                languageSelected = "Cebuano";
                                break;
                            case 13:
                                targetLanguage = "zh-CN";
                                languageSelected = "Chinese (Simplified)";
                                break;
                            case 14:
                                targetLanguage = "zh-TW";
                                languageSelected = "Chinese (Traditional)";
                                break;
                            case 15:
                                targetLanguage = "co";
                                languageSelected = "Corsican";
                                break;
                            case 16:
                                targetLanguage = "hr";
                                languageSelected = "Croatian";
                                break;
                            case 17:
                                targetLanguage = "cs";
                                languageSelected = "Czech";
                                break;
                            case 18:
                                targetLanguage = "da";
                                languageSelected = "Danish";
                                break;
                            case 19:
                                targetLanguage = "nl";
                                languageSelected = "Dutch";
                                break;
                            case 20:
                                targetLanguage = "en";
                                languageSelected = "English";
                                break;
                            case 21:
                                targetLanguage = "eo";
                                languageSelected = "Esperanto";
                                break;
                            case 22:
                                targetLanguage = "et";
                                languageSelected = "Estonian";
                                break;
                            case 23:
                                targetLanguage = "fi";
                                languageSelected = "Finnish";
                                break;
                            case 24:
                                targetLanguage = "fr";
                                languageSelected = "French";
                                break;
                            case 25:
                                targetLanguage = "fy";
                                languageSelected = "Frisian";
                                break;
                            case 26:
                                targetLanguage = "gl";
                                languageSelected = "Galician";
                                break;
                            case 27:
                                targetLanguage = "ka";
                                languageSelected = "Georgian";
                                break;
                            case 28:
                                targetLanguage = "de";
                                languageSelected = "German";
                                break;
                            case 29:
                                targetLanguage = "el";
                                languageSelected = "Greek";
                                break;
                            case 30:
                                targetLanguage = "gu";
                                languageSelected = "Gujarati";
                                break;
                            case 31:
                                targetLanguage = "ht";
                                languageSelected = "Haitian Creole";
                                break;
                            case 32:
                                targetLanguage = "ha";
                                languageSelected = "Hausa";
                                break;
                            case 33:
                                targetLanguage = "haw";
                                languageSelected = "Hawaiian";
                                break;
                            case 34:
                                targetLanguage = "he";
                                languageSelected = "Hebrew";
                                break;
                            case 35:
                                targetLanguage = "hi";
                                languageSelected = "Hindi";
                                break;
                            case 36:
                                targetLanguage = "hmn";
                                languageSelected = "Hmong";
                                break;
                            case 37:
                                targetLanguage = "hu";
                                languageSelected = "Hungarian";
                                break;
                            case 38:
                                targetLanguage = "is";
                                languageSelected = "Icelandic";
                                break;
                            case 39:
                                targetLanguage = "ig";
                                languageSelected = "Igbo";
                                break;
                            case 40:
                                targetLanguage = "id";
                                languageSelected = "Indonesian";
                                break;
                            case 41:
                                targetLanguage = "ga";
                                languageSelected = "Irish";
                                break;
                            case 42:
                                targetLanguage = "it";
                                languageSelected = "Italian";
                                break;
                            case 43:
                                targetLanguage = "ja";
                                languageSelected = "Javanese";
                                break;
                            case 44:
                                targetLanguage = "jv";
                                languageSelected = "Javanese";
                                break;
                            case 45:
                                targetLanguage = "kn";
                                languageSelected = "Kannada";
                                break;
                            case 46:
                                targetLanguage = "kk";
                                languageSelected = "Kazakh";
                                break;
                            case 47:
                                targetLanguage = "km";
                                languageSelected = "Khmer";
                                break;
                            case 48:
                                targetLanguage = "rw";
                                languageSelected = "Kinyarwanda";
                                break;
                            case 49:
                                targetLanguage = "ko";
                                languageSelected = "Korean";
                                break;
                            case 50:
                                targetLanguage = "ku";
                                languageSelected = "Kurdish";
                                break;
                            case 51:
                                targetLanguage = "ky";
                                languageSelected = "Kyrgyz";
                                break;
                            case 52:
                                targetLanguage = "lo";
                                languageSelected = "Lao";
                                break;
                            case 53:
                                targetLanguage = "la";
                                languageSelected = "Latin";
                                break;
                            case 54:
                                targetLanguage = "lv";
                                languageSelected = "Latvian";
                                break;
                            case 55:
                                targetLanguage = "lt";
                                languageSelected = "Lithuanian";
                                break;
                            case 56:
                                targetLanguage = "lb";
                                languageSelected = "Luxembourgish";
                                break;
                            case 57:
                                targetLanguage = "mk";
                                languageSelected = "Macedonian";
                                break;
                            case 58:
                                targetLanguage = "mg";
                                languageSelected = "Malagasy";
                                break;
                            case 59:
                                targetLanguage = "ms";
                                languageSelected = "Malay";
                                break;
                            case 60:
                                targetLanguage = "ml";
                                languageSelected = "Malayalam";
                                break;
                            case 61:
                                targetLanguage = "mt";
                                languageSelected = "Maltese";
                                break;
                            case 62:
                                targetLanguage = "mi";
                                languageSelected = "Maori";
                                break;
                            case 63:
                                targetLanguage = "mr";
                                languageSelected = "Marathi";
                                break;
                            case 64:
                                targetLanguage = "mn";
                                languageSelected = "Mongolian";
                                break;
                            case 65:
                                targetLanguage = "my";
                                languageSelected = "Myanmar (Burmese)";
                                break;
                            case 66:
                                targetLanguage = "ne";
                                languageSelected = "Nepali";
                                break;
                            case 67:
                                targetLanguage = "no";
                                languageSelected = "Norwegian";
                                break;
                            case 68:
                                targetLanguage = "ny";
                                languageSelected = "Nyanja (Chichewa)";
                                break;
                            case 69:
                                targetLanguage = "or";
                                languageSelected = "Odia (Oriya)";
                                break;
                            case 70:
                                targetLanguage = "ps";
                                languageSelected = "Pashto";
                                break;
                            case 71:
                                targetLanguage = "fa";
                                languageSelected = "Persian";
                                break;
                            case 72:
                                targetLanguage = "pl";
                                languageSelected = "Polish";
                                break;
                            case 73:
                                targetLanguage = "pt";
                                languageSelected = "Portuguese (Portugal, Brazil)";
                                break;
                            case 74:
                                targetLanguage = "pa";
                                languageSelected = "Punjabi";
                                break;
                            case 75:
                                targetLanguage = "ro";
                                languageSelected = "Romanian";
                                break;
                            case 76:
                                targetLanguage = "ru";
                                languageSelected = "Russian";
                                break;
                            case 77:
                                targetLanguage = "sm";
                                languageSelected = "Samoan";
                                break;
                            case 78:
                                targetLanguage = "gd";
                                languageSelected = "Scots Gaelic";
                                break;
                            case 79:
                                targetLanguage = "sr";
                                languageSelected = "Serbian";
                                break;
                            case 80:
                                targetLanguage = "st";
                                languageSelected = "Sesotho";
                                break;
                            case 81:
                                targetLanguage = "sn";
                                languageSelected = "Shona";
                                break;
                            case 82:
                                targetLanguage = "sd";
                                languageSelected = "Sindhi";
                                break;
                            case 83:
                                targetLanguage = "si";
                                languageSelected = "Sinhala (Sinhalese)";
                                break;
                            case 84:
                                targetLanguage = "sk";
                                languageSelected = "Slovak";
                                break;
                            case 85:
                                targetLanguage = "sl";
                                languageSelected = "Slovenian";
                                break;
                            case 86:
                                targetLanguage = "so";
                                languageSelected = "Somali";
                                break;
                            case 87:
                                targetLanguage = "es";
                                languageSelected = "Spanish";
                                break;
                            case 88:
                                targetLanguage = "su";
                                languageSelected = "Sundanese";
                                break;
                            case 89:
                                targetLanguage = "sw";
                                languageSelected = "Swahili";
                                break;
                            case 90:
                                targetLanguage = "sv";
                                languageSelected = "Swedish";
                                break;
                            case 91:
                                targetLanguage = "tl";
                                languageSelected = "Tagalog (Filipino)";
                                break;
                            case 92:
                                targetLanguage = "tg";
                                languageSelected = "Tajik";
                                break;
                            case 93:
                                targetLanguage = "ta";
                                languageSelected = "Tamil";
                                break;
                            case 94:
                                targetLanguage = "tt";
                                languageSelected = "Tatar";
                                break;
                            case 95:
                                targetLanguage = "te";
                                languageSelected = "Telugu";
                                break;
                            case 96:
                                targetLanguage = "th";
                                languageSelected = "Thai";
                                break;
                            case 97:
                                targetLanguage = "tr";
                                languageSelected = "Turkish";
                                break;
                            case 98:
                                targetLanguage = "tk";
                                languageSelected = "Turkmen";
                                break;
                            case 99:
                                targetLanguage = "uk";
                                languageSelected = "Ukrainian";
                                break;
                            case 100:
                                targetLanguage = "ur";
                                languageSelected = "Urdu";
                                break;
                            case 101:
                                targetLanguage = "ug";
                                languageSelected = "Uyghur";
                                break;
                            case 102:
                                targetLanguage = "uz";
                                languageSelected = "Uzbek";
                                break;
                            case 103:
                                targetLanguage = "vi";
                                languageSelected = "Vietnamese";
                                break;
                            case 104:
                                targetLanguage = "cy";
                                languageSelected = "Welsh";
                                break;
                            case 105:
                                targetLanguage = "xh";
                                languageSelected = "Xhosa";
                                break;
                            case 106:
                                targetLanguage = "yi";
                                languageSelected = "Yiddish";
                                break;
                            case 107:
                                targetLanguage = "yo";
                                languageSelected = "Yoruba";
                                break;
                            case 108:
                                targetLanguage = "zu";
                                languageSelected = "Zulu";
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                translateBT.setOnClickListener(view1 -> {
                    String dataToTranslate = translateText.getText().toString().trim();
                    if (dataToTranslate.isEmpty()) {
                        translateText.setText(R.string.Enter_text_here);
                    } else {
                        alertDialog.dismiss();
                        if (utils.networkIsOnline(MainActivity.this)) {
                            google.translateAPI(dataToTranslate, getApplicationContext());
                        } else {
                            myToast.showRed(MainActivity.this, "No Internet Connection");
                        }

                    }
                });
                break;

        }
    }

    private void installVoiceData() {
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.google.android.tts"/*replace with the package name of the target TTS engine*/);
        String TAG = "TTS";
        try {
            Log.v(TAG, "Installing voice data: " + intent.toUri(0));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to install TTS data, no acitivty found for " + intent + ")");
        }
    }

    private void openChatHead() {
        showFloatingView(MainActivity.this, true, false);
        //startService(new Intent(MainActivity.this, ChatHeadService.class));
        //finish();
    }

    private void showFloatingView(Activity activity, boolean isShowOverlayPermission, boolean isCustomFloatingView) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startFloatingViewService(this, isCustomFloatingView);
            return;
        }
        if (Settings.canDrawOverlays(activity)) {
            startFloatingViewService(this, isCustomFloatingView);
            return;
        }
    }

    private void startFloatingViewService(Activity activity, boolean isCustomFloatingView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (activity.getWindow().getAttributes().layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER) {
                throw new RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'");
            }
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                throw new RuntimeException("Do not set Activity to landscape");
            }
        }
        Intent intent = new Intent(activity, chatHeadService.class);
        intent.putExtra(chatHeadService.EXTRA_CUTOUT_SAFE_AREA, FloatingViewManager.findCutoutSafeArea(activity));
        ContextCompat.startForegroundService(activity, intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.targetLanguage:
                if (appSharePreference.getSelectedLanguage().isEmpty()) {
                    dialogForLanguage();
                } else {
                    MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
                    View view = getLayoutInflater().inflate(R.layout.popup_warning, null);
                    alert.setView(view);
                    final AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                    Button yes = view.findViewById(R.id.yesBT);
                    Button no = view.findViewById(R.id.noBt);

                    yes.setOnClickListener(view12 -> {
                        alertDialog.dismiss();
                        appSharePreference.clearPreferences();
                        dialogForLanguage();
                    });
                    no.setOnClickListener(view1 -> alertDialog.dismiss());
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dialogForLanguage() {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_language, null);
        alert.setView(view);
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();
        SearchableSpinner searchableSpinner = view.findViewById(R.id.languageSpinner);
        searchableSpinner.setTitle("Select language in which it has to converted");
        searchableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        targetLanguage = "af";
                        languageSelected = "Afrikaans";
                        break;
                    case 1:
                        targetLanguage = "sq";
                        languageSelected = "Albanian";
                        break;
                    case 2:
                        targetLanguage = "am";
                        languageSelected = "Amharic";
                        break;
                    case 3:
                        targetLanguage = "ar";
                        languageSelected = "Arabic";
                        break;
                    case 4:
                        targetLanguage = "hy";
                        languageSelected = "Armenian";
                        break;
                    case 5:
                        targetLanguage = "az";
                        languageSelected = "Azerbaijani";
                        break;
                    case 6:
                        targetLanguage = "eu";
                        languageSelected = "Basque";
                        break;
                    case 7:
                        targetLanguage = "be";
                        languageSelected = "Belarusian";
                        break;
                    case 8:
                        targetLanguage = "bn";
                        languageSelected = "Bengali";
                        break;
                    case 9:
                        targetLanguage = "bs";
                        languageSelected = "Bosnian";
                        break;
                    case 10:
                        targetLanguage = "bg";
                        languageSelected = "Bulgarian";
                        break;
                    case 11:
                        targetLanguage = "ca";
                        languageSelected = "Catalan";
                        break;
                    case 12:
                        targetLanguage = "ceb";
                        languageSelected = "Cebuano";
                        break;
                    case 13:
                        targetLanguage = "zh-CN";
                        languageSelected = "Chinese (Simplified)";
                        break;
                    case 14:
                        targetLanguage = "zh-TW";
                        languageSelected = "Chinese (Traditional)";
                        break;
                    case 15:
                        targetLanguage = "co";
                        languageSelected = "Corsican";
                        break;
                    case 16:
                        targetLanguage = "hr";
                        languageSelected = "Croatian";
                        break;
                    case 17:
                        targetLanguage = "cs";
                        languageSelected = "Czech";
                        break;
                    case 18:
                        targetLanguage = "da";
                        languageSelected = "Danish";
                        break;
                    case 19:
                        targetLanguage = "nl";
                        languageSelected = "Dutch";
                        break;
                    case 20:
                        targetLanguage = "en";
                        languageSelected = "English";
                        break;
                    case 21:
                        targetLanguage = "eo";
                        languageSelected = "Esperanto";
                        break;
                    case 22:
                        targetLanguage = "et";
                        languageSelected = "Estonian";
                        break;
                    case 23:
                        targetLanguage = "fi";
                        languageSelected = "Finnish";
                        break;
                    case 24:
                        targetLanguage = "fr";
                        languageSelected = "French";
                        break;
                    case 25:
                        targetLanguage = "fy";
                        languageSelected = "Frisian";
                        break;
                    case 26:
                        targetLanguage = "gl";
                        languageSelected = "Galician";
                        break;
                    case 27:
                        targetLanguage = "ka";
                        languageSelected = "Georgian";
                        break;
                    case 28:
                        targetLanguage = "de";
                        languageSelected = "German";
                        break;
                    case 29:
                        targetLanguage = "el";
                        languageSelected = "Greek";
                        break;
                    case 30:
                        targetLanguage = "gu";
                        languageSelected = "Gujarati";
                        break;
                    case 31:
                        targetLanguage = "ht";
                        languageSelected = "Haitian Creole";
                        break;
                    case 32:
                        targetLanguage = "ha";
                        languageSelected = "Hausa";
                        break;
                    case 33:
                        targetLanguage = "haw";
                        languageSelected = "Hawaiian";
                        break;
                    case 34:
                        targetLanguage = "he";
                        languageSelected = "Hebrew";
                        break;
                    case 35:
                        targetLanguage = "hi";
                        languageSelected = "Hindi";
                        break;
                    case 36:
                        targetLanguage = "hmn";
                        languageSelected = "Hmong";
                        break;
                    case 37:
                        targetLanguage = "hu";
                        languageSelected = "Hungarian";
                        break;
                    case 38:
                        targetLanguage = "is";
                        languageSelected = "Icelandic";
                        break;
                    case 39:
                        targetLanguage = "ig";
                        languageSelected = "Igbo";
                        break;
                    case 40:
                        targetLanguage = "id";
                        languageSelected = "Indonesian";
                        break;
                    case 41:
                        targetLanguage = "ga";
                        languageSelected = "Irish";
                        break;
                    case 42:
                        targetLanguage = "it";
                        languageSelected = "Italian";
                        break;
                    case 43:
                        targetLanguage = "ja";
                        languageSelected = "Javanese";
                        break;
                    case 44:
                        targetLanguage = "jv";
                        languageSelected = "Javanese";
                        break;
                    case 45:
                        targetLanguage = "kn";
                        languageSelected = "Kannada";
                        break;
                    case 46:
                        targetLanguage = "kk";
                        languageSelected = "Kazakh";
                        break;
                    case 47:
                        targetLanguage = "km";
                        languageSelected = "Khmer";
                        break;
                    case 48:
                        targetLanguage = "rw";
                        languageSelected = "Kinyarwanda";
                        break;
                    case 49:
                        targetLanguage = "ko";
                        languageSelected = "Korean";
                        break;
                    case 50:
                        targetLanguage = "ku";
                        languageSelected = "Kurdish";
                        break;
                    case 51:
                        targetLanguage = "ky";
                        languageSelected = "Kyrgyz";
                        break;
                    case 52:
                        targetLanguage = "lo";
                        languageSelected = "Lao";
                        break;
                    case 53:
                        targetLanguage = "la";
                        languageSelected = "Latin";
                        break;
                    case 54:
                        targetLanguage = "lv";
                        languageSelected = "Latvian";
                        break;
                    case 55:
                        targetLanguage = "lt";
                        languageSelected = "Lithuanian";
                        break;
                    case 56:
                        targetLanguage = "lb";
                        languageSelected = "Luxembourgish";
                        break;
                    case 57:
                        targetLanguage = "mk";
                        languageSelected = "Macedonian";
                        break;
                    case 58:
                        targetLanguage = "mg";
                        languageSelected = "Malagasy";
                        break;
                    case 59:
                        targetLanguage = "ms";
                        languageSelected = "Malay";
                        break;
                    case 60:
                        targetLanguage = "ml";
                        languageSelected = "Malayalam";
                        break;
                    case 61:
                        targetLanguage = "mt";
                        languageSelected = "Maltese";
                        break;
                    case 62:
                        targetLanguage = "mi";
                        languageSelected = "Maori";
                        break;
                    case 63:
                        targetLanguage = "mr";
                        languageSelected = "Marathi";
                        break;
                    case 64:
                        targetLanguage = "mn";
                        languageSelected = "Mongolian";
                        break;
                    case 65:
                        targetLanguage = "my";
                        languageSelected = "Myanmar (Burmese)";
                        break;
                    case 66:
                        targetLanguage = "ne";
                        languageSelected = "Nepali";
                        break;
                    case 67:
                        targetLanguage = "no";
                        languageSelected = "Norwegian";
                        break;
                    case 68:
                        targetLanguage = "ny";
                        languageSelected = "Nyanja (Chichewa)";
                        break;
                    case 69:
                        targetLanguage = "or";
                        languageSelected = "Odia (Oriya)";
                        break;
                    case 70:
                        targetLanguage = "ps";
                        languageSelected = "Pashto";
                        break;
                    case 71:
                        targetLanguage = "fa";
                        languageSelected = "Persian";
                        break;
                    case 72:
                        targetLanguage = "pl";
                        languageSelected = "Polish";
                        break;
                    case 73:
                        targetLanguage = "pt";
                        languageSelected = "Portuguese (Portugal, Brazil)";
                        break;
                    case 74:
                        targetLanguage = "pa";
                        languageSelected = "Punjabi";
                        break;
                    case 75:
                        targetLanguage = "ro";
                        languageSelected = "Romanian";
                        break;
                    case 76:
                        targetLanguage = "ru";
                        languageSelected = "Russian";
                        break;
                    case 77:
                        targetLanguage = "sm";
                        languageSelected = "Samoan";
                        break;
                    case 78:
                        targetLanguage = "gd";
                        languageSelected = "Scots Gaelic";
                        break;
                    case 79:
                        targetLanguage = "sr";
                        languageSelected = "Serbian";
                        break;
                    case 80:
                        targetLanguage = "st";
                        languageSelected = "Sesotho";
                        break;
                    case 81:
                        targetLanguage = "sn";
                        languageSelected = "Shona";
                        break;
                    case 82:
                        targetLanguage = "sd";
                        languageSelected = "Sindhi";
                        break;
                    case 83:
                        targetLanguage = "si";
                        languageSelected = "Sinhala (Sinhalese)";
                        break;
                    case 84:
                        targetLanguage = "sk";
                        languageSelected = "Slovak";
                        break;
                    case 85:
                        targetLanguage = "sl";
                        languageSelected = "Slovenian";
                        break;
                    case 86:
                        targetLanguage = "so";
                        languageSelected = "Somali";
                        break;
                    case 87:
                        targetLanguage = "es";
                        languageSelected = "Spanish";
                        break;
                    case 88:
                        targetLanguage = "su";
                        languageSelected = "Sundanese";
                        break;
                    case 89:
                        targetLanguage = "sw";
                        languageSelected = "Swahili";
                        break;
                    case 90:
                        targetLanguage = "sv";
                        languageSelected = "Swedish";
                        break;
                    case 91:
                        targetLanguage = "tl";
                        languageSelected = "Tagalog (Filipino)";
                        break;
                    case 92:
                        targetLanguage = "tg";
                        languageSelected = "Tajik";
                        break;
                    case 93:
                        targetLanguage = "ta";
                        languageSelected = "Tamil";
                        break;
                    case 94:
                        targetLanguage = "tt";
                        languageSelected = "Tatar";
                        break;
                    case 95:
                        targetLanguage = "te";
                        languageSelected = "Telugu";
                        break;
                    case 96:
                        targetLanguage = "th";
                        languageSelected = "Thai";
                        break;
                    case 97:
                        targetLanguage = "tr";
                        languageSelected = "Turkish";
                        break;
                    case 98:
                        targetLanguage = "tk";
                        languageSelected = "Turkmen";
                        break;
                    case 99:
                        targetLanguage = "uk";
                        languageSelected = "Ukrainian";
                        break;
                    case 100:
                        targetLanguage = "ur";
                        languageSelected = "Urdu";
                        break;
                    case 101:
                        targetLanguage = "ug";
                        languageSelected = "Uyghur";
                        break;
                    case 102:
                        targetLanguage = "uz";
                        languageSelected = "Uzbek";
                        break;
                    case 103:
                        targetLanguage = "vi";
                        languageSelected = "Vietnamese";
                        break;
                    case 104:
                        targetLanguage = "cy";
                        languageSelected = "Welsh";
                        break;
                    case 105:
                        targetLanguage = "xh";
                        languageSelected = "Xhosa";
                        break;
                    case 106:
                        targetLanguage = "yi";
                        languageSelected = "Yiddish";
                        break;
                    case 107:
                        targetLanguage = "yo";
                        languageSelected = "Yoruba";
                        break;
                    case 108:
                        targetLanguage = "zu";
                        languageSelected = "Zulu";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Button setLanguage = view.findViewById(R.id.setLanguage);
        setLanguage.setOnClickListener(view1 -> {
            if (targetLanguage.isEmpty() && languageSelected.isEmpty()) {
                Toast.makeText(MainActivity.this, "Select a language", Toast.LENGTH_SHORT).show();
            } else {
                appSharePreference.setTargetLanguageCode(targetLanguage);
                appSharePreference.setSelectedLanguage(languageSelected);
                appSharePreference.setTargetLanguage(targetLanguage);
                alertDialog.dismiss();
                myToast.showGreen(MainActivity.this, "We are good to go");
            }

        });
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.cameraButton:
                Toast.makeText(this, "Via Camera", Toast.LENGTH_SHORT).show();
                break;
            case R.id.viaGallery:
                Toast.makeText(this, "Via Gallery", Toast.LENGTH_SHORT).show();
                break;
            case R.id.viaPhone:
                Toast.makeText(this, "Via PhoneScreen", Toast.LENGTH_SHORT).show();
                break;
            case R.id.viaText:
                Toast.makeText(this, "Via Text", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setLanguage:
                Toast.makeText(this, "Set Language", Toast.LENGTH_SHORT).show();
                break;
            case R.id.download:
                Toast.makeText(this, "Download", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraOpen();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

}