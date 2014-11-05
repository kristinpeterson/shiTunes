/**
 * This is the main entry point of the shiTunes application
 * handling initialization and display of the GUI
 *
 * @author shiTunes inc.
 */
public class ShiTunes {
    static MusicPlayer player;
    static ShiBase db;

    static {
        player = new MusicPlayer();
        db = new ShiBase();
    }

    public static void main(String[] args) {
        new Window().display();
    }
}
