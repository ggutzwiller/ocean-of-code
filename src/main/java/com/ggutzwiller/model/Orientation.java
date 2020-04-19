package com.ggutzwiller.model;

import com.ggutzwiller.io.Printer;

import java.util.Arrays;
import java.util.Optional;

/**
 * Orientation represents an orientation, either North, East, West or South.
 */
public enum Orientation {
    NORTH("N", 0, -1, "W", "E"),
    EAST("E", 1, 0, "N", "S"),
    WEST("W", -1, 0, "S", "N"),
    SOUTH("S", 0, 1, "E", "W");

    public String label;
    public int forwardX;
    public int forwardY;
    public String left;
    public String right;

    Orientation(String label, int forwardX, int forwardY, String left, String right) {
        this.label = label;
        this.forwardX = forwardX;
        this.forwardY = forwardY;
        this.left = left;
        this.right = right;
    }

    public Orientation right() {
            return byLabel(this.right);
    }

    public Orientation left() {
            return byLabel(this.left);
    }

    public static Orientation byLabel(String label) {
        Optional<Orientation> possibleOrientation = Arrays.stream(values())
                .filter(v -> v.label.equals(label))
                .findFirst();

        if (!possibleOrientation.isPresent()) {
            Printer.error("No orientation for label: " + label);
            return Orientation.NORTH;
        }

        return possibleOrientation.get();
    }
}
