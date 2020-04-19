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
    public Set<Path> possibleOpponentPaths;
    public Cell lastShot;
    public Cell lastMine;
    public Map<Cell, Double> possibleOpponentCells;

    public OpponentPositionManager(Grid grid) {
        this.grid = grid;
        resetListOfPaths();
    }

    /**
     * At the beginning of each turn, given the orders and the life that our opponents gave / lost, we compute the
     * possible positions for the opponent.
     * @param orders the last orders given by the opponent
     * @param opponentLifeLost the life our opponent lost during last turn
     */
    public void recomputePositions(String orders, int opponentLifeLost) {
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

        possibleOpponentCells = new HashMap<>();
        double unit = 1. / possibleOpponentPaths.size();

        possibleOpponentPaths.forEach(path -> {
             if (possibleOpponentCells.containsKey(path.lastCell)) {
                 possibleOpponentCells.put(path.lastCell, possibleOpponentCells.get(path.lastCell) + unit);
             } else {
                 possibleOpponentCells.put(path.lastCell, unit);
             }
        });

        Printer.log("Number of possible paths: " + possibleOpponentPaths.size() + " and cells: " + possibleOpponentCells.size());

        if (this.possibleOpponentPaths.size() > 500) {
            Printer.log("Too much paths, reducing size to current possible cells");
            this.possibleOpponentPaths = possibleOpponentCells.keySet()
                    .stream()
                    .map(Path::new)
                    .collect(Collectors.toSet());
        } else if (this.possibleOpponentPaths.size() == 0){
            Printer.log("No path found. Resetting list.");
            resetListOfPaths();
        }
    }

    /**
     * We reset the list of possible paths in some cases.
     */
    private void resetListOfPaths() {
        this.possibleOpponentPaths = Arrays.stream(grid.cells)
                .flatMap(Arrays::stream)
                .filter(cell -> !cell.island)
                .map(Path::new)
                .collect(Collectors.toSet());
    }

    /**
     * If we shot the opponent, we get an indication of where it is.
     */
    private void handleTouchedOpponent(int opponentLifeLost, String orders) {
        if (lastShot != null && opponentLifeLost == 2 && !orders.contains("SURFACE")) {
            this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                    .filter(path -> path.lastCell.equals(this.lastShot))
                    .collect(Collectors.toSet());
        } else if (lastShot != null && (opponentLifeLost == 2 || opponentLifeLost == 1 && !orders.contains("SURFACE"))) {
            Set<Cell> torpedoZone = this.grid.getTorpedoMineZone(lastShot);
            this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                    .filter(path -> torpedoZone.contains(path.lastCell))
                    .collect(Collectors.toSet());
        } else if (lastShot != null) {
            Set<Cell> torpedoZone = this.grid.getTorpedoMineZone(lastShot);
            this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                    .filter(path -> !torpedoZone.contains(path.lastCell) && !path.lastCell.equals(lastShot))
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Based on the movement and previous possible cells we calculate the possible new ones
     */
    private void handleMoveOrder(Orientation orientation) {
        this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                .flatMap(path -> {
                    Optional<Cell> possibleCell = this.grid.applyOrientation(path, orientation);
                    if (!possibleCell.isPresent() || possibleCell.get().island || path.cells.contains(possibleCell.get())) {
                        return Stream.empty();
                    }
                    path.addCell(possibleCell.get());
                    return Stream.of(path);
                })
                .collect(Collectors.toSet());
    }

    /**
     * In case of silence, we try to keep track of possible paths anyway.
     */
    private void handleSilenceOrder() {
        this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                .flatMap(path -> {
                    List<Way> nextWays = new ArrayList<>();
                    for (Orientation orientation : Orientation.values()) {
                        for (int i = 1; i <= 4; i++) {
                            Way way = new Way(i, orientation);
                            this.grid.applyWay(path, way)
                                    .ifPresent(cell -> nextWays.add(way));
                        }
                    }

                    Set<Path> paths = nextWays.stream()
                            .map(way -> {
                                Path newPath = new Path(path);
                                this.grid.applyChosenWayOnPath(newPath, way);
                                return newPath;
                            })
                            .collect(Collectors.toSet());
                    paths.add(path);
                    return paths.stream();
                })
                .collect(Collectors.toSet());
    }

    /**
     * If he used SURFACE, we know in what sector the opponent is.
     */
    void handleSectorOrder(int sector) {
        this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                .filter(path -> path.lastCell.sector == sector)
                .collect(Collectors.toSet());
    }

    /**
     * You cannot send a torpedo at 5+ of distance, so we can determine where the opponent is given the cell it shot.
     */
    void handleTorpedoOrder(String torpedoCoordinates) {
        String[] coord = torpedoCoordinates.split(" ");
        Cell shotCell = this.grid.cells[Integer.parseInt(coord[0])][Integer.parseInt(coord[1])];
        Printer.log("Opponent torpedo hit cell " + shotCell);

        this.possibleOpponentPaths = this.possibleOpponentPaths.stream()
                .filter(path -> shotCell.distance(path.lastCell) <= 4)
                .collect(Collectors.toSet());
    }
}
