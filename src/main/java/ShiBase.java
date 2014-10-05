import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 */
public class ShiBase {

    private static String protocol = "jdbc:derby:";       //these two fields no necessarily correct
    private static String dbName = "shiTunes";
    private static String tableName = "musicDatabase";
    private static Connection conn = null;
    private static Statement stmt = null;

    public static void connect() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(protocol + dbName );
            // getConnection() can also have a second parameter, Properties,  to add username/password etc
        }
        catch (Exception except) {
            except.printStackTrace();
        }
    }

    public static boolean isConnected() {
        return false;
    }

    public static void createDatabase(String databaseName) { }

    public static void dropDatabase(String databaseName) { }

    public static void createTable(String tableName) {
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

    public static void dropTable(String tableName) {
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

    public static void insertSong(Song song) {
        try
        {
            stmt = conn.createStatement();

            //Static errors here. Either make Song object or make Song methods static.
            String query = "INSERT INTO " + tableName +
                    " (filepath, track, artist, title, album, year, genre)" +
                    "VALUES ( "
                    + "'" + song.getFilePath() + "'," + "'" + song.getTrack() + "',"
                    + "'" + song.getArtist() + "'," + "'" + song.getTitle() + "',"
                    + "'" + song.getAlbum() + "'," + "'" + song.getYear() + "',"
                    + "'" + song.getGenre() + "');";

            stmt.execute(query);
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    public static void deleteSong(Song song) {
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
