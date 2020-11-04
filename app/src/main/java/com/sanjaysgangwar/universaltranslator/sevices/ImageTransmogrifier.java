package com.sanjaysgangwar.universaltranslator.sevices;


import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageTransmogrifier implements ImageReader.OnImageAvailableListener {

    public final int width;
    public final int height;
    private final ImageReader imageReader;
    private final chatHeadService svc;
    public Display display;
    int count = 0;
    private Bitmap latestBitmap = null;
    private int statusBarHeight;


    public ImageTransmogrifier(chatHeadService svc) {
        this.svc = svc;

        statusBarHeight = svc.getStatusBarHeight();
        Log.e("statusBarHeight", String.valueOf(statusBarHeight));

        display = svc.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);

        int width = size.x;
        int height = size.y;

        while (width * height > (2 << 19)) {
            width = width >> 1;
            height = height >> 1;
        }

        this.width = width;
        this.height = height;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(this, svc.getHandler());


    }


    @Override
    public void onImageAvailable(ImageReader reader) {
        final Image image = imageReader.acquireLatestImage();

        if (image != null) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            int bitmapWidth = width + rowPadding / pixelStride;

            if (latestBitmap == null ||
                    latestBitmap.getWidth() != bitmapWidth ||
                    latestBitmap.getHeight() != height) {
                if (latestBitmap != null) {
                    latestBitmap.recycle();
                }

                latestBitmap = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888);
            }

            latestBitmap.copyPixelsFromBuffer(buffer);

            if (image != null) {
                image.close();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Log.e("fullHeight", String.valueOf(height));
            Log.e("fullWidth", String.valueOf(width));
            Log.e("statusMain", String.valueOf(statusBarHeight));
            Bitmap cropped = Bitmap.createBitmap(latestBitmap, 0, statusBarHeight, width, (height - statusBarHeight));

            cropped.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] newPng = baos.toByteArray();
            svc.processImage(newPng);
        }
    }

    public Surface getSurface() {
        return (imageReader.getSurface());
    }

    public int getWidth() {
        return (width);
    }

    public int getHeight() {
        return (height);
    }

    void close() {
        imageReader.close();
    }
}

