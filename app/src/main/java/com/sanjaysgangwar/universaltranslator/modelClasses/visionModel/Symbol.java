package com.sanjaysgangwar.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Symbol {

    @SerializedName("property")
    @Expose
    private Property____ property;
    @SerializedName("boundingBox")
    @Expose
    private BoundingBox___ boundingBox;
    @SerializedName("text")
    @Expose
    private String text;

    public Property____ getProperty() {
        return property;
    }

    public void setProperty(Property____ property) {
        this.property = property;
    }

    public BoundingBox___ getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox___ boundingBox) {
        this.boundingBox = boundingBox;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
