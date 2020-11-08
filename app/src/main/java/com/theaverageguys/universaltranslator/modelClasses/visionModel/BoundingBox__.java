package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoundingBox__ {

    @SerializedName("vertices")
    @Expose
    private List<Vertex___> vertices = null;

    public List<Vertex___> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex___> vertices) {
        this.vertices = vertices;
    }

}
