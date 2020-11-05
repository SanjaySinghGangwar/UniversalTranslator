package com.sanjaysgangwar.universaltranslator.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.sanjaysgangwar.universaltranslator.R;
import com.sanjaysgangwar.universaltranslator.sevices.myProgressView;

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

    myProgressView myProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_screen);
        ButterKnife.bind(this);

        initListener();
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
                Toast.makeText(this, "extracted", Toast.LENGTH_SHORT).show();
                break;
            case R.id.translatedLanguageSpeaker:
                Toast.makeText(this, "Transalted", Toast.LENGTH_SHORT).show();
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