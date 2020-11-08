package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Property____ {

    @SerializedName("detectedLanguages")
    @Expose
    private List<DetectedLanguage____> detectedLanguages = null;
    @SerializedName("detectedBreak")
    @Expose
    private DetectedBreak detectedBreak;

    public List<DetectedLanguage____> getDetectedLanguages() {
        return detectedLanguages;
    }

    public void setDetectedLanguages(List<DetectedLanguage____> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
    }

    public DetectedBreak getDetectedBreak() {
        return detectedBreak;
    }

    public void setDetectedBreak(DetectedBreak detectedBreak) {
        this.detectedBreak = detectedBreak;
    }

}
