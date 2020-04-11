import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Grégoire Gutzwiller
 * @since 05/04/2020
 */
public class OrderListTest {

    @Test
    public void addOrders_shouldWork() {
        List<String> orders = List.of(
                "MOVE S",
                "MOVE S",
                "MOVE E | TORPEDO 3 5",
                "MOVE N"
        );
        OrderList orderList = new OrderList();

        orders.forEach(orderList::addOrders);

        Assert.assertEquals(orderList.orientations, List.of(
                Orientation.SOUTH,
                Orientation.SOUTH,
                Orientation.EAST,
                Orientation.NORTH
        ));
    }

}
