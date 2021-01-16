package com.theaverageguys.universaltranslator.sevices;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import com.theaverageguys.universaltranslator.R;

public class myProgressView {
    Context context;
    private Dialog dialog;

    public myProgressView(Context context) {
        this.context = context;
        dialog = new Dialog(context, R.style.Theme_MaterialComponents_DayNight_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loader);
        dialog.setCancelable(false);
    }

    public void showLoader() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showLoader(String text) {
        TextView textView = dialog.findViewById(R.id.title);
        textView.setText(text);
        if (!dialog.isShowing()) {
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void hideLoader() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

    }

    public boolean isShowing() {
        return dialog.isShowing();
    }
}