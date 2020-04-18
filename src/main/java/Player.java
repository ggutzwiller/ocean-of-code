import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Grégoire Gutzwiller
 * @since 27/03/2020
 */
class Player {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        Game game = Reader.readFirstInputs(in);
        String startingPosition = game.chooseStartingPosition();
        Printer.order(startingPosition);

        /* GAME LOOP */
        while (true) {
            Reader.readTurnInputs(game, in);
            String turnOrder = game.playTurn();
            Printer.order(turnOrder);
        }
    }
}

/**
 * Handle all the game logic, ie. choosing the start position and then play each turn according to logic.
 */
class Game {
    static final int NUMBER_OF_MOVEMENT_TO_FORESEE = 10;

    Submarine playerSubmarine;
    Submarine enemySubmarine;
    Grid grid;
    OpponentPositionManager opponentPositionManager;

    String chooseStartingPosition() {
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
    String playTurn() {
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

            if (way.distance == 1) {
                return "MOVE " + way.orientation.label + " " + chooseCharge();
            } else {
                Optional<Cell> cell = Optional.of(currentCell);
                for (int i = 1; i < way.distance; i++) {
                    cell = this.grid.applyOrientation(cell.get(), way.orientation);
                    cell.get().taken = true;
                }

                return "SILENCE " + way.orientation.label + " " + way.distance;
            }
        }
    }

    private Way findBestWay(Cell departureCell, List<Way> possibleWays) {
        Printer.log("FIND BEST WAY - start");
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
                .sorted((way1, way2) -> {
                    if (!way1.getValue().equals(way2.getValue())) {
                        return way2.getValue() - way1.getValue();
                    } else if (!way1.getKey().orientation.equals(way2.getKey().orientation)) {
                        return way2.getKey().orientation.compare(way1.getKey().orientation);
                    } else {
                        return way2.getKey().distance - way1.getKey().distance;
                    }
                })
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
            nextCell.get().taken = true;
            int score = computeScoreForWay(nextCell.get(), nextWay, depth - 1);
            if (score == NUMBER_OF_MOVEMENT_TO_FORESEE) {
                nextCell.get().taken = false;
                return score;
            } else if (score > max) {
                max = score;
            }
            nextCell.get().taken = false;
        }
        return max;
    }



    private List<Way> getPossibleWays(Cell departureCell, int movesCount) {
        Optional<Cell> proposedCell;
        List<Way> ways = new ArrayList<>();
        for (Orientation orientation : Orientation.values()) {
            for (int i = movesCount; i > 0; i--) {
                proposedCell = this.grid.getNextCellForMove(departureCell, orientation, i);

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

    // TODO: torpedo is not the only action now. + should we manage movement here?
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


        return targetCell.map(cell -> {
            this.opponentPositionManager.lastShot = cell;
            return "TORPEDO " + cell.toString();
        }).orElse("");
    }
}

/**
 * A submarine is a representation of the player's submarine.
 */
class Submarine {
    int id;
    Cell cell;
    int life;
    int torpedoCooldown;
    int sonarCooldown;
    int silenceCooldown;
    int mineCooldown;
}

class Grid {
    int width;
    int height;
    Cell[][] cells;

    Grid(int width, int height, List<String> lines) {
        Printer.log("Now creating grid (" + width + ", " + height + ").");
        Printer.log("Input lines are:\n" + String.join("\n", lines));
        this.width = width;
        this.height = height;

        this.cells = new Cell[width][height];
        int sectorSize = width / 3;
        for (int i = 0; i < height; i++) {
            String line = lines.get(i);

            for (int j = 0; j < line.length(); j++) {
                int sector = 1 + j / sectorSize + i / sectorSize * 3;
                cells[j][i] = new Cell(line.charAt(j) == 'x', j, i, sector);
            }
        }
    }

    void reset() {
        Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .forEach(Cell::reset);
    }

    List<Cell> retrieveDistantCells(Cell currentCell, int min, int max) {
        List<Cell> distantCells = Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .filter(c -> (c.distance(currentCell) <= max) && c.distance(currentCell) >= min)
                .collect(Collectors.toList());

        Collections.shuffle(distantCells);
        return distantCells;
    }

    /* TODO: three following methods may be refactored to be more efficient and clearer;
     *  + anyway findNextCellsForOrders will probably be removed with orders refactor
     */
    Optional<Cell> getNextCellForMove(Cell initialCell, Orientation orientation, int movesCount) {
        Optional<Cell> nextCell = this.applyOrientation(initialCell, orientation);

        if (!nextCell.isPresent() || nextCell.get().island || nextCell.get().taken) {
            return Optional.empty();
        } else if (movesCount == 1) {
            return nextCell;
        }

        return getNextCellForMove(nextCell.get(), orientation, movesCount - 1);
    }

    Optional<Cell> applyOrientation(Cell departureCell, Orientation orientation) {
        return applyWay(departureCell, new Way(1, orientation));
    }

    Optional<Cell> applyWay(Cell departureCell, Way way) {
        int nextX = departureCell.posX + (way.orientation.forwardX * way.distance);
        int nextY = departureCell.posY + (way.orientation.forwardY * way.distance);

        if (!(nextX < 0 || nextX > width - 1 || nextY < 0 || nextY > height - 1)) {
            return Optional.of(this.cells[nextX][nextY]);
        }

        return Optional.empty();
    }
}

/**
 * This class manages the computation of the possible opponent positions.
 */
class OpponentPositionManager {
    Grid grid;
    List<Cell> possibleOpponentCells;
    Cell lastShot;

    OpponentPositionManager(Grid grid) {
        this.grid = grid;
        this.possibleOpponentCells = Arrays.stream(grid.cells)
                .flatMap(Arrays::stream)
                .filter(cell -> !cell.island)
                .collect(Collectors.toList());
    }

    void recomputePositions(String orders, int opponentLifeLost) {
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

    void handleTouchedOpponent(int opponentLifeLost) {
        if (lastShot != null && opponentLifeLost == 2) {
            this.possibleOpponentCells = Collections.singletonList(this.lastShot);
        }
    }

    void handleMoveOrder(Orientation orientation) {
        this.possibleOpponentCells = this.possibleOpponentCells.stream()
                .map(cell -> this.grid.applyOrientation(cell, orientation))
                .filter(cell -> cell.isPresent() && !cell.get().island)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
    }

    void handleSilenceOrder() {
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

class Way {
    int distance;
    Orientation orientation;

    Way(int distance, Orientation orientation) {
        this.distance = distance;
        this.orientation = orientation;
    }

    @Override
    public String toString() {
        return orientation.label + distance;
    }
}

/**
 * Represents a cell in the grid. Can be taken if we are not allowed to go on it.
 */
class Cell {
    boolean taken;
    boolean island;
    int posX;
    int posY;
    int sector;

    Cell(boolean island, int posX, int posY, int sector) {
        this.taken = island;
        this.posX = posX;
        this.posY = posY;
        this.island = island;
        this.sector = sector;
    }

    void reset() {
        this.taken = island;
    }

    public String toString() {
        return this.posX + " " + this.posY;
    }

    int distance(Cell cell) {
        return Math.abs(this.posY - cell.posY) + Math.abs(this.posX - cell.posX);
    }
}

/**
 * Orientation represents an orientation, either North, East, West or South.
 */
enum Orientation {
    NORTH("N", 0, -1, "W", "E", "S", 3),
    EAST("E", 1, 0, "N", "S", "W", 2),
    WEST("W", -1, 0, "S", "N", "E", 1),
    SOUTH("S", 0, 1, "E", "W", "N", 0);

    public String label;
    public int forwardX;
    public int forwardY;
    public String left;
    public String right;
    public String opposite;
    public int order;

    Orientation(String label, int forwardX, int forwardY, String left, String right, String opposite, int order) {
        this.label = label;
        this.forwardX = forwardX;
        this.forwardY = forwardY;
        this.left = left;
        this.right = right;
        this.opposite = opposite;
        this.order = order;
    }

    Orientation right() {
            return byLabel(this.right);
    }

    Orientation left() {
            return byLabel(this.left);
    }

    Orientation opposite() {
        return byLabel(this.opposite);
    }

    int compare(Orientation orientation) {
        return this.order - orientation.order;
    }

    static Orientation byLabel(String label) {
        return Arrays.stream(values())
                .filter(v -> v.label.equals(label))
                .findFirst()
                .get();
    }
}

/**
 * Prints details to output and errors.
 */
class Printer {
    static void log(String message) {
        System.err.println(message);
    }

    static void order(String message) {
        System.out.println(message);
    }
}

/**
 * Reads input and create or update game accordingly.
 */
class Reader {
    static Game readFirstInputs(Scanner in) {
        Game game = new Game();
        game.playerSubmarine = new Submarine();
        game.enemySubmarine = new Submarine();

        int width = in.nextInt();
        int height = in.nextInt();
        game.playerSubmarine.id = in.nextInt();
        game.enemySubmarine.id = game.playerSubmarine.id == 0 ? 1 : 0;
        in.nextLine();

        List<String> gridLines = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            gridLines.add(in.nextLine());
        }

        game.grid = new Grid(width, height, gridLines);
        game.opponentPositionManager = new OpponentPositionManager(game.grid);

        return game;
    }

    static void readTurnInputs(Game game, Scanner in) {
        game.playerSubmarine.cell = game.grid.cells[in.nextInt()][in.nextInt()];

        game.playerSubmarine.life = in.nextInt();
        int prevEnemyLife = game.enemySubmarine.life;
        game.enemySubmarine.life = in.nextInt();


        game.playerSubmarine.torpedoCooldown = in.nextInt();
        game.playerSubmarine.sonarCooldown = in.nextInt();
        game.playerSubmarine.silenceCooldown = in.nextInt();
        game.playerSubmarine.mineCooldown = in.nextInt();

        in.nextLine();
        in.nextLine();
        game.opponentPositionManager.recomputePositions(in.nextLine(), prevEnemyLife - game.enemySubmarine.life);
    }
}
