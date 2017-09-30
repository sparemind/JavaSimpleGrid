import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SimpleGrid {
    private GridPanel panel;
    private JFrame frame;
    private int[][] grid;
    private Set<Point> updatedCells;

    public SimpleGrid(int width, int height, int cellSize, int gridlineWeight, String name) {
        this.panel = new GridPanel(width, height, cellSize, gridlineWeight);
        this.frame = new JFrame(name);
        this.grid = new int[height][width];
        this.updatedCells = new HashSet<>();

        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.add(this.panel);
        this.frame.setVisible(true);
        this.frame.setResizable(false);
        Dimension size = new Dimension(width * (cellSize + gridlineWeight) + gridlineWeight, height * (cellSize + gridlineWeight) + gridlineWeight);
        this.panel.setPreferredSize(size);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null); // Center the frame
    }

    private boolean isOOB(int x, int y) {
        return x < 0 || y < 0 || x >= this.grid[0].length || y > this.grid.length;
    }

    private void checkOOB(int x, int y) {
        if (isOOB(x, y)) {
            throw new IllegalArgumentException("Coordinates must specify a cell inside the grid boundaries.");
        }
    }

    public void set(int x, int y, int value) {
        checkOOB(x, y);

        this.grid[y][x] = value;
        // this.panel.repaint();
    }

    private class GridPanel extends JPanel {
        private static final long serialVersionUID = 4114771226550991401L;

        private int width;
        private int height;
        private int cellSize;
        private int gridlineWeight;

        public GridPanel(int width, int height, int cellSize, int gridlineWeight) {
            super();

            this.width = width;
            this.height = height;
            this.cellSize = cellSize;
            this.gridlineWeight = gridlineWeight;
            setBackground(Color.BLACK);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int x = 0; x < this.width; x++) {
                for (int y = 0; y < this.height; y++) {
                    g.setColor(Color.WHITE);
                    int cellX = x * (this.cellSize + this.gridlineWeight) + this.gridlineWeight;
                    int cellY = y * (this.cellSize + this.gridlineWeight) + this.gridlineWeight;
                    g.fillRect(cellX, cellY, this.cellSize, this.cellSize);
                }
            }
        }
    }
}
