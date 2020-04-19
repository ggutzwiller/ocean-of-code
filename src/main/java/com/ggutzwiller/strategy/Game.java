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
        /* SILENCE orders */
        String silenceOrderBefore = "";
        if (this.playerSubmarine.silenceCooldown == 0) {
            silenceOrderBefore = chooseSilence();
        }

        /* MOVE orders */
        String moveOrder = chooseMovement() + " " + actionManager.charge();

        /* TORPEDO orders */
        String torpedoOrder = "";
        if (this.playerSubmarine.torpedoCooldown == 0) {
            Optional<Cell> torpedoCell = actionManager.launchTorpedo(opponentPositionManager.possibleOpponentCells);
            torpedoOrder = torpedoCell.map(cell -> {
                this.opponentPositionManager.lastShot = cell;
                return "TORPEDO " + cell.toString();
            }).orElse("");
        }

        /* MINE orders */
        String mineOrder = "";
        Optional<Cell> triggerCell = actionManager.triggerMine(opponentPositionManager.possibleOpponentCells);
        mineOrder = triggerCell.map(cell -> {
            this.opponentPositionManager.lastMine = cell;
            return "TRIGGER " + cell.toString();
        }).orElseGet(() -> {
            if (this.playerSubmarine.mineCooldown == 0) {
                Optional<Orientation> orientation = actionManager.dropMine();
                if (orientation.isPresent()) {
                    return "MINE " + orientation.get().label;
                }
            }
            return "";
        });

        /* SILENCE orders */
        String silenceOrderAfter = "";
        if (silenceOrderBefore.equals("") && this.playerSubmarine.silenceCooldown == 0) {
            silenceOrderAfter = chooseSilence();
        }


        /* MSG orders */
        String msgOrder = "MSG Hello!";

        return silenceOrderBefore + " | " + moveOrder + " | " + torpedoOrder + " | "
                + mineOrder + " | " + silenceOrderAfter + " | " + msgOrder;
    }

    private String chooseSilence() {
        Optional<Way> way = movementManager.chooseMovement(4);
        return way.map(value -> "SILENCE " + value.orientation.label + " " + value.distance).orElse("");
    }

    /**
     * Choose the movement to play this turn.
     */
    private String chooseMovement() {
        Optional<Way> way = movementManager.chooseMovement(1);
        return way.map(value -> "MOVE " + value.orientation.label).orElse("SURFACE");
    }
}
