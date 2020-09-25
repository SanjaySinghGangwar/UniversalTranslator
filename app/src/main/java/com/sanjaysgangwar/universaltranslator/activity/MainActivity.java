package com.sanjaysgangwar.universaltranslator.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sanjaysgangwar.universaltranslator.R;
import com.sanjaysgangwar.universaltranslator.api.apiInterface;
import com.sanjaysgangwar.universaltranslator.modelClasses.translateModel.Model;
import com.sanjaysgangwar.universaltranslator.modelClasses.visionModel.VisionModel;
import com.sanjaysgangwar.universaltranslator.sevices.myProgressView;
import com.sanjaysgangwar.universaltranslator.sevices.myToast;
import com.sanjaysgangwar.universaltranslator.sevices.utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.sanjaysgangwar.universaltranslator.sevices.utils.APIkey;
import static com.sanjaysgangwar.universaltranslator.sevices.utils.networkIsOnline;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static String pictureFilePathCheckin = "";

    int viaCameraCode = 100, viaGalleryCode = 101;
    @BindView(R.id.cameraButton)
    CardView cameraButton;
    @BindView(R.id.viaText)
    CardView viaText;
    @BindView(R.id.viaGallery)
    CardView viaGallery;
    @BindView(R.id.viaPhone)
    CardView viaPhone;
    String languageSelected;
    String targetLanguage;
    myProgressView myProgressView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String APP_SHARED_PREFS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initListener();
        sharedPref();
        myProgressView = new myProgressView(this);


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
    }

    private void sharedPref() {
        sharedPreferences = this.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        APP_SHARED_PREFS = "Translator";
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
        File pictureFileCheckin = null;
        pictureFileCheckin = getPictureFileCheckin();
        Uri photoURI = FileProvider.getUriForFile(this, "com.sanjaysgangwar.universaltranslator.fileprovider", pictureFileCheckin);
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(cameraIntent, viaCameraCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == viaCameraCode && resultCode == RESULT_OK) {
            File imageFile = null;
            imageFile = new File(pictureFilePathCheckin);

            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(imageFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            croper(Uri.fromFile(imageFile));

        } else if (requestCode == viaGalleryCode && resultCode == RESULT_OK) {
            croper(data.getData());
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (utils.networkIsOnline(this)) {
                    sentImageToGoogleServer(new File(result.getUri().getPath()));
                } else {
                    myToast.showRed(this, "No Internet Connection");
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "" + result.getError(), Toast.LENGTH_SHORT).show();
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
            case R.id.cameraButton:
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 10);
                    } else {
                        if (sharedPreferences.getString("SelectedLanguage", "").isEmpty()) {
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
                if (sharedPreferences.getString("SelectedLanguage", "").isEmpty()) {
                    dialogForLanguage();
                } else {
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(i, "Select Image"), viaGalleryCode);
                }
                break;
            case R.id.viaPhone:
                if (sharedPreferences.getString("SelectedLanguage", "").isEmpty()) {
                    dialogForLanguage();
                } else {
                    Toast.makeText(this, "phone S", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.viaText:
                MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(MainActivity.this);
                View Dialogview = getLayoutInflater().inflate(R.layout.popup_translate, null);
                //Components from the view is added
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
                translateBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String dataToTranslate = translateText.getText().toString().trim();
                        if (dataToTranslate.isEmpty()) {
                            translateText.setText("Enter Text here");
                        } else {
                            alertDialog.dismiss();
                            if (utils.networkIsOnline(MainActivity.this)) {
                                translateAPI(dataToTranslate, targetLanguage, languageSelected);
                            } else {
                                myToast.showRed(MainActivity.this, "No Internet Connection");
                            }

                        }
                    }
                });
                break;

        }
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
                if (sharedPreferences.getString("SelectedLanguage", "").isEmpty()) {
                    dialogForLanguage();
                } else {
                    MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
                    View view = getLayoutInflater().inflate(R.layout.popup_warning, null);
                    alert.setView(view);
                    final AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                    Button yes = view.findViewById(R.id.yesBT);
                    Button no = view.findViewById(R.id.noBt);

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            editor.clear();
                            editor.commit();
                            dialogForLanguage();
                        }
                    });
                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
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
        setLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (targetLanguage.isEmpty() && languageSelected.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Select a language", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("targetLanguage", targetLanguage);
                    editor.putString("SelectedLanguage", languageSelected);
                    editor.commit();
                    alertDialog.dismiss();
                    Toast.makeText(MainActivity.this, languageSelected + " is set", Toast.LENGTH_LONG).show();
                }

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

    public void sentImageToGoogleServer(File absoluteFile) {
        myProgressView.showLoader();
        try {
            File f = new File(String.valueOf((absoluteFile)));
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

            boolean stat = networkIsOnline(MainActivity.this);
            if (stat) {
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
                                    String extractText = response.body().getResponses().get(0).getFullTextAnnotation().getText();
                                    translateAPI(extractText, sharedPreferences.getString("targetLanguage", ""), sharedPreferences.getString("SelectedLanguage", ""));
                                } catch (Exception e) {
                                    if (myProgressView.isShowing()) {
                                        myProgressView.hideLoader();
                                    }
                                    myToast.showRed(MainActivity.this, e.getMessage());
                                }

                            }
                            if (myProgressView.isShowing()) {
                                myProgressView.hideLoader();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<VisionModel> call, Throwable t) {
                        if (myProgressView.isShowing()) {
                            myProgressView.hideLoader();
                        }
                        myToast.showRed(MainActivity.this, t.getMessage());
                    }
                });
            } else {
                if (myProgressView.isShowing()) {
                    myProgressView.hideLoader();
                }
                myToast.showRed(MainActivity.this, "No internet Connection");
            }


        } catch (Exception e) {
            if (myProgressView.isShowing()) {
                myProgressView.hideLoader();
            }
            e.printStackTrace();
            myToast.showRed(MainActivity.this, e.getMessage());
        }
    }

    public void translateAPI(String extractText, String targetLanguage, String languageSelected) {
        if (myProgressView.isShowing()) {
        } else {
            myProgressView.showLoader();
        }
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://translation.googleapis.com/language/translate/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        apiInterface apiInterface = retrofit.create(apiInterface.class);
        Call<Model> call = apiInterface.translateApi(APIkey, extractText, targetLanguage);
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("REST API", "onResponse: TRANSLATE" + response.body().getData().getTranslations().get(0).getTranslatedText());
                        if (response.body().getData().getTranslations().get(0).getTranslatedText() != null) {
                            String sourceLocale = response.body().getData().getTranslations().get(0).getDetectedSourceLanguage();
                            String translatedText = response.body().getData().getTranslations().get(0).getTranslatedText();
                            Intent i = new Intent(MainActivity.this, resultScreen.class);
                            i.putExtra("sourceLocale", sourceLocale);
                            i.putExtra("sourceText", extractText);
                            i.putExtra("translatedText", translatedText);
                            i.putExtra("languageSelected", languageSelected);
                            startActivity(i);
                        }


                    }


                }
                if (myProgressView.isShowing()) {
                    myProgressView.hideLoader();
                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                if (myProgressView.isShowing()) {
                    myProgressView.hideLoader();
                }
                Log.i("API TRANSLATE", "onFailure: " + t);
                myToast.showRed(MainActivity.this, "Try again " + t.getLocalizedMessage());
            }
        });


    }
}