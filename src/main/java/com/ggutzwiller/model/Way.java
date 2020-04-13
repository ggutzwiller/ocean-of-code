package com.ggutzwiller.model;

/**
 * @author Grégoire Gutzwiller
 * @since 12/04/2020
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
