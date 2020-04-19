package com.ggutzwiller.strategy;

import com.ggutzwiller.io.Printer;
import com.ggutzwiller.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The action manager is responsible for chosing what to do during a turn.
 */
public class ActionManager {
    private static final double DEFAULT_MINIMUM_CHANCES_TO_HIT = 0.40;
    private static final double MINIMUM_CHANCES_TO_HIT_WITH_PLAYER = 0.50;

    public Submarine playerSubmarine;
    public Submarine enemySubmarine;
    public Grid grid;
    public Set<Cell> playerMines = new HashSet<>();

    /* TODO: Implement MINE orders */
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
     * Drop a mine on the map
     */
    public Optional<Orientation> dropMine() {
        if (this.playerSubmarine.mineCooldown != 0) {
            Printer.error("Cannot drop mine if not ready.");
            return Optional.empty();
        }

        for (Orientation orientation : Orientation.values()) {
            Optional<Cell> possibleCell = this.grid.applyOrientation(new Path(this.playerSubmarine.cell), orientation);
            if (possibleCell.isPresent() && !this.playerMines.contains(possibleCell.get())) {
                this.playerMines.add(possibleCell.get());
                return Optional.of(orientation);
            }
        }

        return Optional.empty();
    }

    /**
     * Trigger a mine if possible.
     */
    public Optional<Cell> triggerMine(Map<Cell, Double> possibleOpponentCells) {
        if (this.playerMines.isEmpty()) {
            Printer.error("You need to drop mines before triggering them.");
            return Optional.empty();
        }

        Optional<Cell> possibleHit = chooseCellToHit(this.playerMines, possibleOpponentCells);
        possibleHit.ifPresent(cell -> this.playerMines.remove(cell));
        return possibleHit;
    }

    /**
     * Choose where to launch a torpedo.
     * @param possibleOpponentCells the cells where the opponent can be
     * @return an Optional of cell, filled if we want to launch a torpedo on a cell
     */
    public Optional<Cell> launchTorpedo(Map<Cell, Double> possibleOpponentCells) {
        if (this.playerSubmarine.torpedoCooldown != 0) {
            Printer.error("Cannot launch torpedo if not ready.");
            return Optional.empty();
        }

        Set<Cell> possibleTargetCells = this.grid.retrieveDistantCells(this.playerSubmarine.cell, 1, 4);
;
        return chooseCellToHit(possibleTargetCells, possibleOpponentCells);
    }

    /**
     * Choose a cell to be hit based on the possible opponent cells and the cells the player can hit
     */
    private Optional<Cell> chooseCellToHit(Set<Cell> possibleTargetCells, Map<Cell, Double> possibleOpponentCells) {
        if (possibleOpponentCells.size() == 1) {
            Optional<Cell> possibleCell = possibleOpponentCells.keySet().stream().findAny();
            Printer.log("Only one possible cell: " + possibleCell);
            if (possibleTargetCells.contains(possibleCell.get())) {
                return possibleCell;
            } else {
                return possibleTargetCells.stream()
                        .filter(cell -> this.grid.getTorpedoMineZone(cell).contains(possibleCell))
                        .findAny();
            }
        }

        Optional<AbstractMap.SimpleEntry<Cell, Double>> chosenTargetCell = possibleTargetCells.stream()
                .map(cell -> new AbstractMap.SimpleEntry<>(cell, chancesOfHitting(cell, possibleOpponentCells)))
                .filter(pair -> {
                    if (this.grid.getTorpedoMineZone(pair.getKey()).contains(this.playerSubmarine.cell)) {
                        return pair.getValue() > MINIMUM_CHANCES_TO_HIT_WITH_PLAYER && this.playerSubmarine.life > 2;
                    } else {
                        return pair.getValue() > DEFAULT_MINIMUM_CHANCES_TO_HIT;
                    }
                })
                .peek(cell -> Printer.log("Cell " + cell.getKey() + " with score: " + cell.getValue()))
                .max(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue));

        return chosenTargetCell.map(AbstractMap.SimpleEntry::getKey);
    }

    /**
     * Compute the chances to hit the opponent given a target cell.
     */
    private double chancesOfHitting(Cell targetCell, Map<Cell, Double> possibleOpponentCells) {
        return this.grid.getTorpedoMineZone(targetCell).stream()
                .mapToDouble(cell -> possibleOpponentCells.containsKey(cell) ? possibleOpponentCells.get(cell) : 0)
                .reduce(Double::sum)
                .orElse(0.);
    }
}
