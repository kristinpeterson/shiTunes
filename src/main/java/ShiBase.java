import java.sql.*;

/**
 *
 */
public class ShiBase {

    public static String DB_NAME = "ShiBase";
    public static final String TABLE_NAME = "music";
    public static final String[] COLUMNS =  {"Artist", "Title", "Album", "Year", "Genre", "Filename"};

    private static final String CREATE = ";create=true";
    private static final String PROTOCOL = "jdbc:derby:";
    private Connection conn;
    private Statement stmt;
    private boolean connected;


    public ShiBase() {
        connect();
        createTable();
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
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "APP", TABLE_NAME, null);
            if(!rs.next()) {
                String query = "CREATE TABLE " + TABLE_NAME +
                        " (filePath VARCHAR(200) NOT NULL, " +
                        "artist VARCHAR(100), " +
                        "title VARCHAR(150), " +
                        "album VARCHAR(150), " +
                        "year_released VARCHAR(4), " +
                        "genre VARCHAR(20))";
                stmt = conn.createStatement();
                stmt.execute(query);
                stmt.close();
            }
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
        if(songExists(song.getFilePath())){
            return false;
        }
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

    public boolean songExists(String filepath) {
        int rowCount = 0;

        try
        {
            stmt = conn.createStatement();

            String query = "SELECT count(*) AS rowcount FROM " + TABLE_NAME +
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

    public boolean deleteSong(Song song) {
        if(songExists(song.getFilePath())) {
            try {
                stmt = conn.createStatement();
                String query = "DELETE FROM " + TABLE_NAME + " WHERE " +
                        "filePath= '" + song.getFilePath() + "'";

                stmt.execute(query);
                stmt.close();
                return true;
            } catch (SQLException sqlExcept) {
                sqlExcept.printStackTrace();
                return false;
            }
        }
        return false;
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

    public void close() {
        try {
            conn.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
