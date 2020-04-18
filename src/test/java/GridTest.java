import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Grégoire Gutzwiller
 * @since 28/03/2020
 */
public class GridTest {

    @Test
    public void constructor_shouldWork() {
        int height = 5;
        int width = 5;
        List<String> lines = List.of(
                "xx..x",
                ".....",
                "..xx.",
                "x....",
                "xx..."
        );

        Grid grid = new Grid(width, height, lines);

        Assert.assertTrue(grid.cells[0][0].taken);
        Assert.assertTrue(grid.cells[1][0].taken);
        Assert.assertFalse(grid.cells[0][1].taken);
        Assert.assertFalse(grid.cells[2][3].taken);
        Assert.assertTrue(grid.cells[2][2].taken);
    }

    @Test
    public void retrievePossibleOpponentCells_simpleCase_shouldWork() {
        int height = 5;
        int width = 5;
        List<String> lines = List.of(
                "xx..x",
                ".....",
                "..xx.",
                "x....",
                "xx..."
        );
        Grid grid = new Grid(width, height, lines);
        OrderList orderList = new OrderList();
        orderList.addOrders("MOVE S");
        orderList.addOrders("MOVE S");
        orderList.addOrders("MOVE E | TORPEDO 3 5");

        List<Cell> possibleCells = grid.retrievePossibleOpponentCells(orderList);

        Assert.assertEquals(possibleCells, List.of(grid.cells[2][3]));
    }

    @Test
    public void retrievePossibleOpponentCells_complexCase_shouldWork() {
        int height = 15;
        int width = 15;
        List<String> lines = List.of(
                "............xxx",
                "......xx....xx.",
                "......xx.......",
                "...............",
                "...............",
                "..xx...........",
                "..xx......xx.xx",
                "..........xx.xx",
                "...............",
                "...............",
                "...............",
                "...xx..........",
                "...xx....xxxx..",
                ".........xxxx..",
                ".........xxx..."
        );
        Grid grid = new Grid(width, height, lines);
        OrderList orderList = new OrderList();
        orderList.addOrders("MOVE S");
        orderList.addOrders("MOVE S");
        orderList.addOrders("MOVE S");
        orderList.addOrders("MOVE S");
        orderList.addOrders("MOVE E | TORPEDO 3 5");

        List<Cell> possibleCells = grid.retrievePossibleOpponentCells(orderList);

        System.out.println(possibleCells.stream().map(Cell::toString).collect(Collectors.joining(" - ")));
    }

}
