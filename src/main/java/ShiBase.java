import java.sql.*;

/**
 * The ShiBase class contains methods for interacting
 * with the shiTunes database
 *
 * @author shiTunes inc.
 */
public class ShiBase {

    /**
     * The database name
     */
    public static String DB_NAME = "ShiBase";
    /**
     * The music table name
     */
    public static final String MUSIC_TABLE = "MUSIC";
    /**
     * The columns of the music table
     */
    public static final String[] MUSIC_COLUMNS =  {"Artist", "Title", "Album", "Year", "Genre", "File Path"};

    private static final String CREATE = ";create=true";
    private static final String PROTOCOL = "jdbc:derby:";
    private Connection conn;
    private Statement stmt;
    private boolean connected;

    /**
     * The ShiBase default constructor
     * <p>
     * Connects to the database, if not already connected &
     * creates tables, if not already created
     */
    public ShiBase() {
        connect();
        createMusicTable();
    }

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
     * Creates music table, if it doesn't already exist
     *
     * @return true if table was created successfully
     */
    public boolean createMusicTable() {
        try
        {
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "APP", MUSIC_TABLE, null);
            if(!rs.next()) {
                String query = "CREATE TABLE " + MUSIC_TABLE +
                        " (filePath VARCHAR(200) NOT NULL, " +
                        "artist VARCHAR(100), " +
                        "title VARCHAR(150), " +
                        "album VARCHAR(150), " +
                        "year_released VARCHAR(4), " +
                        "genre VARCHAR(20))";
                stmt = conn.createStatement();
                stmt.execute(query);
                stmt.close();
                return true;
            }
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return false;
    }

    /**
     * Drops the given table from ShiBase database
     *
     * @return true if the table was dropped successfully
     */
    public boolean dropTable(String tableName) {
        try
        {
            stmt = conn.createStatement();
            String query ="DROP TABLE " + tableName;

            stmt.execute(query);
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
        try
        {
            stmt = conn.createStatement();

            //Static errors here. Either make Song object or make Song methods static.
            String query = "INSERT INTO " + MUSIC_TABLE +
                    " (filepath, artist, title, album, year_released, genre)" +
                    " VALUES ( "
                    + "'" + song.getFilePath() + "',"
                    + "'" + song.getArtist() + "'," + "'" + song.getTitle() + "',"
                    + "'" + song.getAlbum() + "'," + "'" + song.getYear() + "',"
                    + "'" + song.getGenre() + "')";
            stmt.execute(query);
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
     * Checks if song exists in database
     *
     * @param filepath the filepath of the song to look for in the database
     * @return true if the song exists in the database
     */
    public boolean songExists(String filepath) {
        int rowCount = 0;

        try
        {
            stmt = conn.createStatement();

            String query = "SELECT count(*) AS rowcount FROM " + MUSIC_TABLE +
                    " WHERE filepath='" + filepath + "'";
            ResultSet resultSet = stmt.executeQuery(query);
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
     * @param filePath the filepath of the song to delete
     * @return true if the song was successfully deleted, false if otherwise
     */
    public boolean deleteSong(String filePath) {
        if(songExists(filePath)) {
            try {
                stmt = conn.createStatement();
                String query = "DELETE FROM " + MUSIC_TABLE + " WHERE " +
                        "filePath= '" + filePath + "'";

                stmt.execute(query);
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
        String filePath;
        String artist;
        String title;
        String album;
        String year_released;
        String genre;

        try
        {
            stmt = conn.createStatement();

            // Get record count
            String rowCountQuery = "SELECT count(*) AS rowcount FROM " + MUSIC_TABLE;
            ResultSet rowCountRS = stmt.executeQuery(rowCountQuery);
            rowCountRS.next();
            rowCount = rowCountRS.getInt("rowcount");

            // Initialize multidimensional array large enough to hold all songs
            allSongs = new Object[rowCount][MUSIC_COLUMNS.length];

            // Get all records
            String allSongsQuery = "SELECT * FROM " + MUSIC_TABLE + " ORDER BY artist";
            ResultSet allSongsRS = stmt.executeQuery(allSongsQuery);

            while(allSongsRS.next()) {
                filePath = allSongsRS.getString("filePath");
                artist = allSongsRS.getString("artist");
                title = allSongsRS.getString("title");
                album = allSongsRS.getString("album");
                year_released = allSongsRS.getString("year_released");
                genre = allSongsRS.getString("genre");
                Object[] song = {artist, title, album, year_released, genre, filePath};
                allSongs[index] = song;
                index++;
            }
            stmt.close();
            return allSongs;
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return new Object[0][0];
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
}
