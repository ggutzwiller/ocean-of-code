package com.ggutzwiller.strategy;

import com.ggutzwiller.io.Printer;
import com.ggutzwiller.model.*;
import com.sun.source.tree.IfTree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handle all the game logic, ie. choosing the start position and then play each turn according to logic.
 */
public class Game {
    public Submarine playerSubmarine;
    public Submarine enemySubmarine;
    public Grid grid;
    public String lastOrders;

    public OpponentPositionManager opponentPositionManager;
    public MovementManager movementManager;
    public ActionManager actionManager;

    /**
     * Choose the starting position.
     */
    public String chooseStartingPosition() {
        Cell startCell = Arrays.stream(this.grid.cells)
                .flatMap(Arrays::stream)
                .filter(c -> !c.island && c.posX > 0 && c.posX < grid.width - 1 && c.posY > 0 && c.posY < grid.height - 1)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Collections.shuffle(list);
                            return list.stream();
                        }
                ))
                .findAny()
                .orElse(this.grid.cells[1][1]);

        this.movementManager = new MovementManager(this.grid, startCell, this.playerSubmarine);
        return startCell.toString();
    }

    /**
     * Play a turn.
     */
    public String playTurn() {
        if (this.playerSubmarine.lostLife > 0 && this.playerSubmarine.silenceCooldown == 0) {
            return chooseMovement(4);
        }

        String movement = chooseMovement(1) + " " + actionManager.charge();
        String action = "";

        if (this.playerSubmarine.torpedoCooldown == 0) {
            Optional<Cell> torpedoCell = actionManager.launchTorpedo(opponentPositionManager.getPossibleOpponentCells());
            action = torpedoCell.map(cell -> {
                this.opponentPositionManager.lastShot = cell;
                return "TORPEDO " + cell.toString();
            }).orElse("");
        }

        if (action.equals("")) {
            return movement;
        }

        return movement + " | " + action;
    }

    /**
     * Choose the movement to play this turn.
     */
    private String chooseMovement(int movesCount) {
        Optional<Way> way = movementManager.chooseMovement(movesCount);

        if (!way.isPresent()) {
            return "SURFACE";
        } else if (movesCount == 1) {
            return "MOVE " + way.get().orientation.label;
        } else {
            return "SILENCE " + way.get().orientation.label + " " + way.get().distance;
        }
    }
}
