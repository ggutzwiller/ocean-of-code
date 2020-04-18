package com.ggutzwiller.strategy;

import com.ggutzwiller.io.Printer;
import com.ggutzwiller.model.Cell;
import com.ggutzwiller.model.Grid;
import com.ggutzwiller.model.Submarine;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The action manager is responsible for chosing what to do during a turn.
 */
public class ActionManager {
    private static final int MAXIMUM_NUMBER_OF_OPPONENT_CELL = 24;

    public Submarine playerSubmarine;
    public Submarine enemySubmarine;
    public Grid grid;

    public ActionManager(Submarine playerSubmarine, Submarine enemySubmarine, Grid grid) {
        this.playerSubmarine = playerSubmarine;
        this.enemySubmarine = enemySubmarine;
        this.grid = grid;
    }

    /**
     * Choose what to charge, either TORPEDO, SILENCE, MINE OR SONAR.
     */
    public String charge() {
        if (this.playerSubmarine.torpedoCooldown != 0) {
            return "TORPEDO";
        } else if (this.playerSubmarine.silenceCooldown != 0) {
            return "SILENCE";
        } else if (this.playerSubmarine.mineCooldown != 0) {
            return "MINE";
        } else if (this.playerSubmarine.sonarCooldown != 0){
            return "SONAR";
        } else {
            return "";
        }
    }

    /**
     * Choose where to launch a torpedo.
     * @param possibleOpponentCells the cells where the opponent can be
     * @return an Optional of cell, filled if we want to launch a torpedo on a cell
     */
    public Optional<Cell> launchTorpedo(Set<Cell> possibleOpponentCells) {
        if (this.playerSubmarine.torpedoCooldown != 0) {
            Printer.error("Cannot launch torpedo if not ready.");
            return Optional.empty();
        }

        if (possibleOpponentCells.size() > MAXIMUM_NUMBER_OF_OPPONENT_CELL) {
            Printer.log("Trying to launch torpedo but we cannot precisely find where the opponent is.");
            return Optional.empty();
        }

        Set<Cell> possibleTargetCells = this.grid.retrieveDistantCells(this.playerSubmarine.cell, 1, 4);
        Optional<Cell> targetCell = possibleTargetCells.stream()
                .filter(possibleOpponentCells::contains)
                .filter(cell -> !this.grid.getTorpedoZone(cell).contains(this.playerSubmarine.cell))
                .findAny();

        if (!targetCell.isPresent() && this.playerSubmarine.life > 2) {
            targetCell = possibleTargetCells.stream()
                    .filter(possibleOpponentCells::contains)
                    .findAny();
        }

        if (!targetCell.isPresent()) {
            for (Cell possibleTargetCell : possibleTargetCells) {
                Set<Cell> torpedoZone = this.grid.getTorpedoZone(possibleTargetCell);

                if (!Collections.disjoint(torpedoZone, possibleOpponentCells)) {
                    targetCell = Optional.of(possibleTargetCell);
                    break;
                }
            }
        }

        return targetCell;
    }
}
