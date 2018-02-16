import java.awt.Point;

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
 * @version 1.2.9
 */
public class SubGrid {
    private final SimpleGrid grid;
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * Creates a new sub-grid of a SimpleGrid with its own local coordinates.
     *
     * @param grid   The grid this is a sub-grid of.
     * @param x      The x-coordinate of this sub-grid.
     * @param y      The y-coordinate of this sub-grid.
     * @param width  The width of this sub-grid.
     * @param height The height of this sub-grid.
     * @since v1.2.7
     */
    public SubGrid(SimpleGrid grid, int x, int y, int width, int height) {
        this.grid = grid;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width of the sub-grid in number of cells.
     *
     * @return Number of cells wide the sub-grid is.
     * @since v1.2.8
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the height of the sub-grid in number of cells.
     *
     * @return Number of cells high the sub-grid is.
     * @since v1.2.8
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Converts sub-grid xy-coordinate point to the corresponding point in
     * the main grid.
     *
     * @param pos The xy-coordinate position to convert.
     * @return The corresponding point in the given SimpleGrid.
     * @see SubGrid#convert(int, int)
     * @since v1.2.7
     */
    public Point convert(Point pos) {
        return convert(pos.x, pos.y);
    }

    /**
     * Converts sub-grid xy-coordinate point to the corresponding point in the
     * main grid.
     *
     * @param x The sub-grid x-coordinate to convert.
     * @param y The sub-grid y-coordinate to convert.
     * @return The corresponding point in the given SimpleGrid.
     * @see SubGrid#convert(Point)
     * @since v1.2.7
     */
    public Point convert(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    /**
     * Returns whether the given coordinates are out-of-bounds of the sub-grid.
     *
     * @param pos The coordinates to check.
     * @return True if the given coordinates are out-of-bounds of the sub-grid
     * or if the position is null, false otherwise.
     * @see SubGrid#isOOB(int, int)
     * @since v1.2.7
     */
    public boolean isOOB(Point pos) {
        return isOOB(pos.x, pos.y);
    }

    /**
     * Returns whether the given coordinates are out-of-bounds of the sub-grid.
     *
     * @param x The x-coordinate to check.
     * @param y The y-coordinate to check.
     * @return True if the given coordinates are out-of-bounds of the sub-grid,
     * false otherwise.
     * @see SubGrid#isOOB(Point)
     * @since v1.2.7
     */
    public boolean isOOB(int x, int y) {
        return x < this.x || y < this.y || x >= this.x + this.width || y >= this.y + this.height;
    }
}
