package com.ggutzwiller.model;

/**
 * A Way is a combination of an Orientation and a distance.
 */
public class Way {
    public int distance;
    public Orientation orientation;

    public Way(int distance, Orientation orientation) {
        this.distance = distance;
        this.orientation = orientation;
    }

    @Override
    public String toString() {
        return orientation.label + distance;
    }
}
