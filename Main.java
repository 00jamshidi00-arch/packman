package pacman;

import pacman.ui.MainFrame;

import javax.swing.SwingUtilities;

/**
 * start point
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
