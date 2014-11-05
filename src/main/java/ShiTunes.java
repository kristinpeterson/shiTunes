/**
 * This is the main entry point of the shiTunes application
 * handling initialization and display of the GUI
 *
 * @author shiTunes inc.
 */
public class ShiTunes {
    static MusicPlayer player;
    static ShiBase db;
    static Window mainWindow;

    static {
        player = new MusicPlayer();
        db = new ShiBase();
        mainWindow = new Window(Window.MAIN, new MusicTable());
    }

    public static void main(String[] args) {
        // Display the main application window
        mainWindow.display();
    }
}
