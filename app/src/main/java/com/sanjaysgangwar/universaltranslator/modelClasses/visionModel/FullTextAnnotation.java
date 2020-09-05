package com.sanjaysgangwar.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FullTextAnnotation {

    @SerializedName("pages")
    @Expose
    private List<Page> pages = null;
    @SerializedName("text")
    @Expose
    private String text;

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
