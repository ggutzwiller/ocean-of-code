import java.text.CollationElementIterator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Grégoire Gutzwiller
 * @since 27/03/2020
 */
class Player {

    public static void main(String args[]) {
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

class Game {
    Submarine playerSubmarine;
    Submarine enemySubmarine;
    Grid grid;
    OrderList opponentOrders = new OrderList();

    String chooseStartingPosition() {
        Cell startCell = Arrays.stream(this.grid.cells)
                .flatMap(s -> Arrays.stream(s))
                .filter(c -> !c.taken)
                .filter(c -> c.posX > 0 && c.posX < grid.width - 1 && c.posY > 0 && c.posY < grid.height - 1)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Collections.shuffle(list);
                            return list.stream();
                        }
                ))
                .findAny()
                .orElse(this.grid.cells[0][0]);

        Printer.log("We choose starting position: " + startCell.toString());

        return startCell.toString();
    }

    String playTurn() {
        String movement = chooseMovement();
        String action = chooseAction();

        if (action.equals("")) {
            return movement;
        }

        return movement + " | " + action;
    }

    String chooseMovement() {
        Cell currentCell = this.playerSubmarine.cell;
        currentCell.taken = true;

        Optional<Cell> nextCell = Optional.empty();
        Optional<Orientation> chosenOrientation = Optional.empty();
        List<Orientation> orientations = Orientation.all();

        for (Orientation orientation : orientations) {
            nextCell = this.grid.findNextCell(currentCell, orientation);

            if (nextCell.isPresent() && !nextCell.get().taken) {
                chosenOrientation = Optional.of(orientation);
                break;
            }
        }

        if (!nextCell.isPresent() || nextCell.get().taken) {
            Printer.log("Next cell is taken, " + nextCell.toString());
            this.grid.reset();
            return "SURFACE";
        } else {
            return "MOVE " + chosenOrientation.get().label + " " + chooseCharge();
        }
    }

    String chooseCharge() {
        return this.playerSubmarine.torpedoCooldown == 0 ? "SILENCE" : "TORPEDO";
    }

    String chooseAction() {
        Printer.log("Torpedo cooldown: " + this.playerSubmarine.torpedoCooldown);
        Printer.log("Orders from the enemy : " + this.opponentOrders.toString());

        if (this.playerSubmarine.torpedoCooldown != 0) {
            return "";
        }

        List<Cell> possibleOpponentCells = this.grid.retrievePossibleOpponentCells(this.opponentOrders);
        Printer.log("Possible opponents cells: " + possibleOpponentCells.stream().map(Cell::toString).collect(Collectors.joining("-")));

        List<Cell> possibleTargetCells = this.grid.retrieveDistantCells(this.playerSubmarine.cell, 3, 4);
        Printer.log("Possible target cells: " + possibleTargetCells.stream().map(Cell::toString).collect(Collectors.joining("-")));

        Optional<Cell> targetCell = possibleTargetCells.stream()
                .filter(possibleOpponentCells::contains)
                .peek(c -> Printer.log("Possible cell: " + c))
                .findAny();

        return targetCell.map(cell -> "TORPEDO " + cell.toString()).orElse("");
    }
}

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
        for (int i = 0; i < height; i++) {
            String line = lines.get(i);

            for (int j = 0; j < line.length(); j++) {
                cells[j][i] = new Cell(line.charAt(j) == 'x', j, i);
            }
        }
    }

    void reset() {
        Arrays.stream(cells)
                .flatMap(s -> Arrays.stream(s))
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

    List<Cell> retrievePossibleOpponentCells(OrderList orderList) {
        List<Cell> possibleOpponentCells = Arrays.stream(this.cells)
                .flatMap(Arrays::stream)
                .filter(c -> !c.initiallyTaken)
                .flatMap(cell -> findNextCellsForOrders(cell, orderList, 0).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Collections.shuffle(possibleOpponentCells);
        return possibleOpponentCells;
    }

    List<Cell> findNextCellsForOrders(Cell cell, OrderList orderList, int depth) {
        if (orderList.orientations.size() <= depth) {
            return Collections.singletonList(cell);
        } else if (orderList.orientations.get(depth) == null) {
            List<Cell> possibleNextCells = new ArrayList<>();

            for (Orientation orientation : Orientation.all()) {
                Optional<Cell> nextCell = findNextCell(cell, orientation);
                if (nextCell.isPresent() && !nextCell.get().initiallyTaken) {
                    possibleNextCells.addAll(findNextCellsForOrders(nextCell.get(), orderList, depth + 1));
                }
            }

            return possibleNextCells;
        } else {
            Optional<Cell> nextCell = findNextCell(cell, orderList.orientations.get(depth));
            if (!nextCell.isPresent() || nextCell.get().initiallyTaken) {
                return new ArrayList<>();
            } else {
                return findNextCellsForOrders(nextCell.get(), orderList, depth + 1);
            }
        }
    }


    Optional<Cell> findNextCell(Cell cell, Orientation orientation) {
        int nextX = cell.posX + orientation.forwardX;
        int nextY = cell.posY + orientation.forwardY;

        if (!(nextX < 0 || nextX > width - 1 || nextY < 0 || nextY > height - 1)) {
            return Optional.of(this.cells[nextX][nextY]);
        }

        return Optional.empty();
    }
}

class OrderList {
    List<Orientation> orientations = new ArrayList<>();

    void addOrders(String orders) {
        Arrays.stream(orders.split("\\|"))
                .map(String::trim)
                .forEach(o -> {
                    if (o.contains("MOVE")) {
                        this.orientations.add(Orientation.byLabel(o.replace("MOVE", "").trim()));
                    } else if (o.contains("SILENCE")) {
                        this.orientations.add(null);
                    }
                });
    }

    public String toString() {
        return orientations.stream()
                .map(o -> o != null ? o.label : "R")
                .collect(Collectors.joining(" - "));
    }
}

class Cell {
    boolean taken;
    boolean initiallyTaken;
    int posX;
    int posY;

    Cell(boolean taken, int posX, int posY) {
        this.taken = taken;
        this.posX = posX;
        this.posY = posY;
        this.initiallyTaken = taken;
    }

    void reset() {
        this.taken = initiallyTaken;
    }

    public String toString() {
        return this.posX + " " + this.posY;
    }

    int distance(Cell cell) {
        return Math.abs(this.posY - cell.posY) + Math.abs(this.posX - cell.posX);
    }
}

enum Orientation {
    NORTH("N", 0, -1, "W", "E"),
    EAST("E", 1, 0, "N", "S"),
    WEST("W", -1, 0, "S", "N"),
    SOUTH("S", 0, 1, "E", "W");

    public String label;
    public int forwardX;
    public int forwardY;
    public String left;
    public String right;
    private static List<Orientation> allOrientations = Arrays.asList(
            Orientation.SOUTH,
            Orientation.NORTH,
            Orientation.EAST,
            Orientation.WEST
    );

    Orientation(String label, int forwardX, int forwardY, String left, String right) {
        this.label = label;
        this.forwardX = forwardX;
        this.forwardY = forwardY;
        this.left = left;
        this.right = right;
    }

    Orientation right() {
            return byLabel(this.right);
    }

    Orientation left() {
            return byLabel(this.left);
    }

    static List<Orientation> all() {
        Collections.shuffle(allOrientations);
        return allOrientations;
    }

    static Orientation byLabel(String label) {
        return Arrays.stream(values())
                .filter(v -> v.label.equals(label))
                .findFirst()
                .get();
    }
}

class Printer {
    static void log(String message) {
        System.err.println(message);
    }

    static void order(String message) {
        System.out.println(message);
    }
}

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

        return game;
    }

    static void readTurnInputs(Game game, Scanner in) {
        game.playerSubmarine.cell = game.grid.cells[in.nextInt()][in.nextInt()];

        game.playerSubmarine.life = in.nextInt();
        game.enemySubmarine.life = in.nextInt();

        game.playerSubmarine.torpedoCooldown = in.nextInt();
        game.playerSubmarine.sonarCooldown = in.nextInt();
        game.playerSubmarine.silenceCooldown = in.nextInt();
        game.playerSubmarine.mineCooldown = in.nextInt();

        in.nextLine();
        in.nextLine();
        game.opponentOrders.addOrders(in.nextLine());
    }
}
