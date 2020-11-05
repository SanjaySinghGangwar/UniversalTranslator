package com.sanjaysgangwar.universaltranslator.imageProcessing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sanjaysgangwar.universaltranslator.activity.resultScreen;
import com.sanjaysgangwar.universaltranslator.api.apiInterface;
import com.sanjaysgangwar.universaltranslator.modelClasses.AppSharePreference;
import com.sanjaysgangwar.universaltranslator.modelClasses.translateModel.Model;
import com.sanjaysgangwar.universaltranslator.modelClasses.visionModel.VisionModel;
import com.sanjaysgangwar.universaltranslator.sevices.myToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.sanjaysgangwar.universaltranslator.sevices.utils.APIkey;
import static com.sanjaysgangwar.universaltranslator.sevices.utils.networkIsOnline;

public class google {

    public static AppSharePreference appSharePreference;

    public static void sentImageToServer(File absoluteFile, Context context) {
        try {
            File f = new File(String.valueOf((absoluteFile)));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));

            b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            //vision Api
            JsonObject type = new JsonObject();
            JsonObject content = new JsonObject();
            JsonObject requests = new JsonObject();
            JsonArray jsonArray = new JsonArray();

            content.addProperty("content", imageString);
            type.addProperty("type", "DOCUMENT_TEXT_DETECTION");/*TEXT_DETECTION*/
            requests.add("image", content);
            JsonArray array = new JsonArray();
            array.add(type);
            requests.add("features", array);
            jsonArray.add(requests);
            JsonObject request = new JsonObject();
            request.add("requests", jsonArray);

            boolean stat = networkIsOnline(context);
            if (stat) {
                Retrofit.Builder builder = new Retrofit.Builder()
                        .baseUrl("https://vision.googleapis.com/v1/images:annotate/")
                        .addConverterFactory(GsonConverterFactory.create());
                Retrofit retrofit = builder.build();
                apiInterface apiInterface = retrofit.create(apiInterface.class);
                Call<VisionModel> call = apiInterface.visionApi(request);
                call.enqueue(new Callback<VisionModel>() {
                    @Override
                    public void onResponse(Call<VisionModel> call, Response<VisionModel> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                try {
                                    String extractText = response.body().getResponses().get(0).getFullTextAnnotation().getText();
                                    translateAPI(extractText, context);
                                } catch (Exception e) {
                                    myToast.showRed(context, e.getMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<VisionModel> call, Throwable t) {
                        myToast.showRed(context, t.getMessage());
                    }
                });
            } else {
                myToast.showRed(context, "No internet Connection");
            }


        } catch (Exception e) {
            myToast.showRed(context, e.getMessage());
        }
    }

    public static void translateAPI(String extractText, Context context) {
        appSharePreference = new AppSharePreference(context);
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://translation.googleapis.com/language/translate/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        apiInterface apiInterface = retrofit.create(apiInterface.class);
        Call<Model> call = apiInterface.translateApi(APIkey, extractText, appSharePreference.getTargetLanguage());
        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("REST API", "onResponse: TRANSLATE" + response.body().getData().getTranslations().get(0).getTranslatedText());
                        if (response.body().getData().getTranslations().get(0).getTranslatedText() != null) {
                            String sourceLocale = response.body().getData().getTranslations().get(0).getDetectedSourceLanguage();
                            String translatedText = response.body().getData().getTranslations().get(0).getTranslatedText();
                            Intent i = new Intent(context, resultScreen.class);
                            i.putExtra("sourceLocale", sourceLocale);
                            i.putExtra("sourceText", extractText);
                            i.putExtra("translatedText", translatedText);
                            i.putExtra("languageSelected", appSharePreference.getSelectedLanguage());
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                myToast.showRed(context, "Try again " + t.getLocalizedMessage());
            }
        });
    }

}
