import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MIT License
 * <p>
 * Copyright (c) 2017 Jake Chiang
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * <p>
 * This class allows for the creation, management, and display of a simple 2D
 * graphical grid.
 * <p>
 * Each grid box can be set to contain a single integer value. These values can
 * be mapped to color, text, and text color, which are then drawn in any box
 * containing the corresponding value.
 *
 * @author Jake Chiang
 * @version 1.3.2
 */
public class SimpleGrid {
    private GridPanel panel;
    private JFrame frame;
    private List<int[][]> grids;
    private Map<Integer, ValueData> valueData;
    private volatile boolean mouseDown;
    private boolean autoRepaint;

    /**
     * Create a new window containing a blank grid. The window will be
     * automatically sized to fit the grid and will be positioned in the center
     * of the screen.
     * <p>
     * By default, all cells will have a value of 0 and the gridlines will be
     * colored black. By default, 0 will also be mapped to have no text, black
     * text color, and "null" cell color. This cell color will appear white on
     * the default grid layer (layer 0) and transparent on higher layers. It is
     * not recommended you change this color, since then lower layers may not be
     * visible.
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
        this.valueData = new HashMap<>();
        this.valueData.put(0, new ValueData(null, ValueData.DEFAULT_TEXT_COLOR, '\0', null));
        this.mouseDown = false;
        this.autoRepaint = true;

        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
     * Returns the JFrame that the grid is drawn on.
     *
     * @return The frame that the grid is drawn on.
     * @since v1.2.1
     */
    public JFrame getFrame() {
        return this.frame;
    }

    /**
     * Returns whether the given coordinates are out-of-bounds of the grid.
     *
     * @param pos The coordinates to check.
     * @return True if the given coordinates are out-of-bounds of the grid or if
     * the position is null, false otherwise.
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
     * @return True if the given coordinates are out-of-bounds of the grid,
     * false otherwise.
     * @see SimpleGrid#isOOB(Point)
     */
    public boolean isOOB(int x, int y) {
        return x < 0 || y < 0 || x >= this.panel.width || y >= this.panel.height;
    }

    /**
     * Returns the grid position of the mouse cursor.
     *
     * @return The xy-coordinates of the cell the mouse cursor is currently
     * over. If the mouse is out-of-bounds or over gridlines, returns null
     * instead.
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
     * Creates a new grid layer on top of all others. All cells in this layer
     * will have value 0.
     *
     * @since v1.1
     */
    public void addLayer() {
        this.grids.add(new int[getHeight()][getWidth()]);
    }

    /**
     * Set whether to automatically repaint after a change is made. By default,
     * auto repainting is enabled.
     *
     * @param autoRepaint Whether to automatically repaint after the grid or its
     *                    settings change.
     * @see SimpleGrid#repaint()
     * @since v1.1
     */
    public void setAutoRepaint(boolean autoRepaint) {
        this.autoRepaint = autoRepaint;
    }

    /**
     * Repaints the grid if auto repainting is enabled.
     *
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since v1.2
     */
    private void tryRepaint() {
        if (this.autoRepaint) {
            this.panel.repaint();
        }
    }

    /**
     * Repaints the given area of the grid if auto repainting is enabled.
     *
     * @param x The x-coordinate of the rectangular area to repaint.
     * @param y The y-coordinate of the rectangular area to repaint.
     * @param w The width of the rectangular area to repaint.
     * @param h The height of the rectangular area to repaint.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since v1.2
     */
    private void tryRepaint(int x, int y, int w, int h) {
        if (this.autoRepaint) {
            this.panel.repaint(x, y, w, h);
        }
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
     * Set the cell at the given coordinates to a value. Sets the cell of the
     * default grid (layer 0).
     *
     * @param pos   The coordinates of the cell to set. If null, the grid will
     *              not be changed.
     * @param value The value to set the cell to.
     * @throws GridIndexOutOfBoundsException If the given coordinates are out of
     *                                       bounds.
     * @see SimpleGrid#set(int, int, int)
     * @see SimpleGrid#set(int, Point, int)
     */
    public void set(Point pos, int value) {
        set(0, pos, value);
    }

    /**
     * Set the cell at the given coordinates to a value. Sets the cell of the
     * default grid (layer 0). This is equivalent to calling set(0, Point,
     * value).
     *
     * @param x     The x-coordinate of the cell to set.
     * @param y     The y-coordinate of the cell to set.
     * @param value The value to set the cell to.
     * @throws GridIndexOutOfBoundsException If the given coordinates are out of
     *                                       bounds.
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
     * @param pos   The coordinates of the cell to set. If null, the grid will
     *              not be changed.
     * @param value The value to set the cell to.
     * @throws GridIndexOutOfBoundsException If the given coordinates are out of
     *                                       bounds.
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
     * Set the cell at the given coordinates to a value. Repaints grid if auto
     * repainting is enabled.
     *
     * @param layer The layer of the cell to set. If not a valid layer, the grid
     *              will not be changed.
     * @param x     The x-coordinate of the cell to set.
     * @param y     The y-coordinate of the cell to set.
     * @param value The value to set the cell to.
     * @throws GridIndexOutOfBoundsException If the given coordinates are out of
     *                                       bounds.
     * @see SimpleGrid#set(int, Point, int)
     * @since v1.1
     */
    public void set(int layer, int x, int y, int value) {
        if (layer < 0 || layer > this.grids.size() - 1) {
            return;
        }
        if (isOOB(x, y)) {
            throw new GridIndexOutOfBoundsException("Grid coordinates must be in bounds.");
        }
        this.grids.get(layer)[y][x] = value;
        ensureValueData(value);

        // Only repaint this cell. Slightly faster than repainting everything with repaint()
        int cellX = x * (this.panel.cellSize + this.panel.gridlineWeight) + this.panel.gridlineWeight;
        int cellY = y * (this.panel.cellSize + this.panel.gridlineWeight) + this.panel.gridlineWeight;
        tryRepaint(cellX, cellY, this.panel.cellSize, this.panel.cellSize);
    }

    /**
     * Fills the grid with a value, setting all cells in the grid to the value.
     * Fills the default grid (layer 0). Repaints grid if auto repainting is
     * enabled. This is equivalent to calling fill(0, value).
     *
     * @param value The value to set all the cells in the grid to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since 1.2.3
     */
    public void fill(int value) {
        fill(0, value);
    }

    /**
     * Fills the grid with a value, setting all cells in the grid to the value.
     * Repaints grid if auto repainting is enabled.
     *
     * @param layer The grid layer to fill. If not a valid layer, the grid will
     *              not be changed.
     * @param value The value to set all the cells in the grid layer to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since 1.2.3
     */
    public void fill(int layer, int value) {
        if (layer < 0 || layer > this.grids.size() - 1) {
            return;
        }
        for (int x = 0; x < this.panel.width; x++) {
            for (int y = 0; y < this.panel.height; y++) {
                this.grids.get(layer)[y][x] = value;
            }
        }
        tryRepaint();
    }

    /**
     * Replaces all cells of a given value with cells of a new value. Repaints
     * the grid if auto repainting is enabled.
     *
     * @param currentValue The value to be replaced.
     * @param newValue     The value to set all cells of the current value to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since v1.2.5
     */
    public void replace(int currentValue, int newValue) {
        replace(0, currentValue, newValue);
    }

    /**
     * Replaces all cells of a given value with cells of a new value. Repaints
     * the grid if auto repainting is enabled.
     *
     * @param layer        The grid layer to replace on. If not a valid layer,
     *                     the grid will not be changed.
     * @param currentValue The value to be replaced.
     * @param newValue     The value to set all cells of the current value to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since v1.2.5
     */
    public void replace(int layer, int currentValue, int newValue) {
        if (layer < 0 || layer > this.grids.size() - 1) {
            return;
        }
        for (int x = 0; x < this.panel.width; x++) {
            for (int y = 0; y < this.panel.height; y++) {
                if (this.grids.get(layer)[y][x] == currentValue) {
                    this.grids.get(layer)[y][x] = newValue;
                }
            }
        }
        tryRepaint();
    }

    /**
     * Fills the given row with a value, setting all cells in the row to the
     * value. Repaints grid if auto repainting is enabled. This is equivalent to
     * calling fillRow(0, row, value).
     *
     * @param row   The y-coordinate of the row to fill. If not a valid row, the
     *              grid will not be changed.
     * @param value The value to set all the cells in the row to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since 1.2.4
     */
    public void fillRow(int row, int value) {
        fillRow(0, row, value);
    }

    /**
     * Fills the given row with a value, setting all cells in the row to the
     * value. Repaints grid if auto repainting is enabled.
     *
     * @param layer The grid layer to fill the row of. If not a valid layer, the
     *              grid will not be changed.
     * @param row   The y-coordinate of the row to fill. If not a valid row, the
     *              grid will not be changed.
     * @param value The value to set all the cells in the row to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since 1.2.4
     */
    public void fillRow(int layer, int row, int value) {
        if (row < 0 || row >= this.panel.height || layer < 0 || layer > this.grids.size() - 1) {
            return;
        }
        for (int x = 0; x < this.panel.width; x++) {
            this.grids.get(layer)[row][x] = value;
        }
    }

    /**
     * Fills the given column with a value, setting all cells in the column to
     * the value. Repaints grid if auto repainting is enabled. This is
     * equivalent to calling fillColumn(0, column, value).
     *
     * @param column The x-coordinate of the column to fill. If not a valid row,
     *               the grid will not be changed.
     * @param value  The value to set all the cells in the column to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since 1.2.4
     */
    public void fillColumn(int column, int value) {
        fillColumn(0, column, value);
    }

    /**
     * Fills the given column with a value, setting all cells in the column to
     * the value. Repaints grid if auto repainting is enabled.
     *
     * @param layer  The grid layer to fill the row of. If not a valid layer,
     *               the grid will not be changed.
     * @param column The x-coordinate of the column to fill. If not a valid row,
     *               the grid will not be changed.
     * @param value  The value to set all the cells in the column to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since 1.2.4
     */
    public void fillColumn(int layer, int column, int value) {
        if (column < 0 || column >= this.panel.width || layer < 0 || layer > this.grids.size() - 1) {
            return;
        }
        for (int y = 0; y < this.panel.height; y++) {
            this.grids.get(layer)[y][column] = value;
        }
    }

    /**
     * Returns the value of the cell at the given coordinates. Gets the cell of
     * the default grid (layer 0). This is equivalent to calling get(0, pos).
     *
     * @param pos The coordinates of the cell to get.
     * @return The value of the cell.
     * @throws NullPointerException          If the given position is null.
     * @throws GridIndexOutOfBoundsException If the given coordinates are out of
     *                                       bounds.
     * @see SimpleGrid#get(int, int)
     * @see SimpleGrid#get(int, Point)
     */
    public int get(Point pos) {
        return get(0, pos);
    }

    /**
     * Returns the value of the cell at the given coordinates. Gets the cell of
     * the default grid (layer 0). This is equivalent to calling get(0, x, y).
     *
     * @param x The x-coordinate of the cell to get.
     * @param y The y-coordinate of the cell to get.
     * @return The value of the cell.
     * @throws GridIndexOutOfBoundsException If the given coordinates are out of
     *                                       bounds.
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
     * @return The value of the cell.
     * @throws NullPointerException          If the given position is null.
     * @throws IllegalArgumentException      If the given layer does not exist.
     * @throws GridIndexOutOfBoundsException If the given coordinates are out of
     *                                       bounds.
     * @see SimpleGrid#get(int, int, int)
     * @since v1.1
     */
    public int get(int layer, Point pos) {
        if (pos == null) {
            throw new NullPointerException("Position must not be null.");
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
     * @throws IllegalArgumentException      If the given layer does not exist.
     * @throws GridIndexOutOfBoundsException If the given coordinates are out of
     *                                       bounds.
     * @see SimpleGrid#get(int, Point)
     * @since v1.1
     */
    public int get(int layer, int x, int y) {
        if (layer < 0 || layer > this.grids.size() - 1) {
            throw new IllegalArgumentException("Must specify a valid layer.");
        }
        if (isOOB(x, y)) {
            throw new GridIndexOutOfBoundsException("Grid coordinates must be in bounds.");
        }
        return this.grids.get(layer)[y][x];
    }

    /**
     * Set the color of the gridlines. Repaints grid if auto repainting is
     * enabled.
     *
     * @param color The color to set the gridlines to.
     * @see SimpleGrid#setAutoRepaint(boolean)
     */
    public void setGridlineColor(Color color) {
        this.panel.setBackground(color);
        tryRepaint();
    }

    /**
     * Maps default value data to a value if the value doesn't have any data
     * already mapped.
     *
     * @param value The value to check for existing mapped data and to map
     *              default data to if it has no mappings.
     * @since v1.2
     */
    private void ensureValueData(int value) {
        if (!this.valueData.containsKey(value)) {
            this.valueData.put(value, new ValueData());
        }
    }

    /**
     * Assigns a color to a given value. All cells with this value will be
     * colored this color. Repaints grid if auto repainting is enabled.
     *
     * @param value The value to set the color of.
     * @param color The color to be assigned to the value. If null, the cell
     *              color will white if the cell is on layer 0, otherwise the
     *              cell color will be transparent.
     * @see SimpleGrid#setAutoRepaint(boolean)
     */
    public void setColor(int value, Color color) {
        ensureValueData(value);
        this.valueData.get(value).color = color;
        tryRepaint();
    }

    /**
     * Assigns a text color to a given value. All cells with this value will be
     * colored this color. Repaints grid if auto repainting is enabled.
     *
     * @param value     The value to set the text color of.
     * @param textColor The text color to be assigned to the value.
     * @throws NullPointerException If the given Color is null.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since v1.2
     */
    public void setTextColor(int value, Color textColor) {
        if (textColor == null) {
            throw new NullPointerException("Text color cannot be null.");
        }
        ensureValueData(value);
        this.valueData.get(value).textColor = textColor;
        tryRepaint();
    }

    /**
     * Assigns a text character to a given value. All cells with this value will
     * display this character. Repaints grid if auto repainting is enabled.
     *
     * @param value The value to set the text of.
     * @param text  The text character to be assigned to the value. If '\0',
     *              cells with this value will display no text.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since v1.2
     */
    public void setText(int value, char text) {
        ensureValueData(value);
        this.valueData.get(value).text = text;
        tryRepaint();
    }

    /**
     * Assigns an image to a given value. All cells with this image will display
     * this image (stretched to fit the cell bounds). Repaints grid if auto
     * repainting is enabled.
     *
     * @param value The value to set the image of.
     * @param image The image to be assigned to the value. If null, cells with
     *              this text will display no image.
     * @see SimpleGrid#setAutoRepaint(boolean)
     * @since v1.3
     */
    public void setImage(int value, BufferedImage image) {
        ensureValueData(value);
        this.valueData.get(value).image = image;
        tryRepaint();
    }

    /**
     * Saves a PNG image of the grid.
     *
     * @param file The file to write the image to.
     * @throws IOException If an error occurs while writing the image to the
     *                     file.
     * @see #getGridImage()
     * @since v1.3.1
     */
    public void saveGridImage(File file) throws IOException {
        ImageIO.write(getGridImage(), "png", file);
    }

    /**
     * Creates an image of the grid.
     *
     * @return An image of the grid.
     * @see #saveGridImage(File)
     * @since v1.3.1
     */
    public RenderedImage getGridImage() {
        // @formatter:off
        BufferedImage img = new BufferedImage(
                this.panel.getWidth(),
                this.panel.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        // @formatter:on
        Graphics2D g2d = img.createGraphics();

        this.panel.printAll(g2d);
        return img;
    }

    /**
     * Returns an ASCII string representation of the grid's contents that can
     * be loaded using {@link #loadGrid(String)}. Only cell values will be
     * saved. Grid settings such as dimensions, gridline configuration, value
     * color/text/text color assignment, etc. will not be saved.
     *
     * @return A string representation of this grid's contents.
     * @see #loadGrid(String)
     * @since v1.3.1
     */
    public String saveGrid() {
        StringBuilder sb = new StringBuilder();

        // Process each layer
        for (int i = 0; i < this.grids.size(); i++) {
            int[][] layer = this.grids.get(i);

            // Process each cell in this layer
            sb.append(layer[0][0]);
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    if (y == 0 && x == 0) {
                        continue;
                    }
                    sb.append(' ').append(layer[y][x]);
                }
            }

            // Separate layer data with ":"
            if (i < this.grids.size() - 1) {
                sb.append(':');
            }
        }

        return sb.toString();
    }

    /**
     * Load an ASCII string representation of grid contents saved by
     * {@link #saveGridImage(File)}. Additional layers will be added if needed
     * to load the grid. If this grid has more layers than are used in the grid
     * being loaded, any extra layers will be unchanged. All existing values in
     * grid layers used by grid being loaded will be overwritten to match the
     * new grid data.
     * <p>
     * Grid settings such as dimensions, gridline configuration, value
     * color/text/text color assignment, etc. will not be changed.
     *
     * @param gridData The saved grid data to load. Must have been saved from a
     *                 grid with equal dimensions as this one.
     * @see #saveGridImage(File)
     * @since v1.3.1
     */
    public void loadGrid(String gridData) {
        String[] layers = gridData.split(":");

        // Add any needed layers
        while (layers.length > this.grids.size()) {
            addLayer();
        }

        // Load layer data
        for (int i = 0; i < layers.length; i++) {
            String[] cells = layers[i].split(" ");
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    int index = y * getWidth() + x;
                    this.grids.get(i)[y][x] = Integer.parseInt(cells[index]);
                }
            }
        }
    }

    /**
     * This class holds the data mapped to grid values. Consists of:
     * <ul>
     * <li>Cell color</li>
     * <li>Text color</li>
     * <li>Text</li>
     * </ul>
     *
     * @since v1.2
     */
    private static class ValueData {
        public static final Color DEFAULT_COLOR = Color.WHITE;
        public static final Color DEFAULT_TEXT_COLOR = Color.BLACK;

        public Color color;
        public Color textColor;
        public char text;
        public BufferedImage image;

        /**
         * Creates data with default color values and text of '\0'.
         */
        public ValueData() {
            this(DEFAULT_COLOR, DEFAULT_TEXT_COLOR, '\0', null);
        }

        /**
         * Creates new data with the given values.
         *
         * @param color     The color of the cell.
         * @param textColor The color of the text character.
         * @param text      The cell text character.
         * @param image     The image of the cell.
         */
        public ValueData(Color color, Color textColor, char text, BufferedImage image) {
            this.color = color;
            this.textColor = textColor;
            this.text = text;
            this.image = image;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int hash = 1;
            hash = hash * prime + this.color.hashCode();
            hash = hash * prime + this.textColor.hashCode();
            hash = hash * prime + Character.hashCode(this.text);
            hash = hash * prime + this.image.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final ValueData other = (ValueData) o;
            return (this.text == other.text && this.color.equals(other.color) && this.textColor.equals(other.textColor) && this.image.equals(other.image));
        }
    }

    /**
     * This class paints the grid and can give information on the grid settings
     * and mouse cursor.
     */
    private class GridPanel extends JPanel {
        private static final long serialVersionUID = 4114771226550991401L;

        private int width;
        private int height;
        private int cellSize;
        private int gridlineWeight;
        private Font font;

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
            this.font = null;
            setBackground(Color.BLACK);
        }

        /**
         * Paints the entire grid. The grid cells are colored and filled
         * according to the colors, text, and images assigned to their current
         * values.
         * <p>
         * If a cell color is null then the cell will painted with no color,
         * unless the cell is on layer 0, in which case the cell will be painted
         * white. If a cell's text is '\0' it will not be drawn. If a cell's
         * image is null is will not be drawn.
         * <p>
         * The final appearance of each cell will be as follows: <ul> <li>The
         * cell will be painted with the topmost non-null color</li> <li>The
         * cell will contain the text of the topmost non '\0' character</li>
         * <li>If a non-null color is above this character, no text will be
         * drawn</li> <li>The cell will be drawn with all images on layers above
         * the topmost non-null color, from the bottom to top. </li> <li>Images
         * on layers below the layer with the drawn text will be drawn below the
         * text</li><li>For text and image on the same layer, the text will be
         * drawn over the image</li></ul>
         * <p>
         * The following diagram demonstrates an example of this drawing process
         * for a single cell. The left shows the ValueData for all layers and
         * the right shows what will be drawn (from bottom to top).
         * <pre>
         * This box represents the ValueData for a cell on a particular layer:
         * +---------+
         * | Image   |
         * | Text    |
         * | Color   |
         * +---------+
         *
         *         +---------+      +---------+
         * Layer 2 | IMG_2   |      | IMG_2   |
         *         | TEXT_2  | ===> | TEXT_2  |
         *         | null    |      | IMG_1   |
         *         +---------+      | COLOR_1 |
         * Layer 1 | IMG_1   |      +---------+
         *         | TEXT_1  |
         *         | COLOR_1 |
         *         +---------+
         * Layer 0 | IMG_0   |
         *         | TEXT_0  |
         *         | COLOR_0 |
         *         +---------+
         * </pre>
         * In particular note that for the most part everything is drawn layer
         * by layer, but things below an opaque color won't be drawn.
         *
         * @param g The graphics object that the grid will be painted with.
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Setup the font if it has not been done
            if (this.font == null) {
                setupFont(g);
            }

            // Paint all cells
            for (int x = 0; x < this.width; x++) {
                for (int y = 0; y < this.height; y++) {
                    // Begin with colors/text/image being those of the layer 0 cells
                    ValueData defaultLayer = SimpleGrid.this.valueData.get(SimpleGrid.this.grids.get(0)[y][x]);
                    Color topColor = defaultLayer.color == null ? Color.WHITE : defaultLayer.color;
                    char topText = defaultLayer.text;
                    Color topTextColor = defaultLayer.textColor;

                    // Highest layer with an opaque color
                    int highestOpaqueColor = 0;
                    // Highest layer with non-empty text
                    int highestNonEmptyText = 0;

                    // Find the topmost colors/text that should be drawn
                    for (int i = 1; i < SimpleGrid.this.grids.size(); i++) {
                        int value = SimpleGrid.this.grids.get(i)[y][x];
                        ValueData data = SimpleGrid.this.valueData.get(value);

                        if (data.color != null) {
                            topColor = data.color;
                            topText = '\0'; // Clear text if a non-null (opaque) color is above it
                            highestOpaqueColor = i;
                        }
                        if (data.text != '\0') {
                            topText = data.text;
                            topTextColor = data.textColor;
                            highestNonEmptyText = i;
                        }
                    }
                    g.setColor(topColor);

                    // Paint cell
                    int cellX = x * (this.cellSize + this.gridlineWeight) + this.gridlineWeight;
                    int cellY = y * (this.cellSize + this.gridlineWeight) + this.gridlineWeight;
                    g.fillRect(cellX, cellY, this.cellSize, this.cellSize);

                    for (int i = highestOpaqueColor; i < SimpleGrid.this.grids.size(); i++) {
                        int value = SimpleGrid.this.grids.get(i)[y][x];
                        ValueData data = SimpleGrid.this.valueData.get(value);

                        // Draw cell image
                        g.drawImage(data.image, cellX, cellY, this.cellSize, this.cellSize, this);

                        if (i == highestNonEmptyText) {
                            // Draw text
                            if (topText != '\0') {
                                drawCenteredChar(g, x, y, topText, topTextColor);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Creates a Monospaced, bold font with a size such that one character
         * will fit inside of one cell.
         *
         * @param g A graphics object that will be used to paint the grid.
         * @since v1.2
         */
        private void setupFont(Graphics g) {
            int fontSize = 0;
            int textHeight = 0;
            while (textHeight < this.cellSize) {
                fontSize++;
                this.font = new Font("Monospaced", Font.BOLD, fontSize);
                FontMetrics metrics = g.getFontMetrics(this.font);
                textHeight = metrics.getHeight();
            }
        }

        /**
         * Draws a character in the center of a cell.
         *
         * @param g         The graphics object that the character will be drawn
         *                  with.
         * @param x         The x-coordinate of the cell to draw in.
         * @param y         The y-coordinate of the cell to drawn in.
         * @param c         The character to draw in the cell.
         * @param textColor The color of the character to draw.
         * @since v1.2
         */
        public void drawCenteredChar(Graphics g, int x, int y, char c, Color textColor) {
            g.setColor(textColor);
            g.setFont(this.font);

            int xCoord = x * (this.cellSize + this.gridlineWeight) + this.gridlineWeight;
            int yCoord = y * (this.cellSize + this.gridlineWeight) + this.gridlineWeight;
            FontMetrics metrics = g.getFontMetrics(this.font);
            int xPos = xCoord + (this.cellSize - metrics.stringWidth("" + c)) / 2;
            int yPos = yCoord + ((this.cellSize - metrics.getHeight()) / 2) + metrics.getAscent();

            g.drawString("" + c, xPos, yPos);
        }

        /**
         * Returns the grid position of the mouse cursor.
         *
         * @return The xy-coordinates of the cell the mouse cursor is currently
         * over. If the mouse is out-of-bounds or over gridlines, returns null
         * instead.
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
     * This class monitors mouse activity, such as pressing and releasing the
     * mouse button.
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

    /**
     * Thrown to indicate that an index of a grid coordinate is out of range.
     *
     * @since 1.2.9
     */
    public class GridIndexOutOfBoundsException extends IndexOutOfBoundsException {
        /**
         * Constructs a <code>GridIndexOutOfBoundsException</code> with no
         * detail message.
         */
        public GridIndexOutOfBoundsException() {
            super();
        }

        /**
         * Constructs a <code>GridIndexOutOfBoundsException</code> with the
         * specified detail message.
         *
         * @param message the detail message.
         */
        public GridIndexOutOfBoundsException(String message) {
            super(message);
        }
    }
}
