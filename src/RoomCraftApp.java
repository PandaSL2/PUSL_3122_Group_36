import javax.swing.SwingUtilities;
import ui.MainFrame;
import ui.SplashScreen;

/**
 * RoomCraftApp — Application Entry Point.
 * Shows animated splash screen then launches the main window.
 */
public class RoomCraftApp {
    public static void main(String[] args) {
        // Show animated splash screen (3 seconds)
        SplashScreen.showAndWait(2800);

        // Launch main application on EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
