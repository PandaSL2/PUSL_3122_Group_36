package ui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import ui.auth.LoginPanel;
import ui.editor.EditorPanel;
import ui.components.StatusBar;
import controllers.DesignController;

/**
 * MainFrame — Main application window with dark theme, cards layout, and status
 * bar.
 * MEMBER 1 CONTRIBUTION: UI Overhaul & Branding
 */
public class MainFrame extends JFrame {

    private ui.components.SidebarPanel sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private DesignController designController;
    private EditorPanel editorPanel;
    private PortfolioDashboard portfolioDashboard;
    private StatusBar statusBar;

    private static final Color BG_DARK = new Color(10, 15, 35);

    public MainFrame() {
        setTitle("RoomCraft Designer 2.0 — 3D Interior Design Suite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 860);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        // Dark title bar on supported OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        this.designController = new DesignController();
        initializeComponents();
    }

    private void initializeComponents() {
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        // Sidebar
        sidebar = new ui.components.SidebarPanel(id -> {
            if ("home".equals(id))
                showView("DASHBOARD");
            else if ("projects".equals(id))
                showView("DASHBOARD");
            else if ("objects".equals(id))
                showView("EDITOR");
            // etc
        });
        add(sidebar, BorderLayout.WEST);

        // Content Area (CardLayout)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);

        LoginPanel loginPanel = new LoginPanel(this);
        portfolioDashboard = new PortfolioDashboard(this);
        RoomSetupPanel roomSetupPanel = new RoomSetupPanel(this);
        editorPanel = new EditorPanel(this, designController);

        contentPanel.add(loginPanel, "LOGIN");
        contentPanel.add(portfolioDashboard, "DASHBOARD");
        contentPanel.add(roomSetupPanel, "ROOM_SETUP");
        contentPanel.add(editorPanel, "EDITOR");

        add(contentPanel, BorderLayout.CENTER);

        // Status bar
        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);

        sidebar.setVisible(false); // Hide during login

        showView("LOGIN");

        // Global keyboard shortcuts
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");
        getRootPane().getActionMap().put("toggleFullscreen", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleFullscreen();
            }
        });
    }

    private boolean isFullscreen = false;

    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        dispose();
        setUndecorated(isFullscreen);
        if (isFullscreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setExtendedState(JFrame.NORMAL);
            setSize(1400, 860);
            setLocationRelativeTo(null);
        }
        setVisible(true);
    }

    public void openEditor(models.Room room) {
        designController.setCurrentRoom(room);
        editorPanel.setRoom(room);
        showView("EDITOR");
    }

    public void showView(String viewName) {
        if ("DASHBOARD".equals(viewName) && portfolioDashboard != null) {
            portfolioDashboard.refresh();
        }
        cardLayout.show(contentPanel, viewName);

        // Show/hide sidebar based on view
        if (sidebar != null) {
            sidebar.setVisible(!"LOGIN".equals(viewName));
        }

        if (statusBar != null)
            statusBar.setView(viewName);
    }

    public void startDesignSession(double width, double depth, double height,
            Color wallColor, Color floorColor, Color ceilingColor,
            String roomType) {
        designController.createNewRoom(width, depth, height);
        models.Room room = designController.getCurrentRoom();
        room.setWallColor(wallColor);
        room.setFloorColor(floorColor);
        room.setCeilingColor(ceilingColor);
        room.setRoomType(roomType);
        editorPanel.setRoom(room);
        showView("EDITOR");
    }

    /** Legacy overload for backward compatibility */
    public void startDesignSession(double width, double depth, double height) {
        startDesignSession(width, depth, height,
                new Color(230, 230, 240), new Color(200, 180, 150), new Color(250, 250, 255), "CUSTOM");
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public controllers.DesignController getDesignController() {
        return designController;
    }
}
