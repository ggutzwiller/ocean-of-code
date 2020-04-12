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

}
