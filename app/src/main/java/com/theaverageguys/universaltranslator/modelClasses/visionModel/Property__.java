package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Property__ {

    @SerializedName("detectedLanguages")
    @Expose
    private List<DetectedLanguage__> detectedLanguages = null;

    public List<DetectedLanguage__> getDetectedLanguages() {
        return detectedLanguages;
    }

    public void setDetectedLanguages(List<DetectedLanguage__> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
    }

}
