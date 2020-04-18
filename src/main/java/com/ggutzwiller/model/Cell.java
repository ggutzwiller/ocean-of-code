package com.ggutzwiller.model;

import com.ggutzwiller.io.Printer;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a cell in the grid.
 */
public class Cell {
    /* The cell is an island, ie. we cannot go on it */
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

    /**
     * Compute Manhattan distance between two cells.
     * @param cell the other cell
     * @return the manhattan distance
     */
    public int distance(Cell cell) {
        return Math.abs(this.posY - cell.posY) + Math.abs(this.posX - cell.posX);
    }

    /**
     * Determine what is the orientation to go to a cell on the same row or same column.
     * @param cell the cell to go to
     * @return the Orientation to go to this cell
     */
    public Orientation orientationToGoTo(Cell cell) {
        if (this.posY == cell.posY && this.posX == cell.posX) {
            Printer.log("This is the same cell, returning default orientation");
            return Orientation.NORTH;
        } else if (this.posY != cell.posY && this.posX != cell.posX) {
            Printer.log("Cell is not on same column or same row, returning default orientation");
            return Orientation.NORTH;
        }

        return Arrays.stream(Orientation.values())
                .filter(v -> v.forwardX == (cell.posX - this.posX) && v.forwardY == (cell.posY - this.posY))
                .findFirst()
                .get();
    }

    /**
     * Find damages done on this cell given the cell that was shot.
     * @param cell the cell that was shot
     * @return a number of damages, either 2, 1, or 0
     */
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
