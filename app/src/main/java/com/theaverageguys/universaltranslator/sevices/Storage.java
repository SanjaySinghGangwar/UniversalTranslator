package com.theaverageguys.universaltranslator.sevices;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Storage {
    static Uri saveToDataDir(Context context, Bitmap b, String type) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String path = Objects.requireNonNull(context.getExternalFilesDir(type)).toString();
        OutputStream fOut;
        File file = new File(path, type + timeStamp + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        try {
            fOut = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush(); // Not really required
            fOut.close();
            Toast.makeText(context, "" + file.getAbsoluteFile(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }
}
