import java.sql.DriverManager;
import java.sql.Connection;

/**
 *
 */
public static class ShiBase {
    private static final Connection conn;

    public void connect() {
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        conn = DriverManager.getConnection("jdbc:derby:shiTunes");
    }

    public boolean isConnected() {
        return false;
    }

    public void createDatabase(String databaseName) { }

    public void dropDatabase(String databaseName) {
        return false;
    }

    public void createTable(String tableName) { }

    public void dropTable(String tableName) {
        return false;
    }

    public void insertSong(Song song) {
        return false;
    }

    public void deleteSong(Song song) {
        return false;
    }

}
