package com.sanjaysgangwar.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Property___ {

    @SerializedName("detectedLanguages")
    @Expose
    private List<DetectedLanguage___> detectedLanguages = null;

    public List<DetectedLanguage___> getDetectedLanguages() {
        return detectedLanguages;
    }

    public void setDetectedLanguages(List<DetectedLanguage___> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
    }

}
