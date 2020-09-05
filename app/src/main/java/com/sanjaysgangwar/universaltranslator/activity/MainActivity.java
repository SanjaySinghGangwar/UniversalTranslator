package com.sanjaysgangwar.universaltranslator.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.sanjaysgangwar.universaltranslator.R;
import com.sanjaysgangwar.universaltranslator.serverConnection.serverResponse;
import com.sanjaysgangwar.universaltranslator.sevices.myToast;
import com.sanjaysgangwar.universaltranslator.sevices.utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static String pictureFilePathCheckin = "";

    int viaCamera = 100;
    @BindView(R.id.cameraButton)
    CardView cameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        cameraButton.setOnClickListener(this);
        cameraButton.setOnLongClickListener(this);
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
        startActivityForResult(cameraIntent, viaCamera);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == viaCamera && resultCode == RESULT_OK) {
            File imageFile = null;
            imageFile = new File(pictureFilePathCheckin);

            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(imageFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            croper(imageFile);

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                serverResponse.sentImageToGoogleServer(new File(result.getUri().getPath()), this);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "" + result.getError(), Toast.LENGTH_SHORT).show();
            }
        } else {
            myToast.showRed(this, "Closed");
        }
    }

    private void croper(File imageFile) {
        CropImage.activity(Uri.fromFile(imageFile))
                .setOutputCompressQuality(100)
                .start(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                try {
                    cameraOpen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cameraButton:
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 10);
                    } else {
                        cameraOpen();
                    }

                } catch (IOException ex) {
                    myToast.showRed(this, "Photo file can't be saved, please try again");
                }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.cameraButton:
                Toast.makeText(this, "Via Camera", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
}