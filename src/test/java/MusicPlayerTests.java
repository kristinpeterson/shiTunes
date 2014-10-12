import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Suite of tests for the MusicPlayer class
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MusicPlayerTests {

    MusicPlayer player = new MusicPlayer();
    Song song = new Song(getClass().getResource("/mp3/test.mp3").getPath());

    @Test
    public void testA_Play() {
        player.play(song.getFilePath());
    }

    @Test
    public void testB_Pause() {
        player.pause();
    }

    @Test
    public void testB_Resume() {
        player.resume();
    }

    @Test
    public void testB_Stop() {
        player.stop();
    }
}
