import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Grégoire Gutzwiller
 * @since 28/03/2020
 */
public class GameTest {

    @Test
    public void chooseStartingPosition_shouldWork() {
        Game game = new Game();
        game.grid = new Grid(5, 5, List.of(
                "xx..x",
                ".....",
                "..xx.",
                "x....",
                "xx..."
        ));

        String startingPosition = game.chooseStartingPosition();

        List<String> possibleResults = List.of(
                "1 1",
                "1 2",
                "2 1",
                "3 1",
                "1 3",
                "2 3",
                "3 3"
        );

        Assert.assertTrue(possibleResults.contains("1 1"));
    }

}
