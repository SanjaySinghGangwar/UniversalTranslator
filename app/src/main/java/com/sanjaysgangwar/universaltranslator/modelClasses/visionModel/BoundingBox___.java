package com.sanjaysgangwar.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoundingBox___ {

    @SerializedName("vertices")
    @Expose
    private List<Vertex____> vertices = null;

    public List<Vertex____> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex____> vertices) {
        this.vertices = vertices;
    }

}
