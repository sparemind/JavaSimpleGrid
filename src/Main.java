import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

/**
 * This class demos the features of SimpleGrid.
 * <p>
 * Click and drag to flip the color of any cell you move over.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // Setup grid to have 3 layers (default + two more)
        SimpleGrid grid = new SimpleGrid(10, 10, 50, 5, "SimpleGrid Testing");
        grid.setGridlineColor(Color.WHITE);
        grid.addLayer();
        grid.addLayer();
        grid.setAutoRepaint(false);

        // The cells that can be toggled on by clicking and dragging
        grid.setColor(1, Color.PINK);
        grid.setText(1, 'X');
        grid.setTextColor(1, Color.LIGHT_GRAY);
        grid.setImage(1, ImageIO.read(new File("knight.png")));

        // The highlighted cell under the mouse cursor. Appears over the toggleable layer.
        grid.setColor(-1, Color.CYAN);
        grid.setText(-1, 'O');
        grid.setTextColor(-1, Color.WHITE);

        // A filled in cell that will appear over everything else
        grid.setText(10, '1');
        grid.setColor(10, Color.GREEN);
        grid.setTextColor(10, Color.BLACK);
        grid.set(2, 0, 0, 10);

        // A text only cell that will appear over everything else.
        // Note how the color of this cell is the color of whatever cell is
        // beneath it, but the text never changes.
        grid.set(2, 1, 0, 11); // Note how cells can be set before their values are mapped
        grid.setText(11, '2');
        grid.setColor(11, null);
        grid.setTextColor(11, Color.RED);

        // An image only cell that will appear over everything else.
        // Note how the color of this cell is the color of whatever cell is
        // beneath it, but the image never changes.
        grid.setImage(12, ImageIO.read(new File("king.png")));
        grid.setColor(12, null);
        grid.set(2, 2, 0, 12);

        Point current = null;
        Point last = new Point(0, 0);
        while (true) {
            Point p = grid.getMousePosition();

            // Toggle cells beneath the mouse
            if (p != null && grid.isMouseDown() && !p.equals(current)) {
                grid.set(p, 1 - grid.get(p));
                current = p;
            }
            if (!grid.isMouseDown()) {
                current = null;
            }

            // Update hover highlighting
            grid.set(1, last, 0);
            grid.set(1, p, -1);
            last = p;

            grid.repaint();
        }
    }
}
