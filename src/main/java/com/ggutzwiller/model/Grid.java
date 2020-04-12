package com.ggutzwiller.model;

import com.ggutzwiller.io.Printer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Grégoire Gutzwiller
 * @since 12/04/2020
 */
public class Grid {
    public int width;
    public int height;
    public Cell[][] cells;

    public Grid(int width, int height, List<String> lines) {
        Printer.log("Now creating grid (" + width + ", " + height + ").");
        Printer.log("Input lines are:\n" + String.join("\n", lines));

        this.width = width;
        this.height = height;

        this.cells = new Cell[width][height];
        int sectorSize = width / 3;
        for (int i = 0; i < height; i++) {
            String line = lines.get(i);

            for (int j = 0; j < line.length(); j++) {
                int sector = 1 + j / sectorSize + i / sectorSize * 3;
                cells[j][i] = new Cell(line.charAt(j) == 'x', j, i, sector);
            }
        }
    }

    public void reset() {
        Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .forEach(Cell::reset);
    }

    public List<Cell> retrieveDistantCells(Cell currentCell, int min, int max) {
        List<Cell> distantCells = Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .filter(c -> (c.distance(currentCell) <= max) && c.distance(currentCell) >= min)
                .collect(Collectors.toList());

        Collections.shuffle(distantCells);
        return distantCells;
    }

    public Optional<Cell> getNextCellForPlayerMove(Cell initialCell, Orientation orientation, int movesCount) {
        for (int i = 1; i < movesCount; i++) {
            Optional<Cell> intermediateCell = applyWay(initialCell, new Way(i, orientation));
            if (!intermediateCell.isPresent() || intermediateCell.get().taken) {
                return Optional.empty();
            }
        }

        Optional<Cell> nextCell = this.applyWay(initialCell, new Way(movesCount, orientation));
        if (!nextCell.isPresent() || nextCell.get().taken) {
            return Optional.empty();
        }

        return nextCell;
    }

    public Optional<Cell> applyOrientation(Cell departureCell, Orientation orientation) {
        return applyWay(departureCell, new Way(1, orientation));
    }

    public Optional<Cell> applyWay(Cell departureCell, Way way) {
        int nextX = departureCell.posX + (way.orientation.forwardX * way.distance);
        int nextY = departureCell.posY + (way.orientation.forwardY * way.distance);

        for (int i = 1; i < way.distance; i++) {
            Optional<Cell> intermediateCell = applyWay(departureCell, new Way(i, way.orientation));
            if (!intermediateCell.isPresent()) {
                return Optional.empty();
            }
        }

        if (!(nextX < 0 || nextX > width - 1 || nextY < 0 || nextY > height - 1)) {
            Cell foundCell = this.cells[nextX][nextY];
            if (!foundCell.island) {
                return Optional.of(foundCell);
            }
        }

        return Optional.empty();
    }

    public List<Cell> getTorpedoZone(Cell targetCell) {
        return Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .filter(cell -> !cell.island)
                .filter(cell -> targetCell.torpedoDamages(cell) == 1)
                .collect(Collectors.toList());
    }
}
