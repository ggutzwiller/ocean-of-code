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

    public List<Cell> retrieveDistantCells(Cell currentCell, int min, int max) {
        List<Cell> distantCells = Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .filter(c -> (c.distance(currentCell) <= max) && c.distance(currentCell) >= min)
                .collect(Collectors.toList());

        Collections.shuffle(distantCells);
        return distantCells;
    }

    public Optional<Cell> applyOrientation(Path currentPath, Orientation orientation) {
        return applyWay(currentPath, new Way(1, orientation));
    }

    public Optional<Cell> applyWay(Path currentPath, Way way) {
        int nextX = currentPath.lastCell.posX + (way.orientation.forwardX * way.distance);
        int nextY = currentPath.lastCell.posY + (way.orientation.forwardY * way.distance);

        if (way.distance > 1) {
            Optional<Cell> intermediateCell = applyWay(currentPath, new Way(way.distance - 1, way.orientation));
            if (!intermediateCell.isPresent() || currentPath.cells.contains(intermediateCell.get())) {
                return Optional.empty();
            }
        }

        if (!(nextX < 0 || nextX > width - 1 || nextY < 0 || nextY > height - 1)) {
            Cell foundCell = this.cells[nextX][nextY];
            if (!foundCell.island && !currentPath.cells.contains(foundCell)) {
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

    public void applyChosenWayOnPath(Path currentPath, Way way) {
        for (int i = 1; i <= way.distance; i++) {
            Optional<Cell> possibleCell = this.applyOrientation(currentPath, way.orientation);
            if (!possibleCell.isPresent()) {
                Printer.error("Unable to find cell to mark as taken");
                return;
            }

            currentPath.addCell(possibleCell.get());
        }
    }
}
