package com.sanjaysgangwar.universaltranslator.activity;

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
import com.sanjaysgangwar.universaltranslator.modelClasses.AppSharePreference;
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
    TextToSpeech tssTranslated, tssSource;
    myProgressView myProgressView;
    AppSharePreference appSharePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_screen);
        ButterKnife.bind(this);

        initListener();
        talkToSpeechInit();
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

    }

    private void talkToSpeechInit() {
        tssTranslated = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int lang = tssTranslated.setLanguage(Locale.forLanguageTag(appSharePreference.getTargetLanguageCode())/*Locale.getDefault()*/);/*Locale.getDefault()*//*Locale.forLanguageTag("hi")*/
                tssTranslated.setVoice(tssTranslated.getVoice());
                tssTranslated.setPitch(1);
                tssTranslated.setSpeechRate(0.9f);
                if (lang == TextToSpeech.LANG_MISSING_DATA
                        || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    myToast.showRed(resultScreen.this, "Sorry, Foreign Language not downloaded !!");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
        tssSource = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int lang = tssSource.setLanguage(Locale.forLanguageTag(sourceLocale.trim())/*Locale.getDefault()*/);/*Locale.getDefault()*//*Locale.forLanguageTag("hi")*/
                tssSource.setVoice(tssTranslated.getVoice());
                tssSource.setPitch(1);
                tssSource.setSpeechRate(0.9f);
                if (lang == TextToSpeech.LANG_MISSING_DATA
                        || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    myToast.showRed(resultScreen.this, "Sorry, Your Language not Supported !!");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initListener() {
        appSharePreference = new AppSharePreference(this);
        translatedLanguageSpeaker.setOnClickListener(this);
        translatedLanguageSpeaker.setOnLongClickListener(this);
        extractedSourceLanguageSpeaker.setOnClickListener(this);
        extractedSourceLanguageSpeaker.setOnLongClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.extractedSourceLanguageSpeaker:
                if (tssSource != null) {
                    if (tssSource.isSpeaking()) {
                        tssSource.stop();
                    } else {
                        tssSource.speak(sourceText.trim(), TextToSpeech.QUEUE_FLUSH, null);

                    }
                }
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

    @Override
    protected void onStart() {
        super.onStart();
        if (myProgressView.isShowing()) {
            myProgressView.hideLoader();
        }
    }
}