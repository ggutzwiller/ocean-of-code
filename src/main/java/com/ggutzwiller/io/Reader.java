package com.ggutzwiller.io;

import com.ggutzwiller.model.Grid;
import com.ggutzwiller.model.Submarine;
import com.ggutzwiller.strategy.Game;
import com.ggutzwiller.strategy.MovementManager;
import com.ggutzwiller.strategy.OpponentPositionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Reads input and create or update game accordingly.
 */
public class Reader {
    public static Game readFirstInputs(Scanner in) {
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

    public static void readTurnInputs(Game game, Scanner in) {
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
