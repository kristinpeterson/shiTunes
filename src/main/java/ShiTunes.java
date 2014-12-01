import java.util.ArrayList;

/**
 * This is the main entry point of the shiTunes application
 * handling initialization and display of the GUI
 *
 * @author shiTunes inc.
 */
public class ShiTunes {
    static ShiBase db;
    static Window mainWindow;
    static ArrayList<Window> windows;

    static {
        db = new ShiBase();
    }

    public static void main(String[] args) {
        // An array list of shiTunes application windows
        windows = new ArrayList<>();

        // The main shiTunes application window
        mainWindow = new Window();

        // Display main window
        mainWindow.display();

        // Add main application window to list of ShiTunes windows
        windows.add(mainWindow);
    }

    /*
    * Updates the table model for all open windows
    *
    */
    public static void updateAllWindows() {
        for(Window w : windows) {
            if(w.getWindowType() == Window.MAIN) {
                if(w.getMusicTable().getType() == MusicTable.LIBRARY) {
                    w.getMusicTable().updateTableModel("Library");
                }
            } else {
                w.getMusicTable().updateTableModel(w.getSelectedPlaylist());
            }
        }
    }
}
