/**
 * This is the main entry point of the shiTunes application
 * handling initialization and display of the GUI
 *
 * @author shiTunes inc.
 */
public class ShiTunes {
    static MusicPlayer player;
    static ShiBase db;
    static MusicLibrary library;

    static {
        player = new MusicPlayer();
        db = new ShiBase();
        library = new MusicLibrary();
    }

    public static void main(String[] args) {
        // Init GUI
        GUI gui = new GUI();

        // Display GUI once initialized
        gui.displayGUI();
    }
}
