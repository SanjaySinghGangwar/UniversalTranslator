package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DetectedLanguage {

    @SerializedName("languageCode")
    @Expose
    private String languageCode;
    @SerializedName("confidence")
    @Expose
    private Double confidence;

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

}
