import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * The MusicTable class contains methods related
 * to MusicTable creation and selected song tracking
 *
 * @author shiTunes inc.
 */
public class MusicTable {

    // To indicate MusicTable type
    public static int LIBRARY = 0;
    public static int PLAYLIST = 1;
    public int type;

    private JTable table;
    private int[] selectedSongRange;    // [min-index, max-index]
    private int selectedSongRow;

    /**
     * The columns of the SONG table properly formatted for GUI
     */
    public static final String[] SONG_COLUMN_NAMES =  {"ID", "Artist", "Title", "Album", "Year",
                                                        "Genre", "File Path", "Comment"};

    /**
     * Default constructor for MusicTable
     * by default populates a JTable with the entire
     * contents of the users music library
     *
     */
    public MusicTable(){
        table = new JTable();
        buildTable(ShiTunes.db.getAllSongs());
        type = MusicTable.LIBRARY;  // Set table type
    }

    /**
     * Overloaded constructor for MusicTable
     * that populates a JTable with the songs
     * associated with the given playlist
     *
     * @param playlistName the playlist to populate the JTable with
     */
    public MusicTable(String playlistName) {
        table = new JTable();
        buildTable(ShiTunes.db.getPlaylistSongs(playlistName));
        type = MusicTable.PLAYLIST; // Set table type
    }

    // Build music table based on given set of songs
    // Instantiate music table model
    private void buildTable(Object[][] songs) {
        DefaultTableModel tableModel = new DefaultTableModel(songs, SONG_COLUMN_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // all cells false -
                // this prevents individual cells from being editable
                return false;
            }
        };

        table.setModel(tableModel);

        // Hide id & file path columns
        hide(0);  // id
        hide(6);  // file path
    }

    private void hide(int index) {
        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setWidth(0);
        column.setPreferredWidth(0);
    }

    /**
     * Updates the MusicTable model based on given model input
     * where model is either "Library" or "[playlist-name]"
     *
     * @param model
     */
    public void updateTableModel(String model) {
        if (model.equals("Library")) {
            // update with library contents
            buildTable(ShiTunes.db.getAllSongs());
            type = MusicTable.LIBRARY;
        } else {
            // update with playlist contents
            buildTable(ShiTunes.db.getPlaylistSongs(model));
            type = MusicTable.PLAYLIST;
        }
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
     * @param id the unique database id of the song
     * @param song the song to add to the library/database
     */
    public void addSongToTable(int id, Song song) {
        // Add row to JTable
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[]{String.valueOf(id), song.getArtist(), song.getTitle(), song.getAlbum(),
                song.getYear(), song.getGenre(), song.getFilePath(), song.getComment()});
    }

    /**
     * Sets the selected songs index range
     *
     * @param min the minimum index of the selected range of songs
     * @param max the maximum index of the selected range of songs
     */
    public void setSelectedSongs(int min, int max) {
        selectedSongRange = new int[] {min, max};
        selectedSongRow = min;
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
     * Gets the selected songs row index
     *
     * @return the selected song row index
     */
    public int getSelectedSongRow() {
        return selectedSongRow;
    }

}
