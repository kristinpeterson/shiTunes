import java.sql.*;

/**
 *
 */
public class ShiBase {

    public static String DB_NAME = "ShiBase";

    private static final String CREATE = ";create=true";
    private static final String PROTOCOL = "jdbc:derby:";
    private static final String TABLE_NAME = "music";
    private Connection conn;
    private Statement stmt;
    private boolean connected;


    public ShiBase() {
        conn = null;
    }

    public void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(PROTOCOL + DB_NAME);
            System.out.println(DB_NAME + " CONNECTED!");
            // getConnection() can also have a second parameter, Properties,  to add username/password etc
            connected = true;
        }
        catch (Exception except) {
            except.printStackTrace();
            // If database does not exist; create database
            createDatabase();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean createDatabase() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(PROTOCOL + DB_NAME + CREATE);
            // getConnection() can also have a second parameter, Properties,  to add username/password etc
            System.out.println(DB_NAME + " CREATED!");
            connected = true;
            return true;
        } catch (Exception except) {
            except.printStackTrace();
            return false;
        }
    }

    public boolean createTable() {
        try
        {
            stmt = conn.createStatement();
            String query ="CREATE TABLE " + TABLE_NAME +
                    " (filePath VARCHAR(200) NOT NULL, " +
                    "artist VARCHAR(100), " +
                    "title VARCHAR(150), " +
                    "album VARCHAR(150), " +
                    "year_released VARCHAR(4), " +
                    "genre VARCHAR(20))";

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

    public boolean dropTable() {
        try
        {
            stmt = conn.createStatement();
            String query ="DROP TABLE " + TABLE_NAME;

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

    public boolean insertSong(Song song) {
        try
        {
            stmt = conn.createStatement();

            //Static errors here. Either make Song object or make Song methods static.
            String query = "INSERT INTO " + TABLE_NAME +
                    " (filepath, artist, title, album, year_released, genre)" +
                    "VALUES ( "
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

    public boolean deleteSong(Song song) {
        try
        {
            stmt = conn.createStatement();
            String query ="DELETE FROM " + TABLE_NAME + " WHERE " +
                    "filePath= '" + song.getFilePath() + "'";

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
            String rowCountQuery = "SELECT count(*) AS rowcount FROM " + TABLE_NAME;
            ResultSet rowCountRS = stmt.executeQuery(rowCountQuery);
            rowCountRS.next();
            rowCount = rowCountRS.getInt("rowcount");

            // Initialize multidimensional array of size [rowCount][6]
            // 6 being the column count
            allSongs = new Object[rowCount][6];

            // Get all records
            String allSongsQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY artist";
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
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
            return new Object[0][0];
        }
        return allSongs;
    }

}
