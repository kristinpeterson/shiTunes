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
    String playlist = "Polka";
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
    public void testE_AddPlaylist() {
        assertEquals(true, db.addPlaylist(playlist));
    }

    @Test
    public void testF_AddSongToPlaylist() {
        assertEquals(true, db.addSongToPlaylist(song.getFilePath(), playlist));
    }

    @Test
    public void testG_GetPlaylistSongs() {
        Object[][] playlistSongs = db.getPlaylistSongs(playlist);
        assertEquals(true, playlistSongs.length > 0);
    }

    @Test
    public void testH_DeleteSong() {
        assertEquals(true, db.deleteSong(song.getFilePath()));
    }

    @Test
    public void testI_DeletePlaylist() {
        assertEquals(true, db.deletePlaylist(playlist));
    }

    @Test
    public void testJ_DropTables() {
        assertEquals(true, db.dropTable(ShiBase.PLAYLIST_SONG_TABLE));
        assertEquals(true, db.dropTable(ShiBase.SONG_TABLE));
        assertEquals(true, db.dropTable(ShiBase.PLAYLIST_TABLE));
    }

    @Test
    public void testK_Close() {
        assertEquals(true, db.close());
    }
}
