package com.ggutzwiller.strategy;

import com.ggutzwiller.io.Printer;
import com.ggutzwiller.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class manages the computation of the possible opponent positions.
 */
public class OpponentPositionManager {
    public Grid grid;
    public List<Path> possibleOpponentPaths;
    public Cell lastShot;

    public OpponentPositionManager(Grid grid) {
        this.grid = grid;
        this.possibleOpponentPaths = Arrays.stream(grid.cells)
                .flatMap(Arrays::stream)
                .filter(cell -> !cell.island)
                .map(Path::new)
                .collect(Collectors.toList());
    }

    public void recomputePositions(String orders, int opponentLifeLost) {
        Printer.log("Now recomputing position in the position manager");
        handleTouchedOpponent(opponentLifeLost, orders);
        this.lastShot = null;

        Arrays.stream(orders.split("\\|"))
                .map(String::trim)
                .forEach(order -> {
                    if (order.contains("MOVE")) {
                        handleMoveOrder(Orientation.byLabel(Character.toString(order.trim().charAt(5))));
                    } else if (order.contains("SILENCE")) {
                        handleSilenceOrder();
                    } else if (order.contains("SURFACE")) {
                        handleSectorOrder(Integer.parseInt(order.replace("SURFACE", "").trim()));
                    } else if (order.contains("TORPEDO")) {
                        handleTorpedoOrder(order.replace("TORPEDO", "").trim());
                    }
                });

        List<Cell> possibleCells = this.getPossibleOpponentCells();
        Printer.log("Number of possible paths: " + possibleOpponentPaths.size() + " and cells: " + possibleCells.size());
        if (possibleCells.size() < 36) {
            Printer.log("Possible cells are: " + possibleCells);
        }

        if (this.possibleOpponentPaths.size() > 500) {
            Printer.log("Too much paths, reducing size to current possible cells");
            this.possibleOpponentPaths = possibleCells.stream()
                    .map(Path::new)
                    .collect(Collectors.toList());
        }
    }

    public List<Cell> getPossibleOpponentCells() {
        return possibleOpponentPaths.stream()
                .map(Path::lastCell)
                .distinct()
                .collect(Collectors.toList());
    }

    private void handleTouchedOpponent(int opponentLifeLost, String orders) {
        if (lastShot != null && opponentLifeLost == 2 && !orders.contains("SURFACE")) {
            this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                    .filter(path -> path.lastCell().equals(this.lastShot))
                    .collect(Collectors.toList());
        } else if (lastShot != null && (opponentLifeLost == 2 || opponentLifeLost == 1 && !orders.contains("SURFACE"))) {
            List<Cell> torpedoZone = this.grid.getTorpedoZone(lastShot);
            this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                    .filter(path -> torpedoZone.contains(path.lastCell()))
                    .collect(Collectors.toList());
        }
    }

    private void handleMoveOrder(Orientation orientation) {
        this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                .flatMap(path -> {
                    Optional<Cell> possibleCell = this.grid.applyOrientation(path.lastCell(), orientation);
                    if (!possibleCell.isPresent() || possibleCell.get().island || path.cells.contains(possibleCell.get())) {
                        return Stream.empty();
                    }
                    path.cells.add(possibleCell.get());
                    return Stream.of(path);
                })
                .collect(Collectors.toList());
    }

    private void handleSilenceOrder() {
        this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                .flatMap(path -> {
                    List<Cell> nextCells = new ArrayList<>();
                    for (Orientation orientation : Orientation.values()) {
                        for (int i = 1; i <= 4; i++) {
                            this.grid.applyWay(path.lastCell(), new Way(i, orientation))
                                    .ifPresent(nextCells::add);
                        }
                    }
                    nextCells.add(path.lastCell());

                    return nextCells.stream()
                            .distinct()
                            .filter(cell -> !cell.island && (cell == path.lastCell() || !path.cells.contains(cell)))
                            .map(cell -> {
                                Path newPath = new Path(path);
                                newPath.cells.add(cell);
                                return newPath;
                            });
                })
                .distinct()
                .collect(Collectors.toList());
    }

    void handleSectorOrder(int sector) {
        this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                .filter(path -> path.lastCell().sector == sector)
                .collect(Collectors.toList());
    }

    void handleTorpedoOrder(String torpedoCoordinates) {
        String[] coord = torpedoCoordinates.split(" ");
        Cell shotCell = this.grid.cells[Integer.parseInt(coord[0])][Integer.parseInt(coord[1])];

        this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                .filter(path -> shotCell.distance(path.lastCell()) <= 4)
                .collect(Collectors.toList());
    }
}
