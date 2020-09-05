package com.sanjaysgangwar.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Word {

    @SerializedName("property")
    @Expose
    private Property___ property;
    @SerializedName("boundingBox")
    @Expose
    private BoundingBox__ boundingBox;
    @SerializedName("symbols")
    @Expose
    private List<Symbol> symbols = null;

    public Property___ getProperty() {
        return property;
    }

    public void setProperty(Property___ property) {
        this.property = property;
    }

    public BoundingBox__ getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox__ boundingBox) {
        this.boundingBox = boundingBox;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<Symbol> symbols) {
        this.symbols = symbols;
    }

}