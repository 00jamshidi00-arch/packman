package pacman.model;

import java.util.ArrayList;
import java.util.List;


public class GameMap {

    private static final String[] RAW_MAP = {
        "###################",
        "#........#........#",
        "#.##.###.#.###.##.#",
        "#O##.###.#.###.##O#",
        "#.................#",
        "#.##.#.#####.#.##.#",
        "#....#...#...#....#",
        "####.### # ###.####",
        "   #.#       #.#   ",
        "####.# ##### #.####",
        "#........#........#",
        "#.##.###.#.###.##.#",
        "#O.#.....P.....#.O#",
        "##.#.#.#####.#.#.##",
        "#....#...#...#....#",
        "#.######.#.######.#",
        "#.................#",
        "###################"
    };

    private int rows;
    private int cols;
    private char[][] grid;
    private int totalDots;
    private double pacStartRow;
    private double pacStartCol;
    private final List<int[]> ghostStartPositions = new ArrayList<>();

    public GameMap() {
        build();
    }

    private void build() {
        rows = RAW_MAP.length;
        cols = RAW_MAP[0].length();
        grid = new char[rows][cols];
        ghostStartPositions.clear();
        totalDots = 0;

        for (int r = 0; r < rows; r++) {
            String line = RAW_MAP[r];
            for (int c = 0; c < cols; c++) {
                char ch = c < line.length() ? line.charAt(c) : ' ';
                if (ch == 'P') {
                    pacStartRow = r;
                    pacStartCol = c;
                    grid[r][c] = '.';
                } else if (ch == 'G') {
                    ghostStartPositions.add(new int[]{r, c});
                    grid[r][c] = ' ';
                } else {
                    grid[r][c] = ch;
                }
                if (grid[r][c] == '.' || grid[r][c] == 'O') totalDots++;
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public double getPacStartRow() {
        return pacStartRow;
    }

    public double getPacStartCol() {
        return pacStartCol;
    }

    public List<int[]> getGhostStartPositions() {
        return ghostStartPositions;
    }

    public int getTotalDots() {
        return totalDots;
    }

    public boolean isWall(int r, int c) {
        if (r < 0 || r >= rows) return true;
        int cc = ((c % cols) + cols) % cols;
        return grid[r][cc] == '#';
    }

    public char getCell(int r, int c) {
        return grid[r][c];
    }


    public int eatAt(int r, int c) {
        char here = grid[r][c];
        int points = 0;
        if (here == '.') {
            grid[r][c] = ' ';
            points = 10;
            totalDots--;
        } else if (here == 'O') {
            grid[r][c] = ' ';
            points = 50;
            totalDots--;
        }
        return points;
    }
}
