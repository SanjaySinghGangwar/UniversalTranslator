package com.theaverageguys.universaltranslator.modelClasses.visionModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BoundingBox {

    @SerializedName("vertices")
    @Expose
    private List<Vertex_> vertices = null;

    public List<Vertex_> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex_> vertices) {
        this.vertices = vertices;
    }

}
