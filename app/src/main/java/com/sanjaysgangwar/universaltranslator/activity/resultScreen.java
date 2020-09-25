package com.sanjaysgangwar.universaltranslator.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.sanjaysgangwar.universaltranslator.R;
import com.sanjaysgangwar.universaltranslator.sevices.myProgressView;
import com.sanjaysgangwar.universaltranslator.sevices.myToast;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class resultScreen extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    @BindView(R.id.sourceLanguage)
    TextView sourceLanguage;
    @BindView(R.id.sourceLanguageTv)
    TextView sourceLanguageTv;
    @BindView(R.id.sourceLanguageSpeaker)
    ImageView sourceLanguageSpeaker;
    @BindView(R.id.extractedSourceLanguageSpeaker)
    CardView extractedSourceLanguageSpeaker;
    @BindView(R.id.targetLanguage)
    TextView targetLanguage;
    @BindView(R.id.translatedLanguageTV)
    TextView translatedLanguageTV;
    @BindView(R.id.translatedLanguageSpeaker)
    ImageView translatedLanguageSpeaker;
    String sourceText, translatedText, sourceLocale, languageSelected;
    TextToSpeech tss;
    myProgressView myProgressView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String APP_SHARED_PREFS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_screen);
        ButterKnife.bind(this);

        initListener();
        sharedPref();
        myProgressView = new myProgressView(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sourceText = extras.getString("sourceText");
            translatedText = extras.getString("translatedText");
            sourceLocale = extras.getString("sourceLocale");
            languageSelected = extras.getString("languageSelected");
            sourceLanguage.append(sourceLocale.trim());
            translatedLanguageTV.setText(translatedText.trim());
            sourceLanguageTv.setText(sourceText.trim());
            targetLanguage.setText(languageSelected);
            if (myProgressView.isShowing()) {
                myProgressView.hideLoader();
            }
        } else {
            onBackPressed();
        }

        tss = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int lang = tss.setLanguage(Locale.getDefault());
                if (tss != null) {
                    tss.setVoice(tss.getVoice());
                    tss.setPitch(1);
                    tss.setSpeechRate(0.9f);
                    if (lang == TextToSpeech.LANG_MISSING_DATA
                            || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(resultScreen.this, "Not supported", Toast.LENGTH_SHORT).show();
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    myToast.showRed(resultScreen.this, "Not Supported");
                }

            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
    }

    private void sharedPref() {
        sharedPreferences = this.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        APP_SHARED_PREFS = "Translator";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initListener() {
        translatedLanguageSpeaker.setOnClickListener(this);
        translatedLanguageSpeaker.setOnLongClickListener(this);
        extractedSourceLanguageSpeaker.setOnClickListener(this);
        extractedSourceLanguageSpeaker.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.extractedSourceLanguageSpeaker:
                if (tss != null) {
                    if (tss.isSpeaking()) {
                        tss.stop();
                    } else {
                        tss.speak(sourceText, TextToSpeech.QUEUE_FLUSH, null);

                    }
                }
                break;
            case R.id.translatedLanguageSpeaker:
                if (tss != null) {
                    if (tss.isSpeaking()) {
                        tss.stop();
                    } else {
                        tss.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null);

                    }
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.extractedSourceLanguageSpeaker:
                Toast.makeText(this, "sourceLanguage Long", Toast.LENGTH_SHORT).show();
                break;
            case R.id.translatedLanguageSpeaker:
                Toast.makeText(this, "Translated Long", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
}