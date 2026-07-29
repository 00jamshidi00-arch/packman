package pacman.score;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ScoreManager {

    private static final String SCORE_FILE = "pacman_highscores.txt";
    private static final int MAX_HIGH_SCORES = 10;

    private ScoreManager() {
    }

    public static List<Integer> loadHighScores() {
        List<Integer> list = new ArrayList<>();
        File f = new File(SCORE_FILE);
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        try {
                            list.add(Integer.parseInt(line));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
        Collections.sort(list, Collections.reverseOrder());
        return list;
    }

    public static void saveHighScores(List<Integer> scores) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SCORE_FILE))) {
            for (int s : scores) pw.println(s);
        } catch (IOException ignored) {
        }
    }

    public static List<Integer> addScore(List<Integer> highScores, int newScore) {
        highScores.add(newScore);
        Collections.sort(highScores, Collections.reverseOrder());
        while (highScores.size() > MAX_HIGH_SCORES) {
            highScores.remove(highScores.size() - 1);
        }
        saveHighScores(highScores);
        return highScores;
    }

    public static boolean isNewRecord(List<Integer> highScores, int score) {
        return !highScores.isEmpty() && highScores.get(0) == score;
    }
}
