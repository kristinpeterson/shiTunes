import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * The MusicTable class contains methods related
 * to MusicTable creation and selected song tracking
 *
 * @author shiTunes inc.
 */
public class MusicTable {

    private JTable table;
    private String selectedSong;
    private int[] selectedSongRange;    // [min-index, max-index]

    /**
     * Default constructor for MusicTable
     * by default populates a JTable with the entire
     * contents of the users music library
     *
     */
    public MusicTable(){
        createLibrary();
    }

    /**
     * Overloaded constructor for MusicTable
     * that populates a JTable with the songs
     * associated with the given playlist
     *
     * @param playlistName the playlist to populate the JTable with
     */
    public MusicTable(String playlistName) {
        createPlaylist(playlistName);
    }

    /**
     * Creates the ShiTunes music Library table
     */
    private void createLibrary() {
        // Build library table
        // Instantiate library table model - this prevents individual cells from being editable
        DefaultTableModel tableModel = new DefaultTableModel(ShiTunes.db.getAllSongs(), ShiBase.SONG_COLUMN_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        table = new JTable(tableModel);
    }

    /**
     * Creates a JTable containing the songs
     * associated with the given playlist
     *
     * @param playlistName the name of the playlist to populate the table
     * @return the JTable object containing the given playlist's songs
     */
    private void createPlaylist(String playlistName) {
        // Build library table
        // Instantiate library table model - this prevents individual cells from being editable
        DefaultTableModel tableModel = new DefaultTableModel(
                ShiTunes.db.getPlaylistSongs(playlistName), ShiBase.SONG_COLUMN_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        table = new JTable(tableModel);
    }

    /**
     * Returns the ShiTunes table
     *
     * @return the ShiTunes Music
     */
    public JTable getTable() {
        return table;
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
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.addRow(new Object[]{song.getArtist(), song.getTitle(), song.getAlbum(),
                    song.getYear(), song.getGenre(), song.getFilePath(), song.getComment()});
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
     * Sets the selected songs index range
     *
     * @param min the minimum index of the selected range of songs
     * @param max the maximum index of the selected range of songs
     */
    public void setSelectedSongRange(int min, int max) {
        selectedSongRange = new int[] {min, max};
    }

    /**
     * Gets the selected songs index range
     *
     * @return the selected song range as an array [min-index, max-index]
     */
    public int[] getSelectedSongRange() {
        return selectedSongRange;
    }

    /**
     * Checks if a song is already listed in the library table
     *
     * @param filePath the filepath of the song being searched for
     * @return true if the song exists in the library table
     */
    public boolean songExistsInLibraryTable(String filePath) {
        for(int i = 0; i < table.getRowCount(); i++) {
            if(filePath.equals(table.getValueAt(i, 5))) {
                return true;
            }
        }
        return false;
    }
}
