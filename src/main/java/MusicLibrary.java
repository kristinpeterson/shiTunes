import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * The MusicLibrary class contains methods related
 * to the MusicLibrary
 *
 * @author shiTunes inc.
 */
public class MusicLibrary {

    private JTable libTable;
    private String selectedSong;

    /**
     * Default constructor for ShiTunes MusicLibrary
     * Calls createTable() to create MusicLibrary table
     *
     */
    public MusicLibrary(){
        createTable();
    }

    /**
     * Creates the ShiTunes MusicLibrary table
     */
    private void createTable() {
        // Build library table
        // Instantiate library table model - this prevents individual cells from being editable
        DefaultTableModel tableModel = new DefaultTableModel(ShiTunes.db.getAllSongs(), ShiBase.MUSIC_COLUMNS) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        libTable = new JTable(tableModel);
    }

    /**
     * Returns the ShiTunes MusicLibrary table
     *
     * @return the ShiTunes Music
     */
    public JTable getTable() {
        return libTable;
    }

    /**
     * Adds the given song to the library list and database
     *
     * @param song the song to add to the library/database
     */
    public void addSongToLibrary(Song song) {
        if(ShiTunes.db.insertSong(song)) {
            // insert song was successful
            // Add row to JTable
            DefaultTableModel model = (DefaultTableModel) libTable.getModel();
            model.addRow(new Object[]{song.getArtist(), song.getTitle(), song.getAlbum(),
                    song.getYear(), song.getGenre(), song.getFilePath()});
        } else {
            // TODO: display something that tells the user the song isn't being added
        }
    }

    /**
     * Gets the selected songs file path
     *
     * @return the selected song file path
     */
    public String getSelectedSong() {
        return selectedSong;
    }

    /**
     * Sets the selected songs file path and index
     *
     * @param index the selected song file path
     */
    public void setSelectedSong(int index) {
        if(index >= 0) {
            this.selectedSong = ShiTunes.library.getTable().getValueAt(index, 5).toString();
        }
    }

    /**
     * Checks if a song is already listed in the library table
     *
     * @param filePath the filepath of the song being searched for
     * @return true if the song exists in the library table
     */
    public boolean songExistsInLibraryTable(String filePath) {
        for(int i = 0; i < libTable.getRowCount(); i++) {
            if(filePath.equals(libTable.getValueAt(i, 5))) {
                return true;
            }
        }
        return false;
    }
}