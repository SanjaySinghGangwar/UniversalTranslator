package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Property {

    @SerializedName("detectedLanguages")
    @Expose
    private List<DetectedLanguage> detectedLanguages = null;

    public List<DetectedLanguage> getDetectedLanguages() {
        return detectedLanguages;
    }

    public void setDetectedLanguages(List<DetectedLanguage> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
    }

}
