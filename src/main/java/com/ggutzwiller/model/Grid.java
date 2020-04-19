package com.ggutzwiller.model;

import com.ggutzwiller.io.Printer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the grid for the ocean of code game. It generally contains 225 cells.
 */
public class Grid {
    public int width;
    public int height;
    public Cell[][] cells;

    /**
     * Constructor for grid : includes the construction of all the cells based on the
     * lines given by the Reader.
     * @param width width of the grid, generally 15
     * @param height height of the grid, generally 15
     * @param lines lines representing the grid, 'x' is an island cell, '.' is a sea cell
     */
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

    /**
     * Retrieve all the cells around a given cell.
     * @param currentCell the cell to search around
     * @param min min distance (let's say I want to avoid to send a torpedo on my submarine...)
     * @param max max distance (I cannot shoot at 5+ distance)
     * @return a set of cells
     */
    public Set<Cell> retrieveDistantCells(Cell currentCell, int min, int max) {
        return Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .filter(c -> (c.distance(currentCell) <= max) && c.distance(currentCell) >= min)
                .collect(Collectors.toSet());
    }

    /**
     * Return the "next cell" given an orientation
     * @param currentPath the current path with current cell is its lastCell
     * @param orientation the orientation I want to "go to"
     * @return the "next cell", if it exists, an empty optional otherwise
     */
    public Optional<Cell> applyOrientation(Path currentPath, Orientation orientation) {
        return applyWay(currentPath, new Way(1, orientation));
    }

    /**
     * Similar as applyOrientation but with a Way, ie. an orientation and a distance.
     * It will check that I can go on all the cells on the way.
     * @param currentPath the current path with current cells is its lastCell
     * @param way the way I want to "go to"
     * @return the "next cell", if it exists, an empty optional otherwise
     */
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

    /**
     * Returns all the cells that were shot by a torpedo but were not the target cell itself
     * @param targetCell the target cell
     * @return a set of cells that were shot
     */
    public Set<Cell> getTorpedoMineZone(Cell targetCell) {
        return Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .filter(cell -> !cell.island)
                .filter(cell -> targetCell.torpedoDamages(cell) == 1)
                .collect(Collectors.toSet());
    }

    /**
     * Apply a way on a path, similar as applyWay but will really apply it on the path.
     * @param currentPath the current path
     * @param way the way to "go to"
     */
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
