package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Property_ {

    @SerializedName("detectedLanguages")
    @Expose
    private List<DetectedLanguage_> detectedLanguages = null;

    public List<DetectedLanguage_> getDetectedLanguages() {
        return detectedLanguages;
    }

    public void setDetectedLanguages(List<DetectedLanguage_> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
    }

}
