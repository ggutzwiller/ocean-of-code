package com.ggutzwiller;

import com.ggutzwiller.io.Printer;
import com.ggutzwiller.io.Reader;
import com.ggutzwiller.strategy.Game;

import java.util.Scanner;

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

