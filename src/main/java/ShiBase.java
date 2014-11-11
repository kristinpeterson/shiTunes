import java.sql.*;
import java.util.ArrayList;

/**
 * The ShiBase class contains methods for connecting to
 * and interacting with the shiTunes database
 *
 * @author shiTunes inc.
 */
public class ShiBase {

    /**
     * The database name
     */
    public static String DB_NAME = "ShiBase";
    /**
     * Table Names
     */
    public static final String SONG_TABLE = "SONG";
    public static final String PLAYLIST_TABLE = "PLAYLIST";
    public static final String PLAYLIST_SONG_TABLE = "PLAYLIST_SONG";
    /**
     * The columns of the SONG table properly formatted for GUI
     */
    public static final String[] SONG_COLUMN_NAMES =  {"Artist", "Title", "Album", "Year", "Genre", "File Path", "Comment"};

    // The database table column names
    // SONG Table
    private static final String[] SONG_COLUMNS =  {"songId", "artist", "title", "album", "yearReleased",
            "genre", "filePath", "comment"};
    // PLAYLIST Table
    private static final String[] PLAYLIST_COLUMNS = {"playlistId", "playlistName"};
    // PLAYLIST_SONG Table
    private static final String[] PLAYLIST_SONG_COLUMNS = {"playlistId", "songId"};

    // Database connection related variables
    private static final String CREATE = ";create=true";
    private static final String PROTOCOL = "jdbc:derby:";
    private Connection conn;
    private PreparedStatement stmt;
    private boolean connected;

    /**
     * The ShiBase default constructor
     * <p>
     * Connects to the database, if not already connected &
     * creates tables, if not already created
     */
    public ShiBase() {
        connect();      // creates db if not already present
        createTables(); // if not already present
    }

    /* ************************ */
    /* ************************ */
    /* GENERAL DATABASE METHODS */
    /* ************************ */
    /* ************************ */

    /**
     * Connects to the database, creates database if not already
     * created
     *
     */
    public void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(PROTOCOL + DB_NAME);
            // getConnection() can also have a second parameter, Properties,  to add username/password etc
            connected = true;
        }
        catch (Exception except) {
            // If database does not exist; create database
            createDatabase();
        }
    }

    /**
     * Checks if database is connected
     *
     * @return true if database is connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Closes ShiBase database connection
     *
     * @return true if connection closed successfully
     */
    public boolean close() {
        try {
            conn.close();
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return false;
    }

    /**
     * Creates ShiBase database
     *
     * @return true if database was created successfully
     */
    public boolean createDatabase() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(PROTOCOL + DB_NAME + CREATE);
            // getConnection() can also have a second parameter, Properties,  to add username/password etc
            connected = true;
            return true;
        } catch (Exception except) {
            except.printStackTrace();
            return false;
        }
    }

    /**
     * Drops the given table from ShiBase database
     *
     * @return true if the table was dropped successfully
     */
    public boolean dropTable(String tableName) {
        try
        {
            String query ="DROP TABLE " + tableName;
            stmt = conn.prepareStatement(query);
            stmt.execute();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Creates the database tables, if not already present
     */
    public void createTables() {
        createSongTable();
        createPlaylistTable();
        createPlaylistSongTable();
    }

    /* ******************* */
    /* ******************* */
    /* SONG TABLE  METHODS */
    /* ******************* */
    /* ******************* */

    /**
     * Creates SONG table, if it doesn't already exist
     *
     * @return true if table was created successfully
     */
    public boolean createSongTable() {
        try {
            String query = "CREATE TABLE " + SONG_TABLE +
                    " (songId INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "filePath VARCHAR(200) UNIQUE NOT NULL, " +
                    "artist VARCHAR(100), " +
                    "title VARCHAR(150), " +
                    "album VARCHAR(150), " +
                    "yearReleased VARCHAR(4), " +
                    "genre VARCHAR(20), " +
                    "comment VARCHAR(200), " +
                    "PRIMARY KEY (songId))";
            stmt = conn.prepareStatement(query);
            stmt.execute();
            stmt.close();
            return true;
        }
        catch (SQLException sqlExcept) {
            // Table Exists
        }
        return false;
    }

    /**
     * Inserts the given song into the ShiBase database
     *
     * @param song the song to insert into the database
     * @return true if the song was inserted successfully
     *         false if the song already exists, or the insert failed
     */
    public boolean insertSong(Song song) {
        if(songExists(song.getFilePath())){
            return false;
        }
        try {
            String query = "INSERT INTO " + SONG_TABLE +
                    " (filePath, artist, title, album, yearReleased, genre, comment)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, song.getFilePath());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getTitle());
            stmt.setString(4, song.getAlbum());
            stmt.setString(5, song.getYear());
            stmt.setString(6, song.getGenre());
            stmt.setString(7, song.getComment());
            stmt.execute();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Checks if song exists in database
     *
     * @param filePath the filePath of the song to look for in the database
     * @return true if the song exists in the database
     */
    public boolean songExists(String filePath) {
        int rowCount = 0;

        try {
            String query = "SELECT count(*) AS rowcount FROM " + SONG_TABLE +
                    " WHERE filePath=?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, filePath);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            rowCount = resultSet.getInt("rowcount");
            stmt.close();
            if(rowCount != 0) {
                // song exists, return true
                return true;
            }
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            return false;
        }
        // song doesn't exist
        return false;
    }

    /**
     * Deletes a given song from the database
     *
     * @param filePath the filePath of the song to delete
     * @return true if the song was successfully deleted, false if otherwise
     */
    public boolean deleteSong(String filePath) {
        if(songExists(filePath)) {
            try {
                String query = "DELETE FROM " + SONG_TABLE + " WHERE filePath=?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, filePath);
                stmt.execute();
                stmt.close();
                return true;
            } catch (SQLException sqlExcept) {
                sqlExcept.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Gets all songs from the database
     *
     * @return all songs from the database as a multidimensional object array
     */
    public Object[][] getAllSongs()
    {
        Object[][] allSongs;
        int rowCount = 0;
        int index = 0;

        try {
            // Get record count
            String rowCountQuery = "SELECT count(*) AS rowcount FROM " + SONG_TABLE;
            stmt = conn.prepareStatement(rowCountQuery);
            ResultSet rowCountRS = stmt.executeQuery();
            rowCountRS.next();
            rowCount = rowCountRS.getInt("rowcount");

            // Initialize multidimensional array large enough to hold all songs
            allSongs = new Object[rowCount][SONG_COLUMNS.length];

            // Get all records
            String allSongsQuery = "SELECT * FROM " + SONG_TABLE + " ORDER BY artist";
            stmt = conn.prepareStatement(allSongsQuery);
            ResultSet allSongsRS = stmt.executeQuery();

            while(allSongsRS.next()) {
                allSongs[index] = getSongRow(allSongsRS);
                index++;
            }
            stmt.close();
            return allSongs;
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return new Object[0][0];
    }

    /**
     * Get the unique integer id of a song based on its
     * file path (which is also unique)
     *
     * @param filePath the filepath of the song being searched for
     * @return the unique integer id of the song being searched for
     *         returns -1 if not found
     */
    private int getSongId(String filePath) {
        int songId = -1;
        try {
            String query = "SELECT * FROM " + SONG_TABLE + " WHERE filePath=?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, filePath);
            ResultSet songIdRS = stmt.executeQuery();
            if(songIdRS.next()) {
                songId = songIdRS.getInt("songId");
            }
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return songId;
    }

    /**
     * Returns the given SONG row as a String array
     *
     * @param rs the current result set item
     * @return the given result from the SONG table as a String array
     */
    private String[] getSongRow(ResultSet rs) {
        String[] song = new String[SONG_COLUMN_NAMES.length];
        try {
            for(int i = 0; i < SONG_COLUMN_NAMES.length; i++) {
                song[i] = rs.getString(SONG_COLUMNS[i+1]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return song;
    }

    /* ********************** */
    /* ********************** */
    /* PLAYLIST TABLE METHODS */
    /* ********************** */
    /* ********************** */

    /**
     * Accessor method to get all playlist names
     *
     * @return an ArrayList of playlist names as Strings
     */
    public ArrayList<String> getPlaylistNames() {
        ArrayList<String> playlistNames = new ArrayList<String>();
        try {
            // Get all playlist names
            String query = "SELECT playlistName FROM " + PLAYLIST_TABLE +
                    " ORDER BY playlistName ASC";
            stmt = conn.prepareStatement(query);
            ResultSet playlistRS = stmt.executeQuery();
            while(playlistRS.next()) {
                playlistNames.add(playlistRS.getString("playlistName"));
            }
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return playlistNames;
    }

    /**
     * Create the PLAYLIST table
     *
     * @return true if table created successfully
     */
    public boolean createPlaylistTable() {
        try {
            String query = "CREATE TABLE " + PLAYLIST_TABLE +
                    " (playlistId INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "playlistName VARCHAR(100) UNIQUE NOT NULL, " +
                    "PRIMARY KEY (playlistId))";
            stmt = conn.prepareStatement(query);
            stmt.execute();
            stmt.close();
            return true;
        }
        catch (SQLException sqlExcept) {
            // Table Exists
        }
        return false;
    }

    /**
     * Add a new playlist to the PLAYLIST table
     *
     * @param playlist the name of the newly created playlist
     * @return true if entry successfully added to table
     */
    public boolean addPlaylist(String playlist) {
        try {
            String query = "INSERT INTO " + PLAYLIST_TABLE + " (playlistName) VALUES (?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, playlist);
            stmt.execute();
            stmt.close();

            //playlistNames.add(playlist);
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Delete a playlist from the PLAYLIST table
     *
     * @param playlist the name of playlist to be deleted
     * @return true if entry successfully deleted from table
     */
    public boolean deletePlaylist(String playlist) {
        try {
            String query = "DELETE FROM " + PLAYLIST_TABLE +
                    " WHERE playlistName = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, playlist);
            stmt.execute();
            stmt.close();

            //playlistNames.remove(playlist);
            return true;
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return false;
    }

    /**
     * Adds the given song to given playlist
     *
     * @param filePath the filePath of the song being added
     * @param playlistName the name of the playlist to add the given song to
     * @return true if song successfully added to playlist
     */
    public boolean addSongToPlaylist(String filePath, String playlistName) {
        try {
            int songId = getSongId(filePath);
            int playlistId = getPlaylistId(playlistName);
            if(songId!= -1 && playlistId != -1) {
                // SUCCESS: song and playlist id's found
                String query = "INSERT INTO " + PLAYLIST_SONG_TABLE +
                        " (playlistId, songId) " +
                        " VALUES (" + playlistId + ", " + songId + ")";
                stmt = conn.prepareStatement(query);
                stmt.execute();
                stmt.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes the given song to given playlist
     *
     * @param filePath the filePath of the song being deleted
     * @param playlist the playlist to delete the given song from
     * @return true if song successfully deleted to playlist
     */
    public boolean deleteSongFromPlaylist(String filePath, String playlist) {
        try {
            int songId = getSongId(filePath);
            int playlistId = getPlaylistId(playlist);
            if(songId!= -1 && playlistId != -1) {
                // SUCCESS: song and playlist id's found
                String query = "DELETE FROM " + PLAYLIST_SONG_TABLE +
                        " WHERE playlistId = " + playlistId +
                        " AND songId = " + songId;
                stmt = conn.prepareStatement(query);
                stmt.execute();
                stmt.close();
                return true;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Create the junction table that will associate a Song with
     * a Playlist by Primary Key (id)
     *
     * @return true if table created successfully
     */
    public boolean createPlaylistSongTable() {
        try {
            String query = "CREATE TABLE " + PLAYLIST_SONG_TABLE +
                    "(playlistId INTEGER NOT NULL, " +
                    "songId INTEGER NOT NULL, " +
                    "CONSTRAINT fk_songId FOREIGN KEY (songId) " +
                    "REFERENCES " + SONG_TABLE + " (songId) " +
                    "ON DELETE CASCADE, " +
                    "CONSTRAINT fk_playlistId FOREIGN KEY (playlistId) " +
                    "REFERENCES " + PLAYLIST_TABLE + " (playlistId) " +
                    "ON DELETE CASCADE )";
            stmt = conn.prepareStatement(query);
            stmt.execute();
            stmt.close();
            return true;
        }
        catch (SQLException sqlExcept) {
            // Table Exists
        }
        return false;
    }

    /**
     * Get all the songs associated with the given playlistId
     *
     * @param playlistName the name of the playlist to get all songs from
     * @return an ArrayList of Songs associated with the given playlist
     */
    public Object[][] getPlaylistSongs(String playlistName) {
        Object[][] playlistSongs;
        int playlistId = getPlaylistId(playlistName);
        int rowCount = 0;
        int index = 0;

        try {
            // Get record count - which will be the size of
            // the first dimension of the multidimensional array
            // this method returns (ie. the number of songs in playlist)
            String rowCountQuery = "SELECT count(*) AS rowcount FROM " + PLAYLIST_SONG_TABLE +
                    " WHERE playlistId = " + playlistId;
            stmt = conn.prepareStatement(rowCountQuery);
            ResultSet rowCountRS = stmt.executeQuery();
            rowCountRS.next();
            rowCount = rowCountRS.getInt("rowcount");

            // Initialize multidimensional array large enough to hold all songs in playlist
            playlistSongs = new Object[rowCount][SONG_COLUMNS.length];

            // Get all playlist songs
            String query = "SELECT * FROM " + SONG_TABLE +
                    " JOIN " + PLAYLIST_SONG_TABLE +
                    " USING (songId) WHERE playlistID = " + playlistId;
            stmt = conn.prepareStatement(query);
            ResultSet playlistSongsRS = stmt.executeQuery();

            while(playlistSongsRS.next()) {
                playlistSongs[index] = getSongRow(playlistSongsRS);
                index++;
            }
            stmt.close();
            return playlistSongs;
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return new Object[0][0];
    }

    /**
     * Get the unique integer id of a playlist based on its
     * name (which is also unique)
     *
     * @param playlistName the name of the playlist being searched for
     * @return the unique integer id of the playlist being searched for
     *         returns -1 if not found
     */
    private int getPlaylistId(String playlistName) {
        int playlistId = -1;
        try {
            String query = "SELECT * FROM " + PLAYLIST_TABLE + " WHERE playlistName=?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, playlistName);
            ResultSet songIdRS = stmt.executeQuery();
            if(songIdRS.next()) {
                playlistId = songIdRS.getInt("playlistId");
            }
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return playlistId;
    }
}