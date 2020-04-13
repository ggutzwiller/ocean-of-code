package com.ggutzwiller.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Grégoire Gutzwiller
 * @since 12/04/2020
 */
public class Path {
    public List<Cell> cells = new ArrayList<>();

    public Path(Cell cell) {
        this.cells.add(cell);
    }

    public Path(Path pathToCopy) {
        this.cells = new ArrayList<>(pathToCopy.cells);
    }

    public Cell lastCell() {
        return cells.get(cells.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return cells.equals(path.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cells);
    }
}
