import javazoom.jlgui.basicplayer.BasicPlayer;

import java.io.File;

/**
 * MusicPlayer class represents a persistent MusicPlayer object
 * This class handles all operations related to music playing
 *
 */
public class MusicPlayer {

    BasicPlayer player;

    /**
     * MusicPlayer constructor, instantiates the persistent BasicPlayer object
     *
     */
    public MusicPlayer() {
        player = new BasicPlayer();
    }

    /**
     * Plays the given song
     *
     * @param song the song to play
     */
    public void play(Song song) {
        try {
            player.open(new File(song.getFilePath()));
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Resumes a previously paused song
     *
     */
    public void resume() {
        try {
            player.resume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Pauses the currently playing song
     *
     */
    public void pause() {
        try {
            player.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the currently playing song
     *
     */
    public void stop() {
        try {
            player.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
