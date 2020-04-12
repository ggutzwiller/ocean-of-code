package com.ggutzwiller.strategy;

import com.ggutzwiller.io.Printer;
import com.ggutzwiller.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Grégoire Gutzwiller
 * @since 12/04/2020
 */
public class MovementManager {
    private static final int NUMBER_OF_MOVEMENT_TO_FORESEE = 10;

    public Grid grid;
    public Cell currentCell;
    public Submarine playerSubmarine;

    public MovementManager(Grid grid, Cell currentCell, Submarine playerSubmarine) {
        this.grid = grid;
        this.currentCell = currentCell;
        this.playerSubmarine = playerSubmarine;
    }

    public Optional<Way> chooseMovement(int movesCount) {
        Cell currentCell = this.playerSubmarine.cell;
        this.playerSubmarine.cell.taken = true;

        List<Way> possibleWays = getPossibleWays(currentCell, movesCount);

        if (possibleWays.isEmpty()) {
            Printer.log("There is no possible ways, let's surface.");
            this.grid.reset();
            return Optional.empty();
        } else {
            Path path = new Path();
            path.cells.add(currentCell);

            Way way = findBestWay(path, possibleWays);

            this.playerSubmarine.cell = this.grid.applyWay(currentCell, way).get();
            this.grid.markCellOnWayAs(currentCell, way, true);

            return Optional.of(way);
        }
    }

    private Way findBestWay(Path path, List<Way> possibleWays) {
        Map<Way, Integer> scoresPerWay = new HashMap<>();

        for (Way way : possibleWays) {
            int score = computeScoreForWay(path, way, NUMBER_OF_MOVEMENT_TO_FORESEE);
            if (score == NUMBER_OF_MOVEMENT_TO_FORESEE) {
                return way;
            }
            scoresPerWay.put(way, score);
        }

        Map.Entry<Way, Integer> chosenWay = scoresPerWay.entrySet()
                .stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .get();
        Printer.log("Chosen way (" + chosenWay.getKey().toString() + ") has a score of: " + chosenWay.getValue());
        return chosenWay.getKey();
    }

    private int computeScoreForWay(Path path, Way way, int depth) {
        if (depth == 0) {
            return NUMBER_OF_MOVEMENT_TO_FORESEE;
        }

        Cell currentCell = path.cells.get(path.cells.size() - 1);

        Optional<Cell> nextCell = this.grid.applyWay(currentCell, way);
        if (!nextCell.isPresent()) {
            return 0;
        }

        List<Way> possibleWays = getPossibleWays(nextCell.get(), 1);
        if (possibleWays.isEmpty() || path.cells.contains(nextCell.get())) {
            return NUMBER_OF_MOVEMENT_TO_FORESEE - depth;
        }

        int max = 0;
        for (Way nextWay : possibleWays) {
            path.cells.add(nextCell.get());
            int score = computeScoreForWay(path, nextWay, depth - 1);
            if (score == NUMBER_OF_MOVEMENT_TO_FORESEE) {
                path.cells.remove(nextCell.get());
                return score;
            } else if (score > max) {
                max = score;
            }
            path.cells.remove(nextCell.get());
        }
        return max;
    }

    private List<Way> getPossibleWays(Cell departureCell, int movesCount) {
        Optional<Cell> proposedCell;
        List<Way> ways = new ArrayList<>();
        for (Orientation orientation : Orientation.values()) {
            for (int i = movesCount; i > 0; i--) {
                proposedCell = this.grid.getNextCellForPlayerMove(departureCell, orientation, i);

                if (proposedCell.isPresent()) {
                    ways.add(new Way(i, orientation));
                }
            }
        }

        return ways;
    }
}
