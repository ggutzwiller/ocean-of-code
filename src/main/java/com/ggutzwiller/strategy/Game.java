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
    private static final int NUMBER_OF_MOVEMENT_TO_FORESEE = 10;

    public Submarine playerSubmarine;
    public Submarine enemySubmarine;
    public Grid grid;
    public OpponentPositionManager opponentPositionManager;

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

        return startCell.toString();
    }

    // TODO: refactor the logic of a "turn". It is not either silence or default, whether the silence is ready or not.
    public String playTurn() {
        this.playerSubmarine.cell.taken = true;

        if (this.playerSubmarine.silenceCooldown == 0) {
            return playSilenceTurn();
        }

        return playDefaultTurn();
    }

    private String playSilenceTurn() {
        return chooseMovement(4);
    }

    private String playDefaultTurn() {
        String movement = chooseMovement(1);
        String action = chooseAction();

        if (action.equals("")) {
            return movement;
        }

        return movement + " | " + action;
    }

    /* TODO: refactor to make it clearer,
     *  Option 1: this is used only for MOVE order, not SILENCE --> Silence logic is moved elsewhere.
     *  Option 2: if we keep it for SILENCE --> should be renamed, and logic simplified.
     *  + Shall we return a cell instead? or an orientation? or a couple of (orientation, distance)?
     */
    private String chooseMovement(int movesCount) {
        Cell currentCell = this.playerSubmarine.cell;

        List<Way> possibleWays = getPossibleWays(currentCell, movesCount);

        if (possibleWays.isEmpty()) {
            Printer.log("There is no possible ways, let's surface.");
            this.grid.reset();
            return "SURFACE";
        } else {
            Way way = findBestWay(currentCell, possibleWays);
            this.playerSubmarine.cell = this.grid.applyWay(currentCell, way).get();
            this.grid.markCellOnWayAs(currentCell, way, true);

            if (way.distance == 1) {
                return "MOVE " + way.orientation.label + " " + chooseCharge();
            } else {
                return "SILENCE " + way.orientation.label + " " + way.distance;
            }
        }
    }

    private Way findBestWay(Cell departureCell, List<Way> possibleWays) {
        Map<Way, Integer> scoresPerWay = new HashMap<>();

        for (Way way : possibleWays) {
            int score = computeScoreForWay(departureCell, way, NUMBER_OF_MOVEMENT_TO_FORESEE);
            if (score == NUMBER_OF_MOVEMENT_TO_FORESEE) {
                return way;
            }
            scoresPerWay.put(way, score);
        }

        Map.Entry<Way, Integer> chosenWay = scoresPerWay.entrySet()
                .stream()
                .sorted((way1, way2) -> way2.getValue() - way1.getValue())
                .findFirst()
                .get();
        Printer.log("Chosen way (" + chosenWay.getKey().toString() + ") has a score of: " + chosenWay.getValue());
        return chosenWay.getKey();
    }

    private int computeScoreForWay(Cell departureCell, Way way, int depth) {
        if (depth == 0) {
            return NUMBER_OF_MOVEMENT_TO_FORESEE;
        }

        Optional<Cell> nextCell = this.grid.applyWay(departureCell, way);
        if (!nextCell.isPresent()) {
            return 0;
        }

        List<Way> possibleWays = getPossibleWays(nextCell.get(), 1);
        if (possibleWays.isEmpty()) {
            return NUMBER_OF_MOVEMENT_TO_FORESEE - depth;
        }

        int max = 0;
        for (Way nextWay : possibleWays) {
            this.grid.markCellOnWayAs(departureCell, nextWay, true);
            int score = computeScoreForWay(nextCell.get(), nextWay, depth - 1);
            if (score == NUMBER_OF_MOVEMENT_TO_FORESEE) {
                this.grid.markCellOnWayAs(departureCell, nextWay, false);
                return score;
            } else if (score > max) {
                max = score;
            }
            this.grid.markCellOnWayAs(departureCell, nextWay, false);
        }
        return max;
    }

    private List<Way> getPossibleWays(Cell departureCell, int movesCount) {
        Optional<Cell> proposedCell;
        List<Way> ways = new ArrayList<>();
        for (Orientation orientation : Orientation.values()) {
            for (int i = movesCount; i > 0; i--) {
                proposedCell = this.grid.getNextCellForPlayerMove(departureCell, orientation, i);

                if (proposedCell.isPresent() && !proposedCell.get().taken) {
                    ways.add(new Way(i, orientation));
                }
            }
        }

        return ways;
    }

    // TODO: logic of choose charge should be more complex than based on torpedo cooldown
    private String chooseCharge() {
        return this.playerSubmarine.torpedoCooldown == 0 ? "SILENCE" : "TORPEDO";
    }

    /* TODO: torpedo is not the only action now. + should we manage movement here?
     *  + we should absolutely use SILENCE order differently. It should not be automatic.
     */
    private String chooseAction() {
        Printer.log("Torpedo cooldown: " + this.playerSubmarine.torpedoCooldown);

        if (this.playerSubmarine.torpedoCooldown != 0) {
            return "";
        }

        List<Cell> possibleOpponentCells = this.opponentPositionManager.possibleOpponentCells;
        if (possibleOpponentCells.size() > 48) {
            return "";
        }

        List<Cell> possibleTargetCells = this.grid.retrieveDistantCells(this.playerSubmarine.cell, 3, 4);
        Optional<Cell> targetCell = possibleTargetCells.stream()
                .filter(possibleOpponentCells::contains)
                .findAny();

        /* If we find none, let's try with closer cells */
        if (!targetCell.isPresent() && possibleOpponentCells.size() < 24) {
            possibleTargetCells = this.grid.retrieveDistantCells(this.playerSubmarine.cell, 1, 4);
            targetCell = possibleTargetCells.stream()
                    .filter(possibleOpponentCells::contains)
                    .findAny();
        }

        /* If we still find none, perhaps we can shot on an adjacent cell */
        if (!targetCell.isPresent() && possibleOpponentCells.size() < 24) {
            for (Cell possibleTargetCell : possibleTargetCells) {
                List<Cell> torpedoZone = this.grid.getTorpedoZone(possibleTargetCell);

                if (!Collections.disjoint(torpedoZone, possibleOpponentCells)) {
                    targetCell = Optional.of(possibleTargetCell);
                    break;
                }
            }
        }

        return targetCell.map(cell -> {
            this.opponentPositionManager.lastShot = cell;
            return "TORPEDO " + cell.toString();
        }).orElse("");
    }
}
