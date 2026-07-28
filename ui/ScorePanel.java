package pacman.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class ScorePanel extends JPanel {

    private final JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel scoreLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel recordLabel = new JLabel(" ", SwingConstants.CENTER);
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> highScoreList = new JList<>(listModel);

    public ScorePanel(Runnable onNewGame) {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));

        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));

        recordLabel.setForeground(Color.GREEN);
        recordLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.setOpaque(false);
        topPanel.add(titleLabel);
        topPanel.add(scoreLabel);
        topPanel.add(recordLabel);

        JLabel listHeader = new JLabel("جدول بهترین امتیازها", SwingConstants.CENTER);
        listHeader.setForeground(Color.CYAN);
        listHeader.setFont(new Font("Arial", Font.BOLD, 16));

        highScoreList.setBackground(new Color(20, 20, 20));
        highScoreList.setForeground(Color.WHITE);
        highScoreList.setFont(new Font("Arial", Font.PLAIN, 16));
        highScoreList.setFocusable(false);
        highScoreList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int i0, int i1) {
            }
        });

        JScrollPane scrollPane = new JScrollPane(highScoreList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(33, 33, 222), 2));
        scrollPane.setPreferredSize(new Dimension(300, 220));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setOpaque(false);
        centerPanel.add(listHeader, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JButton newGameButton = new JButton("بازی جدید");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 16));
        newGameButton.addActionListener(e -> onNewGame.run());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(newGameButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(400, 500));
    }

    public void refresh(int lastScore, List<Integer> highScores, boolean isNewRecord) {
        titleLabel.setText("پایان بازی!");
        scoreLabel.setText("امتیاز شما: " + lastScore);
        recordLabel.setText(isNewRecord ? " رکورد جدید!" : " ");

        listModel.clear();
        int rank = 1;
        for (int s : highScores) {
            listModel.addElement(rank + ".   " + s + " امتیاز");
            rank++;
        }
    }
}
