import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;

/**
 * Suite of tests for the ShiBase class
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShiBaseTests {

    Song song = new Song(getClass().getResource("/mp3/test.mp3").getPath());
    ShiBase db= new ShiBase();

    @Test
    public void testA_DBConnect() {
        assertEquals(true, db.isConnected());
    }

    @Test
    public void testB_InsertSong() {
        assertEquals(true, db.insertSong(song));
    }

    @Test
    public void testC_GetAllSongs() {
        Object[][] allSongs = db.getAllSongs();
        assertEquals(true, allSongs.length > 0);
    }

    @Test
    public void testD_SongExists() {
        assertEquals(true, db.songExists(song.getFilePath()));
    }

    @Test
    public void testE_DeleteSong() {
        assertEquals(true, db.deleteSong(song.getFilePath()));
    }

    @Test
    public void testF_DropTable() {
        assertEquals(true, db.dropTable(ShiBase.MUSIC_TABLE));
    }

    @Test
    public void testG_Close() {
        assertEquals(true, db.close());
    }
}
