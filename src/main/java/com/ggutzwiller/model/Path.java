package com.ggutzwiller.model;

import java.util.*;

/**
 * A Path is the set of cells that a submarine already went to.
 * The representation is a set of cells that a submarine went to, and the lastCell is the cell where
 * the submarine is currently.
 */
public class Path {
    public Set<Cell> cells = new HashSet<>();
    public Cell lastCell;

    public Path(Cell cell) {
        this.cells.add(cell);
        this.lastCell = cell;
    }

    public Path(Path pathToCopy) {
        this.cells = new HashSet<>(pathToCopy.cells);
        this.lastCell = pathToCopy.lastCell;
    }

    public void addCell(Cell cell) {
        this.cells.add(cell);
        this.lastCell = cell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return cells.equals(path.cells) &&
                lastCell.equals(path.lastCell);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cells, lastCell);
    }

    @Override
    public String toString() {
        return "Path{" +
                "cells=" + cells +
                ", lastCell=" + lastCell +
                '}';
    }
}
