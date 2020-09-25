package com.sanjaysgangwar.universaltranslator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.sanjaysgangwar.universaltranslator.activity.MainActivity;

import java.util.Objects;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
            Intent i = new Intent(Splash.this, MainActivity.class);
            startActivity(i);
            finish();
        }, 1500);

    }
}