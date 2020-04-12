package com.ggutzwiller.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a cell in the grid. Can be taken if we are not allowed to go on it.
 */
public class Cell {
    public boolean taken;
    public boolean island;
    public int posX;
    public int posY;
    public int sector;

    public Cell(boolean island, int posX, int posY, int sector) {
        this.taken = island;
        this.posX = posX;
        this.posY = posY;
        this.island = island;
        this.sector = sector;
    }

    void reset() {
        this.taken = island;
    }

    public String toString() {
        return this.posX + " " + this.posY;
    }

    public int distance(Cell cell) {
        return Math.abs(this.posY - cell.posY) + Math.abs(this.posX - cell.posX);
    }

    public Orientation orientationToGoTo(Cell cell) {
        return Arrays.stream(Orientation.values())
                .filter(v -> v.forwardX == (cell.posX - this.posX) && v.forwardY == (cell.posY - this.posY))
                .findFirst()
                .get();
    }

    public int torpedoDamages(Cell cell) {
        if (cell.equals(this)) {
            return 2;
        } else if (Math.abs(this.posY - cell.posY) <= 1 && Math.abs(this.posX - cell.posX) <= 1) {
            return 1;
        }

        return 0;
    }
}
