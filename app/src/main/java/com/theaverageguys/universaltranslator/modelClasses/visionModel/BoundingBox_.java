package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoundingBox_ {

    @SerializedName("vertices")
    @Expose
    private List<Vertex__> vertices = null;

    public List<Vertex__> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex__> vertices) {
        this.vertices = vertices;
    }

}
