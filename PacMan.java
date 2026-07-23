import javax.swing.*;


public class PacMan extends JPanel implements ActionListener {
    public static void main(String[] args) {
        JFrame frame = new JFrame("پک من - Pac-Man");
        PacMan game = new PacMan();
        frame.add(game);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocusInWindow();
    }
}