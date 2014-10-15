import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Suite of tests for the Song class
 *
 */
public class SongTests {

    @Test
    public void testSongDefaultConstructor() {
        String path = getClass().getResource("/mp3/test.mp3").getPath();
        Song song = new Song(path);

        assertEquals(path, song.getFilePath());
    }
}
