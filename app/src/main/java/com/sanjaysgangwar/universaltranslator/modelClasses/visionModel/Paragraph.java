package com.sanjaysgangwar.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Paragraph {

    @SerializedName("property")
    @Expose
    private Property__ property;
    @SerializedName("boundingBox")
    @Expose
    private BoundingBox_ boundingBox;
    @SerializedName("words")
    @Expose
    private List<Word> words = null;

    public Property__ getProperty() {
        return property;
    }

    public void setProperty(Property__ property) {
        this.property = property;
    }

    public BoundingBox_ getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox_ boundingBox) {
        this.boundingBox = boundingBox;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

}
