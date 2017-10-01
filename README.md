# Java Simple Grid #

This single-file project provides the means for the creation, management, and display of a simple 2D graphical grid using Java's built in standard library.

## Overview ##
Creating a SimpleGrid will make a new window containing a grid with the specified settings. Each cell in the grid holds a numerical value, initially 0. The color of each cell is dependent on what the value of the cell is.

Features include:

* Creating grids with different dimensions and cell sizes
* Mapping integer values to colors
* Changing gridline thickness and color
* Getting mouse input in terms of grid coordinates and button presses
* Full JavaDoc documentation

## Examples ##
```java
// Create new 20x20 grid with 30x30px cells and 2px gridline
SimpleGrid grid = new SimpleGrid(20, 20, 30, 2, "SimpleGrid Example");
```
![Creating a blank grid](img/example1.png)

```java
// Map colors and fill some random cells
grid.setColor(1, Color.BLUE);
grid.setColor(2, Color.RED);
for (int i = 0; i < 100; i++) {
    grid.set(random.nextInt(20), random.nextInt(20), 1);
    grid.set(random.nextInt(20), random.nextInt(20), 2);
}
```
![Filling grid](img/example2.png)


```java
// Change color of gridlines
grid.setGridlineColor(Color.WHITE);
```
![Changing gridline color](img/example3.png)


## License ##
This project is licensed under the MIT license. See [LICENSE](LICENSE) for details.
