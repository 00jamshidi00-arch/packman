import java.awt.*;


private void drawCenteredMessage(Graphics2D g, String msg) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, cols * TILE, rows * TILE);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(msg);
        g.drawString(msg, (cols * TILE - w) / 2, rows * TILE / 2);
    }