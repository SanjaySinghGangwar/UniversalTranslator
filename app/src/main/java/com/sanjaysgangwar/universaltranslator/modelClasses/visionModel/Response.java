package com.sanjaysgangwar.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response {

    @SerializedName("textAnnotations")
    @Expose
    private List<TextAnnotation> textAnnotations = null;
    @SerializedName("fullTextAnnotation")
    @Expose
    private FullTextAnnotation fullTextAnnotation;

    public List<TextAnnotation> getTextAnnotations() {
        return textAnnotations;
    }

    public void setTextAnnotations(List<TextAnnotation> textAnnotations) {
        this.textAnnotations = textAnnotations;
    }

    public FullTextAnnotation getFullTextAnnotation() {
        return fullTextAnnotation;
    }

    public void setFullTextAnnotation(FullTextAnnotation fullTextAnnotation) {
        this.fullTextAnnotation = fullTextAnnotation;
    }

}
