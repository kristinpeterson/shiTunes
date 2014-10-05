import java.sql.*;

/**
 *
 */
public class ShiBase {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; //these two fields no necessarily correct
    private static String dbURL = "jdbc:mysql://localhost/Test";       //these two fields no necessarily correct
    private static String tableName = "musicDatabase";
    private static Connection conn = null;
    private static Statement stmt = null;

    public void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(dbURL);
        }
        catch (Exception except) {
            except.printStackTrace();
        }
    }

    public boolean isConnected() {
        return false;
    }

    public void createDatabase(String databaseName) { }

    public void dropDatabase(String databaseName) { }

    public void createTable(String tableName) {
        try
        {
            stmt = conn.createStatement();
            String query ="CREATE TABLE " + tableName +
                    " (filePath VARCHAR(200) NOT NULL" +
                    "track VARCHAR(150) " +
                    "artist VARCHAR(100) " +
                    "title VARCHAR(150) " +
                    "album VARCHAR(150) " +
                    "year VARCHAR(4) " +
                    "genre VARCHAR(20));";

            stmt.execute(query);
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    public void dropTable(String tableName) {
        try
        {
            stmt = conn.createStatement();
            String query ="DROP TABLE " + tableName + ";";

            stmt.execute(query);
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    public void insertSong(Song song) {
        try
        {
            stmt = conn.createStatement();

            //Static errors here. Either make Song object or make Song methods static.
            String query = "INSERT INTO " + tableName +
                    " (filepath, track, artist, title, album, year, genre)" +
                    "VALUES ( "
                    + "'" + Song.getFilePath() + "'," + "'" + Song.getTrack() + "',"
                    + "'" + Song.getArtist() + "'," + "'" + Song.getTitle() + "',"
                    + "'" + Song.getAlbum() + "'," + "'" + Song.getYear() + "',"
                    + "'" + Song.getGenre() + "');";

            stmt.execute(query);
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    public void deleteSong(Song song) {
        try
        {
            stmt = conn.createStatement();
            String query ="DELETE FROM " + tableName + " WHERE " +
                    "filePath= '" + song.getFilePath() + "';";

            stmt.execute(query);
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

}
