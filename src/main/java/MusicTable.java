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

    private int type;
    private JTable table;

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
        type = LIBRARY;  // Set table type
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
        type = PLAYLIST; // Set table type
    }

    /**
     * Build music table based on given set of songs
     * Instantiate and configures music table model
     *
     * Note: this method is called whenever a table is updated
     * via MusicTable.updateTableModel() ensuring that the table
     * model is *always* configured properly
     *
     * @param songs
     */
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
        table.getTableHeader().setReorderingAllowed(false); // don't allow reordering of columns
        setColumnVisibility();
    }

    /**
     * Set visibility for all columns based on state saved in db
     *
     */
    public void setColumnVisibility() {
        for(int i = 0; i < SONG_COLUMN_NAMES.length; i++) {
            String columnName = SONG_COLUMN_NAMES[i];
            if (ShiTunes.db.getColumnVisible(columnName)) {show(columnName);}
            else {hide(columnName);}
        }
    }

    /**
     * Hides given column from view in the table model
     * and sets column visible state in db to false
     *
     * @param columnName column to hide
     */
    public void hide(String columnName) {
        int index = table.getColumnModel().getColumnIndex(columnName);

        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setWidth(0);

        // update visibility in db based on view index
        ShiTunes.db.setColumnVisible(columnName, false);
    }

    /**
     * Shows given column (unhide) in the table model
     * and sets column visible state in db to true
     *
     * @param columnName show the given column
     */
    public void show(String columnName) {
        int index = table.getColumnModel().getColumnIndex(columnName);

        TableColumn column = table.getColumnModel().getColumn(index);
        column.setMinWidth(10);
        column.setMaxWidth(500);
        column.setWidth(10);
        column.setPreferredWidth(80);

        // update visibility in db
        ShiTunes.db.setColumnVisible(columnName, true);
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
            type = LIBRARY;
        } else {
            // update with playlist contents
            buildTable(ShiTunes.db.getPlaylistSongs(model));
            type = PLAYLIST;
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
     * Gets this MusicTable object's type (LIBRARY or PLAYLIST)
     *
     * @return the table type
     */
    public int getType() {
        return type;
    }

    /**
     * Gets min index of the selected row range.
     * If single row selected, this will double as the selected row getter.
     *
     */
    public int getMinSelectedRow() {
        return table.getSelectedRows()[0];
    }

    /**
     * Gets max index of the selected row range.
     *
     */
    public int getMaxSelectedRow() {
        int maxIndex = table.getSelectedRows().length - 1;
        return table.getSelectedRows()[maxIndex];
    }
}
