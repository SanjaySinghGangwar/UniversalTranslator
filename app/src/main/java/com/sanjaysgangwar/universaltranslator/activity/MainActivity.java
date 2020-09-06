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

    int viaCameraCode = 100, viaGalleryCode = 101;
    @BindView(R.id.cameraButton)
    CardView cameraButton;
    @BindView(R.id.viaText)
    CardView viaText;
    @BindView(R.id.viaGallery)
    CardView viaGallery;
    @BindView(R.id.viaPhone)
    CardView viaPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initListener();

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
                serverResponse.sentImageToGoogleServer(new File(result.getUri().getPath()), this);
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
                        cameraOpen();
                    }

                } catch (IOException ex) {
                    myToast.showRed(this, "Photo file can't be saved, please try again");
                }
                break;
            case R.id.viaGallery:
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Image"), viaGalleryCode);
                break;
            case R.id.viaPhone:
                Toast.makeText(this, "phone S", Toast.LENGTH_SHORT).show();
                break;
            case R.id.viaText:

                MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(MainActivity.this);
                //view is added to access data from the popUpDialog
                View Dialogview = getLayoutInflater().inflate(R.layout.popup_translate, null);

                //Components from the view is added
                final EditText translateText = Dialogview.findViewById(R.id.translateET);
                Button translateBT = Dialogview.findViewById(R.id.TranslateBT);
                alert.setView(Dialogview);
                final AlertDialog alertDialog = alert.create();
                alertDialog.show();
                translateBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String dataToTranslate = translateText.getText().toString().trim();
                        if (dataToTranslate.isEmpty()) {
                            translateText.setText("Enter Text here");
                        } else {
                            alertDialog.dismiss();
                            serverResponse.translateAPI(dataToTranslate, MainActivity.this);
                        }
                    }
                });
                break;

        }
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
                try {
                    cameraOpen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}