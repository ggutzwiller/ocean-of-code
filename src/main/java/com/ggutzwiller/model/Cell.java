package com.ggutzwiller.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a cell in the grid. Can be taken if we are not allowed to go on it.
 */
public class Cell {
    public boolean island;
    public int posX;
    public int posY;
    public int sector;

    public Cell(boolean island, int posX, int posY, int sector) {
        this.posX = posX;
        this.posY = posY;
        this.island = island;
        this.sector = sector;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return posX == cell.posX && posY == cell.posY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(posX, posY);
    }
}
