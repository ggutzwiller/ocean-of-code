package com.ggutzwiller.model;

import com.ggutzwiller.model.Cell;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Grégoire Gutzwiller
 * @since 29/03/2020
 */
public class CellTest {

    @Test
    public void distance_shouldWork() {
        Cell cell1 = new Cell(true, 2, 2, 1);
        Cell cell2 = new Cell(true, 4, 4, 1);

        int distance = cell1.distance(cell2);

        Assert.assertEquals(distance, 4);
    }

    @Test
    public void torpedoDamages_sameCell_shouldReturn2() {
        Cell cell = new Cell(false, 2, 2, 1);

        int damages = cell.torpedoDamages(cell);

        Assert.assertEquals(damages, 2);
    }

    @Test
    public void torpedoDamages_rangeCell_shouldReturn1() {
        Cell cell1 = new Cell(false, 2, 2, 1);
        Cell cell2 = new Cell(false, 3, 3, 1);

        int damages = cell1.torpedoDamages(cell2);

        Assert.assertEquals(damages, 1);
    }

    @Test
    public void torpedoDamages_rangeCell_shouldReturn0() {
        Cell cell1 = new Cell(false, 2, 2, 1);
        Cell cell2 = new Cell(false, 4, 2, 1);

        int damages = cell1.torpedoDamages(cell2);

        Assert.assertEquals(damages, 0);
    }

    @Test
    public void orientationToGoTo_neighboorCell_shouldReturnE() {
        Cell cell1 = new Cell(false, 2, 2, 1);
        Cell cell2 = new Cell(false, 3, 2, 1);

        Orientation orientation = cell1.orientationToGoTo(cell2);

        Assert.assertEquals(orientation, Orientation.EAST);
    }

}
