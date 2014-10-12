import javazoom.jlgui.basicplayer.BasicPlayer;

import java.io.File;

/**
 * MusicPlayer class represents a persistent MusicPlayer object
 * This class handles all operations related to music playing
 *
 * @author shiTunes inc.
 */
public class MusicPlayer {

    /**
     * Indicates whether the player is paused
     */
    public boolean paused = false;
    /**
     * Indicates whether the player is playing
     */
    public boolean playing = false;
    /**
     * Indicates whether the player is stopped
     */
    public boolean stopped = true;

    private BasicPlayer player;

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
     * @param song the file path of the song to play
     */
    public void play(String song) {
        try {
            player.open(new File(song));
            player.play();
            playing = true;
            paused = false;
            stopped = false;
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
            playing = true;
            paused = false;
            stopped = false;
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
            playing = false;
            paused = true;
            stopped = false;
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
            stopped = true;
            playing = false;
            paused = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
