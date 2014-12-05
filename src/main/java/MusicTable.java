import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

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

        // Disable autoCreateColumnsFromModel
        table.setAutoCreateColumnsFromModel(false);

        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {

            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                // update all column indices in db if a column is moved
                // all have to be updated because moving one column can effect multiple other column indices
                if(e.getToIndex() != e.getFromIndex()) {
                    for(int i = 0; i < SONG_COLUMN_NAMES.length; i++) {
                        String columnName = SONG_COLUMN_NAMES[i];
                        int modelIndex = table.getColumn(columnName).getModelIndex();
                        int viewIndex = table.convertColumnIndexToView(modelIndex);
                        // using view index in db
                        // (as model index is constant, view index indicates where column is in table)
                        ShiTunes.db.setColumnIndex(columnName, viewIndex);
                    }
                }
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {

            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {

            }
        });

        // set the column order based on persistent configuration
        setColumnOrder();

        // Hide id & file path columns
        hide("ID");
        hide("File Path");

        // update table model structure
        tableModel.fireTableStructureChanged();
    }

    /*
    *   Removes all columns and adds back in correct order
    */
    private void setColumnOrder() {
        int[] orderedColumns = ShiTunes.db.getColumnOrder();    // all column view indices
        TableColumn column[] = new TableColumn[SONG_COLUMN_NAMES.length];
        TableColumnModel columnModel = table.getColumnModel();

        // get all column objects, in persistent configuration order
        for (int i = 0; i < column.length; i++) {
            // use model index when interacting with table model
            int modelIndex = table.convertColumnIndexToModel(orderedColumns[i]);
            column[i] = columnModel.getColumn(modelIndex);
        }

        // remove each column
        while (columnModel.getColumnCount() > 0) {
            columnModel.removeColumn(columnModel.getColumn(0));
        }

        // add columns back in persistent configuration order
        for (int i = 0; i < column.length; i++) {
            columnModel.addColumn(column[i]);
        }
    }

    /**
     * Hides given column
     *
     * @param columnName column to hide
     */
    public void hide(String columnName) {
        int viewIndex = ShiTunes.db.getColumnIndex(columnName);
        int modelIndex = table.convertColumnIndexToModel(viewIndex);

        TableColumn column = table.getColumnModel().getColumn(modelIndex);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setWidth(0);

        // update visibility in db based on view index
        ShiTunes.db.setColumnVisible(viewIndex, false);
    }

    /**
     * Shows given column (unhide)
     *
     * @param columnName show the given column
     */
    public void show(String columnName) {
        int viewIndex = ShiTunes.db.getColumnIndex(columnName);
        int modelIndex = table.convertColumnIndexToModel(viewIndex);

        TableColumn column = table.getColumnModel().getColumn(modelIndex);
        column.setMinWidth(10);
        column.setMaxWidth(500);
        column.setWidth(10);
        column.setPreferredWidth(80);

        // update visibility in db
        ShiTunes.db.setColumnVisible(viewIndex, true);
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

    /**
     * Gets the index number of the given column
     *
     * @param column the name of the column being searched for
     * @return the index of the given column
     *          -1 if not found
     */
    public int getColumnIndex(String column) {

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(column)) {
                return i;
            }
        }
        return -1;
    }
}
