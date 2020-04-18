package com.ggutzwiller.strategy;

import com.ggutzwiller.io.Printer;
import com.ggutzwiller.model.*;

import java.util.*;

/**
 * The movement manager computes all the possible moments and knows where to go.
 */
public class MovementManager {
    private static final int NUMBER_OF_MOVEMENT_TO_FORESEE = 10;

    public Grid grid;
    public Cell currentCell;
    public Submarine playerSubmarine;
    public Path playerPath;

    public MovementManager(Grid grid, Cell currentCell, Submarine playerSubmarine) {
        this.grid = grid;
        this.currentCell = currentCell;
        this.playerSubmarine = playerSubmarine;
        this.playerPath = new Path(currentCell);
    }

    /**
     * Choose the way to go to given the distance we can go to
     */
    public Optional<Way> chooseMovement(int movesCount) {
        Cell currentCell = this.playerSubmarine.cell;
        this.playerPath.addCell(currentCell);

        List<Way> possibleWays = getPossibleWays(playerPath, movesCount);

        if (possibleWays.isEmpty()) {
            Printer.log("There is no possible ways, let's surface.");
            this.playerPath = new Path(this.playerSubmarine.cell);
            return Optional.empty();
        } else {
            Path path = new Path(this.playerPath);
            Way way = findBestWay(path, possibleWays);

            this.grid.applyChosenWayOnPath(this.playerPath, way);
            this.playerSubmarine.cell = this.playerPath.lastCell;

            return Optional.of(way);
        }
    }

    /**
     * Find the best way among all the possible ways
     * @param path the current Path of our submarine
     * @param possibleWays the possible ways
     * @return the best way
     */
    private Way findBestWay(Path path, List<Way> possibleWays) {
        Map<Way, Integer> scoresPerWay = new HashMap<>();

        for (Way way : possibleWays) {
            int score = computeScoreForWay(new Path(path), way, NUMBER_OF_MOVEMENT_TO_FORESEE);
            Printer.log("Way " + way.toString() + " has score " + score);
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

    /**
     * Compute a score (0 to NUMBER_OF_MOVEMENT_TO_FORESEE) for a given way. Higher the score, better the way.
     * @param path the current Path of our submarine
     * @param way the way to evaluate
     * @param depth the depth (ie. it is a recursive function)
     * @return the score for the given way
     */
    private int computeScoreForWay(Path path, Way way, int depth) {
        if (depth == 0) {
            return NUMBER_OF_MOVEMENT_TO_FORESEE;
        }

        this.grid.applyChosenWayOnPath(path, way);

        List<Way> possibleWays = getPossibleWays(path, way.distance);
        if (possibleWays.isEmpty()) {
            return NUMBER_OF_MOVEMENT_TO_FORESEE - depth;
        }

        int max = 0;
        for (Way nextWay : possibleWays) {
            Path nextPath = new Path(path);
            int score = computeScoreForWay(nextPath, nextWay, depth - 1);

            if (score == NUMBER_OF_MOVEMENT_TO_FORESEE) {
                return score;
            } else if (score > max) {
                max = score;
            }
        }
        return max;
    }

    /**
     * Returns all the possible ways given a path and the maximum number of moves we can do
     * @param path the path of current submarine
     * @param movesCount the maximum number of moves we can do
     * @return the list of possible ways. We keep it ordered to avoid erratic movements.
     */
    private List<Way> getPossibleWays(Path path, int movesCount) {
        Optional<Cell> proposedCell;
        List<Way> ways = new ArrayList<>();

        List<Integer> distances = getPossibleDistances(movesCount);

        for (Orientation orientation : Orientation.values()) {
            for (Integer i : distances) {
                proposedCell = this.grid.applyWay(path, new Way(i, orientation));

                if (proposedCell.isPresent()) {
                    ways.add(new Way(i, orientation));
                }
            }
        }

        return ways;
    }

    private List<Integer> getPossibleDistances(int maxDistance) {
        List<Integer> possibleDistances = new ArrayList<>();
        for (int i = maxDistance; i > 0; i--) {
            possibleDistances.add(i);
        }
        Collections.shuffle(possibleDistances);
        return possibleDistances;
    }
}
