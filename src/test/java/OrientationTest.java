import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Grégoire Gutzwiller
 * @since 28/03/2020
 */
public class OrientationTest {

    @Test
    public void left_shouldWork() {
        Orientation north = Orientation.NORTH;

        Orientation left = north.left();
        Assert.assertEquals(left, Orientation.WEST);

        left = left.left();
        Assert.assertEquals(left, Orientation.SOUTH);

        left = left.left();
        Assert.assertEquals(left, Orientation.EAST);
    }

    @Test
    public void right_shouldWork() {
        Orientation north = Orientation.NORTH;

        Orientation right = north.right();
        Assert.assertEquals(right, Orientation.EAST);

        right = right.right();
        Assert.assertEquals(right, Orientation.SOUTH);

        right = right.right();
        Assert.assertEquals(right, Orientation.WEST);
    }

    @Test
    public void byLabel_shouldWork() {
        Orientation found = Orientation.byLabel("N");
        Assert.assertEquals(found, Orientation.NORTH);

        found = Orientation.byLabel("E");
        Assert.assertEquals(found, Orientation.EAST);

        found = Orientation.byLabel("W");
        Assert.assertEquals(found, Orientation.WEST);

        found = Orientation.byLabel("S");
        Assert.assertEquals(found, Orientation.SOUTH);
    }
}
