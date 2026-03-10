package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * SidebarPanel — Premium vertical navigation sidebar.
 */
public class SidebarPanel extends JPanel {
    private static final Color BG = new Color(14, 20, 45);
    private static final Color ACCENT = new Color(99, 179, 237);
    private static final Color HOVER_BG = new Color(30, 42, 75);
    private static final Color TEXT = new Color(220, 230, 245);

    private String activeId = "home";
    private final Consumer<String> onSwitch;

    public SidebarPanel(Consumer<String> onSwitch) {
        this.onSwitch = onSwitch;
        setLayout(new BorderLayout());
        setBackground(BG);
        setPreferredSize(new Dimension(80, 0));

        JPanel top = new JPanel(new GridLayout(0, 1, 0, 10));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(20, 0, 0, 0));

        top.add(createIconButton("home", "\uD83C\uDFE0"));
        top.add(createIconButton("projects", "\u229E"));
        top.add(createIconButton("objects", "\u274F"));
        top.add(createIconButton("materials", "\u25D2"));

        add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 10));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 0, 20, 0));
        bottom.add(createIconButton("settings", "\u2699"));

        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel createIconButton(String id, String iconChar) {
        JPanel p = new JPanel(new BorderLayout()) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        activeId = id;
                        onSwitch.accept(id);
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = activeId.equals(id);
                if (active) {
                    g2.setColor(HOVER_BG);
                    g2.fillRoundRect(8, 0, getWidth() - 16, getHeight(), 12, 12);
                    g2.setColor(ACCENT);
                    g2.fillRect(0, 10, 3, getHeight() - 20);
                } else if (hover) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fillRoundRect(8, 0, getWidth() - 16, getHeight(), 12, 12);
                }

                g2.setColor(active ? ACCENT : TEXT);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(iconChar, (getWidth() - fm.stringWidth(iconChar)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(80, 60));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return p;
    }
}
