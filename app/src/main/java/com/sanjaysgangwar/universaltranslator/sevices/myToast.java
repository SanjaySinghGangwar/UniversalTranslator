package com.sanjaysgangwar.universaltranslator.sevices;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.muddzdev.styleabletoast.StyleableToast;

public class myToast {
    public static void showGreen(Context context, String Text) {
        new StyleableToast
                .Builder(context)
                .text(Text)
                .length(Toast.LENGTH_LONG)
                .textColor(Color.WHITE)
                .backgroundColor(Color.GREEN)
                .show();
    }

    public static void showRed(Context context, String Text) {
        new StyleableToast
                .Builder(context)
                .text(Text)
                .length(Toast.LENGTH_LONG)
                .textColor(Color.WHITE)
                .backgroundColor(Color.RED)
                .show();
    }
}
