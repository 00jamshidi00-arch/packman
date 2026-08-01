package pacman.model;

import java.awt.Color;
import java.util.Random;


public class Ghost {

    public double row;
    public double col;
    public int dir; 
    public final Color color;

    public Ghost(double startRow, double startCol, Color color) {
        this.row = startRow;
        this.col = startCol;
        this.color = color;
        this.dir = new Random().nextInt(4) + 1;
    }
}
