package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Block {

    @SerializedName("property")
    @Expose
    private Property_ property;
    @SerializedName("boundingBox")
    @Expose
    private BoundingBox boundingBox;
    @SerializedName("paragraphs")
    @Expose
    private List<Paragraph> paragraphs = null;
    @SerializedName("blockType")
    @Expose
    private String blockType;

    public Property_ getProperty() {
        return property;
    }

    public void setProperty(Property_ property) {
        this.property = property;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

}
