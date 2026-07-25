package pacman.ui;

import pacman.model.GameMap;
import pacman.model.Ghost;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;


public class GamePanel extends JPanel implements ActionListener {

    private final GameMap map = new GameMap();

    private final int TILE = 24;

    private double pacRow, pacCol;
    private int pacDir = 0;
    private int wantDir = 0;

    private int mouthPhase = 0;
    private int frameCount = 0;

    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean win = false;
    private boolean endReported = false;

    private final List<Ghost> ghosts = new ArrayList<>();
    private Timer timer;

    private static final double EPS = 1e-6;
    private static final int FRAME_MS = 16;
    private static final double DT = FRAME_MS / 1000.0;
    private static final double PAC_SPEED = 6.5;
    private static final double GHOST_SPEED = 5.2;
    private static final double COLLISION_DIST = 0.55;

    private final Random rnd = new Random();
    private Consumer<Integer> onGameEnd;

    public GamePanel() {
        pacRow = map.getPacStartRow();
        pacCol = map.getPacStartCol();
        createGhosts();

        setPreferredSize(new Dimension(map.getCols() * TILE, map.getRows() * TILE + 40));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_UP) wantDir = 1;
                else if (code == KeyEvent.VK_DOWN) wantDir = 2;
                else if (code == KeyEvent.VK_LEFT) wantDir = 3;
                else if (code == KeyEvent.VK_RIGHT) wantDir = 4;
            }
        });

        timer = new Timer(FRAME_MS, this);
        timer.start();
    }

    public void setOnGameEnd(Consumer<Integer> callback) {
        this.onGameEnd = callback;
    }

    private void createGhosts() {
        Color[] ghostColors = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };
        List<int[]> starts = map.getGhostStartPositions();

        if (starts.isEmpty()) {
            ghosts.add(new Ghost(8, 8, Color.RED));
            ghosts.add(new Ghost(8, 10, Color.PINK));
            ghosts.add(new Ghost(9, 9, Color.CYAN));
        } else {
            for (int i = 0; i < starts.size(); i++) {
                int[] pos = starts.get(i);
                ghosts.add(new Ghost(pos[0], pos[1], ghostColors[i % ghostColors.length]));
            }
        }
    }

    private int[] delta(int dir) {
        switch (dir) {
            case 1: return new int[]{-1, 0};
            case 2: return new int[]{1, 0};
            case 3: return new int[]{0, -1};
            case 4: return new int[]{0, 1};
            default: return new int[]{0, 0};
        }
    }

    private boolean isAligned(double pos) {
        return Math.abs(pos - Math.round(pos)) < EPS;
    }

    private double distanceToNextGridLine(double pos, int stepDir) {
        double frac = pos - Math.floor(pos);
        if (stepDir > 0) {
            return (frac < EPS) ? 1.0 : (1.0 - frac);
        } else {
            return (frac < EPS) ? 1.0 : frac;
        }
    }

    private void movePacman(double dt) {
        double moveAmt = PAC_SPEED * dt;
        int guard = 0;
        while (moveAmt > EPS && guard++ < 20) {
            boolean aligned = isAligned(pacRow) && isAligned(pacCol);
            if (aligned) {
                pacRow = Math.round(pacRow);
                pacCol = Math.round(pacCol);
                int cr = (int) pacRow;
                int cc = (int) pacCol;

                score += map.eatAt(cr, cc);
                if (map.getTotalDots() <= 0) {
                    win = true;
                    timer.stop();
                    return;
                }

                if (wantDir != 0) {
                    int[] wd = delta(wantDir);
                    if (!map.isWall(cr + wd[0], cc + wd[1])) {
                        pacDir = wantDir;
                    }
                }

                if (pacDir == 0) break;
                int[] fd = delta(pacDir);
                if (map.isWall(cr + fd[0], cc + fd[1])) {
                    break;
                }
            }

            int[] d = delta(pacDir);
            double distToNext = (d[0] != 0)
                    ? distanceToNextGridLine(pacRow, d[0])
                    : distanceToNextGridLine(pacCol, d[1]);
            double step = Math.min(moveAmt, distToNext);

            pacRow += d[0] * step;
            pacCol += d[1] * step;
            if (pacCol < 0) pacCol += map.getCols();
            if (pacCol >= map.getCols()) pacCol -= map.getCols();

            moveAmt -= step;
        }
    }

    private int chooseGhostDirection(Ghost g, int gr, int gc) {
        List<Integer> options = new ArrayList<>();
        int[] dirs = {1, 2, 3, 4};
        for (int dir : dirs) {
            int[] d = delta(dir);
            if (!map.isWall(gr + d[0], gc + d[1])) options.add(dir);
        }
        int opposite = g.dir == 1 ? 2 : g.dir == 2 ? 1 : g.dir == 3 ? 4 : g.dir == 4 ? 3 : 0;
        if (options.size() > 1) options.remove((Integer) opposite);

        int chosen;
        if (options.isEmpty()) {
            chosen = opposite != 0 ? opposite : g.dir;
        } else if (rnd.nextInt(3) == 0) {
            int pr = (int) Math.round(pacRow);
            int pc = (int) Math.round(pacCol);
            chosen = options.get(0);
            int bestDist = Integer.MAX_VALUE;
            for (int dir : options) {
                int[] d = delta(dir);
                int nr = gr + d[0];
                int nc = gc + d[1];
                int dist = Math.abs(nr - pr) + Math.abs(nc - pc);
                if (dist < bestDist) {
                    bestDist = dist;
                    chosen = dir;
                }
            }
        } else {
            chosen = options.get(rnd.nextInt(options.size()));
        }
        return chosen;
    }

    private void moveGhostSmooth(Ghost g, double dt) {
        double moveAmt = GHOST_SPEED * dt;
        int guard = 0;
        while (moveAmt > EPS && guard++ < 20) {
            boolean aligned = isAligned(g.row) && isAligned(g.col);
            if (aligned) {
                g.row = Math.round(g.row);
                g.col = Math.round(g.col);
                int gr = (int) g.row;
                int gc = (int) g.col;

                g.dir = chooseGhostDirection(g, gr, gc);
                if (g.dir == 0) break;

                int[] fd = delta(g.dir);
                if (map.isWall(gr + fd[0], gc + fd[1])) break;
            }

            int[] d = delta(g.dir);
            double distToNext = (d[0] != 0)
                    ? distanceToNextGridLine(g.row, d[0])
                    : distanceToNextGridLine(g.col, d[1]);
            double step = Math.min(moveAmt, distToNext);

            g.row += d[0] * step;
            g.col += d[1] * step;
            if (g.col < 0) g.col += map.getCols();
            if (g.col >= map.getCols()) g.col -= map.getCols();

            moveAmt -= step;
        }
    }

    private void checkCollisions() {
        for (Ghost g : ghosts) {
            double dr = pacRow - g.row;
            double dc = pacCol - g.col;
            double dist = Math.sqrt(dr * dr + dc * dc);
            if (dist < COLLISION_DIST) {
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    timer.stop();
                } else {
                    pacDir = 0;
                    wantDir = 0;
                }
                break;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !win) {
            movePacman(DT);
            if (!win) {
                for (Ghost g : ghosts) moveGhostSmooth(g, DT);
                checkCollisions();
            }
            frameCount++;
            if (frameCount % 8 == 0) mouthPhase = (mouthPhase + 1) % 2;
            repaint();
        }

        if ((gameOver || win) && !endReported) {
            endReported = true;
            repaint();
            Timer delay = new Timer(1200, ev -> {
                if (onGameEnd != null) onGameEnd.accept(score);
            });
            delay.setRepeats(false);
            delay.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int rows = map.getRows();
        int cols = map.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * TILE;
                int y = r * TILE;
                char ch = map.getCell(r, c);
                if (ch == '#') {
                    g.setColor(new Color(33, 33, 222));
                    g.fillRoundRect(x + 2, y + 2, TILE - 4, TILE - 4, 6, 6);
                } else if (ch == '.') {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + TILE / 2 - 3, y + TILE / 2 - 3, 6, 6);
                } else if (ch == 'O') {
                    g.setColor(Color.WHITE);
                    g.fillOval(x + TILE / 2 - 7, y + TILE / 2 - 7, 14, 14);
                }
            }
        }

        int px = (int) Math.round(pacCol * TILE);
        int py = (int) Math.round(pacRow * TILE);
        g.setColor(Color.YELLOW);
        int startAngle;
        int arcAngle = (mouthPhase == 1) ? 360 : 300;
        switch (pacDir) {
            case 1: startAngle = 120; break;
            case 2: startAngle = 300; break;
            case 3: startAngle = 210; break;
            case 4: startAngle = 30; break;
            default: startAngle = 30;
        }
        g.fillArc(px + 2, py + 2, TILE - 4, TILE - 4, startAngle, arcAngle);

        for (Ghost gh : ghosts) {
            int gx = (int) Math.round(gh.col * TILE);
            int gy = (int) Math.round(gh.row * TILE);
            g.setColor(gh.color);
            g.fillArc(gx + 2, gy + 2, TILE - 4, TILE - 4, 0, 180);
            g.fillRect(gx + 2, gy + TILE / 2, TILE - 4, TILE / 2 - 4);
            g.setColor(Color.WHITE);
            g.fillOval(gx + 5, gy + 6, 5, 5);
            g.fillOval(gx + TILE - 11, gy + 6, 5, 5);
            g.setColor(Color.BLACK);
            g.fillOval(gx + 6, gy + 7, 3, 3);
            g.fillOval(gx + TILE - 10, gy + 7, 3, 3);
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, rows * TILE, cols * TILE, 40);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("امتیاز: " + score, 10, rows * TILE + 26);
        g.drawString("جان: " + lives, cols * TILE - 100, rows * TILE + 26);

        if (gameOver) {
            drawCenteredMessage(g, "باختی!");
        } else if (win) {
            drawCenteredMessage(g, "بردی!");
        }
    }

    private void drawCenteredMessage(Graphics2D g, String msg) {
        int rows = map.getRows();
        int cols = map.getCols();
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, cols * TILE, rows * TILE);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(msg);
        g.drawString(msg, (cols * TILE - w) / 2, rows * TILE / 2);
    }
}
