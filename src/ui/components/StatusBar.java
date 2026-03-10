package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * StatusBar — Bottom status bar showing view, cursor, room info, zoom.
 * MEMBER 1 CONTRIBUTION: UI Overhaul & Branding
 * MEMBER 6 CONTRIBUTION: UX Polish — live cursor/zoom feedback
 */
public class StatusBar extends JPanel {

    private static final Color BG = new Color(12, 18, 40);
    private static final Color BORDER = new Color(40, 55, 90);
    private static final Color TEXT = new Color(140, 160, 190);
    private static final Color ACCENT = new Color(99, 179, 237);

    private JLabel viewLbl;
    private JLabel posLbl;
    private JLabel selLbl;
    private JLabel zoomLbl;
    private JLabel roomLbl;

    public StatusBar() {
        setBackground(BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(4, 16, 4, 16)));
        setLayout(new BorderLayout());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        left.setOpaque(false);

        viewLbl = dot("Dashboard", ACCENT);
        posLbl = dot("Cursor: —", TEXT);
        selLbl = dot("Selected: none", TEXT);
        roomLbl = dot("Room: —", TEXT);
        zoomLbl = dot("Zoom: 100%", TEXT);

        left.add(viewLbl);
        left.add(sep());
        left.add(posLbl);
        left.add(sep());
        left.add(selLbl);
        left.add(sep());
        left.add(roomLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        right.setOpaque(false);
        right.add(zoomLbl);
        right.add(sep());
        JLabel version = dot("RoomCraft 2.0  |  PUSL3122", new Color(80, 100, 130));
        right.add(version);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    private JLabel dot(String text, Color c) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(c);
        return lbl;
    }

    private JLabel sep() {
        JLabel s = new JLabel("|");
        s.setForeground(BORDER);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        return s;
    }

    public void setView(String v) {
        viewLbl.setText("\u25CF " + v);
        viewLbl.setForeground(ACCENT);
    }

    public void setCursorPos(double x, double y) {
        posLbl.setText(String.format("Cursor: %.0fcm, %.0fcm", x, y));
    }

    public void setSelectedItem(String name) {
        selLbl.setText("Selected: " + (name == null ? "none" : name));
    }

    public void setRoomInfo(double w, double d) {
        roomLbl.setText(String.format("Room: %.1fm \u00D7 %.1fm = %.1fm\u00B2", w / 100.0, d / 100.0, w * d / 10000.0));
    }

    public void setZoom(int pct) {
        zoomLbl.setText("Zoom: " + pct + "%");
    }
}
