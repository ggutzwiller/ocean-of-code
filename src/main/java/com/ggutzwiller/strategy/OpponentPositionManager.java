package com.ggutzwiller.strategy;

import com.ggutzwiller.io.Printer;
import com.ggutzwiller.model.Cell;
import com.ggutzwiller.model.Grid;
import com.ggutzwiller.model.Orientation;
import com.ggutzwiller.model.Way;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class manages the computation of the possible opponent positions.
 */
public class OpponentPositionManager {
    public Grid grid;
    public List<Cell> possibleOpponentCells;
    public Cell lastShot;

    public OpponentPositionManager(Grid grid) {
        this.grid = grid;
        this.possibleOpponentCells = Arrays.stream(grid.cells)
                .flatMap(Arrays::stream)
                .filter(cell -> !cell.island)
                .collect(Collectors.toList());
    }

    public void recomputePositions(String orders, int opponentLifeLost) {
        handleTouchedOpponent(opponentLifeLost);
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
                    }
                });
        Printer.log("Opponent can be on " + possibleOpponentCells.size() + " different cells.");
        if (possibleOpponentCells.size() < 36) {
            Printer.log("Possible cells are: " + possibleOpponentCells.stream().map(Cell::toString).collect(Collectors.joining("-")));
        }
    }

    private void handleTouchedOpponent(int opponentLifeLost) {
        if (lastShot != null && opponentLifeLost == 2) {
            this.possibleOpponentCells = Collections.singletonList(this.lastShot);
        }
    }

    private void handleMoveOrder(Orientation orientation) {
        this.possibleOpponentCells = this.possibleOpponentCells.stream()
                .map(cell -> this.grid.applyOrientation(cell, orientation))
                .filter(cell -> cell.isPresent() && !cell.get().island)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
    }

    private void handleSilenceOrder() {
        this.possibleOpponentCells = this.possibleOpponentCells.stream()
                .flatMap(cell -> {
                    List<Cell> nextCells = new ArrayList<>();
                    for (Orientation orientation : Orientation.values()) {
                        for (int i = 1; i <= 4; i++) {
                            this.grid.applyWay(cell, new Way(i, orientation))
                                    .ifPresent(nextCells::add);
                        }
                    }
                    return nextCells.stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }

    void handleSectorOrder(int sector) {
        this.possibleOpponentCells = this.possibleOpponentCells.stream()
                .filter(cell -> cell.sector == sector)
                .collect(Collectors.toList());
    }

}
