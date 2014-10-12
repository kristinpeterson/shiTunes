/**
 * This is the main entry point of the shiTunes application
 * handling initialization and display of the GUI
 *
 * @author shiTunes inc.
 */
public class ShiTunes {
    public static void main(String[] args) {

        // Initialize and Connect to ShiBase database
        ShiBase db = new ShiBase();
        db.connect();

        // Initialize persistent MusicPlayer
        MusicPlayer player = new MusicPlayer();

        // Init GUI
        GUI gui = new GUI(db, player);

        // Display GUI once initialized
        gui.displayGUI();

    }
}
