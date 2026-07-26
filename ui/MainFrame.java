package pacman.ui;

import pacman.score.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private List<Integer> highScores;
    private final ScorePanel scorePanel;
    private GamePanel gamePanel;

    public MainFrame() {
        super("پک من - Pac-Man");
        highScores = ScoreManager.loadHighScores();

        scorePanel = new ScorePanel(this::startNewGame);
        container.add(scorePanel, "scores");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(container);

        startNewGame();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startNewGame() {
        if (gamePanel != null) {
            container.remove(gamePanel);
        }
        gamePanel = new GamePanel();
        gamePanel.setOnGameEnd(this::handleGameEnd);
        container.add(gamePanel, "game");
        pack();
        cardLayout.show(container, "game");
        gamePanel.requestFocusInWindow();
    }

    private void handleGameEnd(int finalScore) {
        highScores = ScoreManager.addScore(highScores, finalScore);
        boolean isNewRecord = ScoreManager.isNewRecord(highScores, finalScore);
        scorePanel.refresh(finalScore, highScores, isNewRecord);
        cardLayout.show(container, "scores");
        scorePanel.requestFocusInWindow();
    }
}
