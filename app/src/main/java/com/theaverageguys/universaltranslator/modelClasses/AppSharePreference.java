package com.theaverageguys.universaltranslator.modelClasses;


import android.content.Context;
import android.content.SharedPreferences;

public class AppSharePreference {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private String APP_SHARED_PREFS;

    public AppSharePreference(Context mContext) {
        sharedPreferences = mContext.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        APP_SHARED_PREFS = "Translator";
    }

    public void clearPreferences() {
        editor.clear();
        editor.commit();
    }

    public String getMyName() {
        return sharedPreferences.getString("MyName", "");
    }

    public void setMyName(String MyName) {
        editor.putString("MyName", MyName);
        editor.commit();
    }

    public String getSelectedLanguage() {
        return sharedPreferences.getString("SelectedLanguage", "");
    }

    public void setSelectedLanguage(String SelectedLanguage) {
        editor.putString("SelectedLanguage", SelectedLanguage);
        editor.commit();
    }

    public String getTargetLanguage() {
        return sharedPreferences.getString("targetLanguage", "");
    }

    public void setTargetLanguage(String TargetLanguage) {
        editor.putString("targetLanguage", TargetLanguage);
        editor.commit();
    }

    public String getMyUid() {
        return sharedPreferences.getString("MyUid", "");
    }

    public void setMyUid(String MyUid) {
        editor.putString("MyUid", MyUid);
        editor.commit();
    }

    public String getTargetLanguageCode() {
        return sharedPreferences.getString("targetLanguage", "");
    }

    public void setTargetLanguageCode(String targetLanguage) {
        editor.putString("targetLanguage", targetLanguage);
        editor.commit();
    }
}
