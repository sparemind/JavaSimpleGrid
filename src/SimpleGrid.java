import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows for the creation, management, and display of a simple 2D graphical grid.
 * <p>
 * Each grid box can be set to contain a single integer value. These values can be mapped to color,
 * which are then drawn in any box containing the corresponding value.
 *
 * @author Jake Chiang
 * @version 1.1
 */
public class SimpleGrid {
    public static final Color DEFAULT_COLOR = Color.WHITE;

    private GridPanel panel;
    private JFrame frame;
    private List<int[][]> grids;
    private Map<Integer, Color> colors;
    private boolean mouseDown;
    private boolean autoRepaint;

    /**
     * Create a new window containing a blank grid. The window will be automatically sized to fit
     * the grid and will be positioned in the center of the screen.
     * <p>
     * By default, all cells will have a value of 0, corresponding to white, and the gridlines will
     * be colored black.
     *
     * @param width          The width of the grid in cells.
     * @param height         The height of the grid in cells.
     * @param cellSize       The size of each cell in pixels.
     * @param gridlineWeight The width of the gridlines in pixels.
     * @param name           The window's name.
     */
    public SimpleGrid(int width, int height, int cellSize, int gridlineWeight, String name) {
        if (width <= 0 || height <= 0 || cellSize <= 0) {
            throw new IllegalArgumentException("Grid dimensions and cell sizes must be positive.");
        }
        this.panel = new GridPanel(width, height, cellSize, gridlineWeight);
        this.frame = new JFrame(name);
        this.grids = new ArrayList<>();
        addLayer(); // Create default grid layer
        this.colors = new HashMap<>();
        this.colors.put(0, DEFAULT_COLOR);
        this.mouseDown = false;
        this.autoRepaint = true;

        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.add(this.panel);
        this.frame.setVisible(true);
        this.frame.setResizable(false);
        int totalWidth = width * (cellSize + gridlineWeight) + gridlineWeight;
        int totalHeight = height * (cellSize + gridlineWeight) + gridlineWeight;
        Dimension size = new Dimension(totalWidth, totalHeight);
        this.panel.setPreferredSize(size);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null); // Center the frame

        this.frame.addMouseListener(new MouseListener());
    }

    /**
     * Returns the width of the grid in number of cells.
     *
     * @return Number of cells wide the grid is.
     * @since v1.1
     */
    public int getWidth() {
        return this.panel.width;
    }

    /**
     * Returns the height of the grid in number of cells.
     *
     * @return Number of cells high the grid is.
     * @since v1.1
     */
    public int getHeight() {
        return this.panel.height;
    }

    /**
     * Returns whether the given coordinates are out-of-bounds of the grid.
     *
     * @param pos The coordinates to check.
     * @return True if the given coordinates are out-of-bounds of the grid, or if the position is
     * null, false otherwise.
     * @see SimpleGrid#isOOB(int, int)
     */
    public boolean isOOB(Point pos) {
        if (pos == null) {
            return true;
        }
        return isOOB(pos.x, pos.y);
    }

    /**
     * Returns whether the given coordinates are out-of-bounds of the grid.
     *
     * @param x The x-coordinate to check.
     * @param y The y-coordinate to check.
     * @return True if the given coordinates are out-of-bounds of the grid, false otherwise.
     * @see SimpleGrid#isOOB(Point)
     */
    public boolean isOOB(int x, int y) {
        return x < 0 || y < 0 || x >= this.panel.width || y > this.panel.height;
    }

    /**
     * Returns the grid position of the mouse cursor.
     *
     * @return The xy-coordinates of the cell the mouse cursor is currently over. If the mouse is
     * out-of-bounds or over gridlines, returns null instead.
     */
    public Point getMousePosition() {
        return this.panel.getMouseCell();
    }

    /**
     * Returns whether the mouse is currently down.
     *
     * @return True if the mouse is currently down, false if the mouse is up.
     */
    public boolean isMouseDown() {
        return this.mouseDown;
    }

    /**
     * Creates a new grid layer on top of all others.
     */
    public void addLayer() {
        this.grids.add(new int[getHeight()][getWidth()]);
    }

    /**
     * Set whether to automatically repaint after a change is made.
     *
     * @param autoRepaint Whether to automatically repaint after the grid or its settings change.
     * @see SimpleGrid#repaint();
     * @since v1.1
     */
    public void setAutoRepaint(boolean autoRepaint) {
        this.autoRepaint = autoRepaint;
    }

    /**
     * Repaints the grid.
     *
     * @since v1.1
     */
    public void repaint() {
        this.panel.repaint();
    }

    /**
     * Set the cell at the given coordinates to a value. Sets the cell of the default grid (layer
     * 0).
     *
     * @param pos   The coordinates of the cell to set. If null, the grid will not be changed.
     * @param value The value to set the cell to.
     * @see SimpleGrid#set(int, int, int)
     * @see SimpleGrid#set(int, Point, int)
     */
    public void set(Point pos, int value) {
        set(0, pos, value);
    }

    /**
     * Set the cell at the given coordinates to a value. Sets the cell of the default grid (layer
     * 0).
     *
     * @param x     The x-coordinate of the cell to set.
     * @param y     The y-coordinate of the cell to set.
     * @param value The value to set the cell to.
     * @see SimpleGrid#set(Point, int)
     * @see SimpleGrid#set(int, int, int, int)
     */
    public void set(int x, int y, int value) {
        set(0, x, y, value);
    }

    /**
     * Set the cell at the given coordinates to a value.
     *
     * @param layer The layer of the cell to set
     * @param pos   The coordinates of the cell to set. If null, the grid will not be changed.
     * @param value The value to set the cell to.
     * @see SimpleGrid#set(int, int, int, int)
     * @since v1.1
     */
    public void set(int layer, Point pos, int value) {
        if (pos == null) {
            return;
        }
        set(layer, pos.x, pos.y, value);
    }

    /**
     * Set the cell at the given coordinates to a value. Repaints grid if auto repainting is
     * enabled.
     *
     * @param layer The layer of the cell to set. If not a valid layer, the grid will not be
     *              changed.
     * @param x     The x-coordinate of the cell to set.
     * @param y     The y-coordinate of the cell to set.
     * @param value The value to set the cell to.
     * @see SimpleGrid#set(int, Point, int)
     * @since v1.1
     */
    public void set(int layer, int x, int y, int value) {
        if (layer < 0 || layer > this.grids.size() - 1) {
            return;
        }
        this.grids.get(layer)[y][x] = value;
        if (!this.colors.containsKey(value)) {
            setColor(value, DEFAULT_COLOR);
        }
        if (this.autoRepaint) {
            // Only repaint this cell. Slightly faster than repainting everything with repaint()
            int cellX = x * (this.panel.cellSize + this.panel.gridlineWeight) + this.panel.gridlineWeight;
            int cellY = y * (this.panel.cellSize + this.panel.gridlineWeight) + this.panel.gridlineWeight;
            this.panel.repaint(cellX, cellY, this.panel.cellSize, this.panel.cellSize);
        }
    }

    /**
     * Returns the value of the cell at the given coordinates. Gets the cell of the default grid
     * (layer 0).
     *
     * @param pos The coordinates of the cell to get.
     * @return The value of the cell. If the given position is null, returns -1.
     * @throws NullPointerException If the given position is null.
     * @see SimpleGrid#get(int, int)
     * @see SimpleGrid#get(int, Point)
     */
    public int get(Point pos) {
        return get(0, pos);
    }

    /**
     * Returns the value of the cell at the given coordinates. Gets the cell of the default
     * grid (layer 0).
     *
     * @param x The x-coordinate of the cell to get.
     * @param y The y-coordinate of the cell to get.
     * @return The value of the cell.
     * @see SimpleGrid#get(Point)
     * @see SimpleGrid#get(int, int, int)
     */
    public int get(int x, int y) {
        return get(0, x, y);
    }

    /**
     * Returns the value of the cell at the given coordinates.
     *
     * @param layer The layer of the cell to get.
     * @param pos   The coordinates of the cell to get.
     * @return The value of the cell. If the given position is null, returns -1.
     * @throws NullPointerException If the given position is null.
     * @see SimpleGrid#get(int, int, int)
     * @since v1.1
     */
    public int get(int layer, Point pos) {
        if (pos == null) {
            throw new NullPointerException("Position must not be null");
        }
        return get(layer, pos.x, pos.y);
    }

    /**
     * Returns the value of the cell at the given coordinates.
     *
     * @param layer The layer of the cell to get.
     * @param x     The x-coordinate of the cell to get.
     * @param y     The y-coordinate of the cell to get.
     * @return The value of the cell.
     * @throws IllegalArgumentException If the given layer does not exist.
     * @see SimpleGrid#get(int, Point)
     * @since v1.1
     */
    public int get(int layer, int x, int y) {
        if (layer < 0 || layer > this.grids.size() - 1) {
            throw new IllegalArgumentException("Must specify a valid layer.");
        }
        return this.grids.get(layer)[y][x];
    }

    /**
     * Set the color of the gridlines. Repaints grid if auto repainting is enabled.
     *
     * @param color The color to set the gridlines to.
     */
    public void setGridlineColor(Color color) {
        this.panel.setBackground(color);
        if (this.autoRepaint) {
            this.panel.repaint();
        }
    }

    /**
     * Assigns a color to a given value. All cells with this value will be colored this color.
     * Repaints grid if auto repainting is enabled.
     *
     * @param value The value to set the color of.
     * @param color The color to be assigned to the value.
     */
    public void setColor(int value, Color color) {
        this.colors.put(value, color);
        if (this.autoRepaint) {
            this.panel.repaint();
        }
    }

    /**
     * This class paints the grid and can give information on the grid settings and mouse cursor.
     */
    private class GridPanel extends JPanel {
        private static final long serialVersionUID = 4114771226550991401L;

        private int width;
        private int height;
        private int cellSize;
        private int gridlineWeight;

        /**
         * Creates a new blank grid.
         * <p>
         * By default, the gridlines will be black.
         *
         * @param width          The width of the grid in cells.
         * @param height         The height of the grid in cells.
         * @param cellSize       The size of each cell in pixels.
         * @param gridlineWeight The width of the gridlines in pixels.
         */
        public GridPanel(int width, int height, int cellSize, int gridlineWeight) {
            super();

            this.width = width;
            this.height = height;
            this.cellSize = cellSize;
            this.gridlineWeight = gridlineWeight;
            setBackground(Color.BLACK);
        }

        /**
         * Paints the entire grid. The grid cells are colored according to the colors assigned to
         * their current values. If layers exist above the default one, then only the topmost
         * non-zero cell among them will be painted.
         *
         * @param g The graphics object that the grid will be painted with.
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int x = 0; x < this.width; x++) {
                for (int y = 0; y < this.height; y++) {
                    g.setColor(SimpleGrid.this.colors.get(SimpleGrid.this.grids.get(0)[y][x]));
                    for (int i = 1; i < SimpleGrid.this.grids.size(); i++) {
                        int value = SimpleGrid.this.grids.get(i)[y][x];
                        if (value != 0) {
                            g.setColor(SimpleGrid.this.colors.get(value));
                        }
                    }
                    int cellX = x * (this.cellSize + this.gridlineWeight) + this.gridlineWeight;
                    int cellY = y * (this.cellSize + this.gridlineWeight) + this.gridlineWeight;
                    g.fillRect(cellX, cellY, this.cellSize, this.cellSize);
                }
            }
        }

        /**
         * Returns the grid position of the mouse cursor.
         *
         * @return The xy-coordinates of the cell the mouse cursor is currently over. If the mouse
         * is out-of-bounds or over gridlines, returns null instead.
         */
        public Point getMouseCell() {
            Point pos = getMousePosition();

            // Case 1: Mouse is out of the window
            if (pos == null) {
                return null;
            } else {
                // Grid cell coordinates
                int x = pos.x / (this.cellSize + this.gridlineWeight);
                int y = pos.y / (this.cellSize + this.gridlineWeight);
                // Cell local pixel coordinates
                int cellX = pos.x % (this.cellSize + this.gridlineWeight);
                int cellY = pos.y % (this.cellSize + this.gridlineWeight);

                // Case 2: Mouse is inside cell
                if (cellX > this.gridlineWeight && cellY > this.gridlineWeight) {
                    return new Point(x, y);
                } else {
                    // Case 3: Mouse is on gridline
                    return null;
                }
            }
        }
    }

    /**
     * This class monitors mouse activity, such as pressing and releasing the mouse button.
     */
    private class MouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            SimpleGrid.this.mouseDown = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            SimpleGrid.this.mouseDown = false;
        }
    }
}
