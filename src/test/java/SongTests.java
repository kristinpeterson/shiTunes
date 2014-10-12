import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Suite of tests for the Song class
 *
 */
public class SongTests {

    @Test
    public void testSongDefaultConstructor() {
        String music_dir = System.getProperty("user.dir") + "/mp3/";
        String music_file = "1.mp3";
        Song song = new Song(music_dir + music_file);

        assertEquals(music_dir + music_file, song.getFilePath());
    }
}
