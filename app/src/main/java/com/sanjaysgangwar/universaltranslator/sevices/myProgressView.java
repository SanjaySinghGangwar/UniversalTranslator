package com.sanjaysgangwar.universaltranslator.sevices;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sanjaysgangwar.universaltranslator.R;

public class myProgressView {
    Activity context2;
    private Dialog dialog;

    public myProgressView(Activity context) {
        this.context2 = context;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context2);
        LayoutInflater inflater = context2.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progress_dialog, null));
        builder.setCancelable(false);
        dialog = builder.create();

    }

    public void showLoader() {
        if (!dialog.isShowing()) {
            dialog.show();
        } else {
            dialog.dismiss();
            dialog.show();
        }
    }

    public void hideLoader() {
        dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }
}