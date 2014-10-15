import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import java.io.File;
import java.util.Map;

/**
 * MusicPlayer class represents a persistent MusicPlayer object
 * This class handles all operations related to music playing
 *
 * @author shiTunes inc.
 */
public class MusicPlayer implements BasicPlayerListener {

    /*
     * Indicates the player state
     * <p>
     * State Codes:
     * <p>
     * 0: OPENING
     * 1: OPENED
     * 2: PLAYING
     * 3: STOPPED
     * 4: PAUSED
     * 5: RESUMED
     *
     */
    private int state;
    /*
     * Stores the filepath of the
     * song currently loaded to the player
     */
    private String currentSong;
    /*
     * Stores the index of the
     * song currently loaded to the player
     */
    private int currentSongIndex;
    /*
     * Stores the filepath of the
     * currently selected song in the library
     */
    private String selectedSong;
    /*
     * Stores the library index of the
     * currently selected song in the library
     */
    private int selectedSongIndex;
    private BasicPlayer player;
    private BasicController controller;

    /**
     * MusicPlayer default constructor, instantiates the persistent BasicPlayer object
     *
     */
    public MusicPlayer() {
        player = new BasicPlayer();
        controller = (BasicController) player;
        player.addBasicPlayerListener(this);
    }

    /**
     * Plays the selected song
     *
     * @return true if song plays successfully
     */
    public boolean play() {
        try {
            controller.open(new File(getCurrentSong()));
            if (state == 2 || state == 5 || state == 4) {
                // player.state == playing/resumed/paused
                // stop player
                controller.stop();
            }
            // play loaded song
            controller.play();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Resumes a previously paused song
     *
     * @return true if song is resumed successfully
     */
    public boolean resume() {
        try {
            controller.resume();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Pauses the currently playing song
     *
     * @return true if the song is paused successfully
     */
    public boolean pause() {
        try {
            if(state == 2 || state == 5) {
                // player.state == playing/resumed
                controller.pause();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Stops the currently playing song
     *
     * @return true if song stopped successfully
     */
    public boolean stop() {
        try {
            controller.stop();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets the selected songs file path
     *
     * @return the selected song file path
     */
    public String getSelectedSong() {
        return selectedSong;
    }

    /**
     * Sets the selected songs file path
     *
     * @param filePath the selected song file path
     */
    public void setSelectedSong(String filePath) {
        selectedSong = filePath;
    }

    /**
     * Gets the selected songs index
     *
     * @return the selected song index
     */
    public int getSelectedSongIndex() {
        return selectedSongIndex;
    }

    /**
     * Sets the selected songs index
     *
     * @param index the selected song index
     */
    public void setSelectedSongIndex(int index) {
        selectedSongIndex = index;
    }

    /**
     * Gets the currently loaded song filename
     *
     * @return the currently loaded song's filename
     */
    public String getCurrentSong() {
        return currentSong;
    }

    /**
     * Sets the currently loaded song filename
     *
     * @param currentSong the currently loaded song (filename) to set
     */
    public void setCurrentSong(String currentSong) {
        this.currentSong = currentSong;
    }

    /**
     * Gets the currently loaded songs index
     *
     * @return the currently loaded song index
     */
    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    /**
     * Sets the currently loaded songs index
     *
     * @param index the currently loaded song index
     */
    public void setCurrentSongIndex(int index) {
        currentSongIndex = index;
    }

    /**
     * Open callback, stream is ready to play.
     *
     * properties map includes audio format dependant features such as
     * bitrate, duration, frequency, channels, number of frames, vbr flag, ...
     *
     * @param stream could be File, URL or InputStream
     * @param properties audio stream properties.
     */
    public void opened(Object stream, Map properties)
    {
        // Pay attention to properties. It's useful to get duration,
        // bitrate, channels, even tag such as ID3v2.
        // System.out.println("opened : "+properties.toString());
    }

    /**
     * Progress callback while playing.
     *
     * This method is called severals time per seconds while playing.
     * properties map includes audio format features such as
     * instant bitrate, microseconds position, current frame number, ...
     *
     * @param bytesread from encoded stream.
     * @param microseconds elapsed (<b>reseted after a seek !</b>).
     * @param pcmdata PCM samples.
     * @param properties audio stream parameters.
     */
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties)
    {
        // Pay attention to properties. It depends on underlying JavaSound SPI
        // MP3SPI provides mp3.equalizer.
        // System.out.println("progress : "+properties.toString());
    }

    /**
     * Notification callback for basicplayer events such as opened
     * <p>
     * States Codes:
     * <p>
     * 0: OPENING
     * 1: OPENED
     * 2: PLAYING
     * 3: STOPPED
     * 4: PAUSED
     * 5: RESUMED
     *
     *
     * @param event the basicplayer event (OPENED, PAUSED, PLAYING, SEEKING...)
     */
    public void stateUpdated(BasicPlayerEvent event)
    {
        // Notification of BasicPlayer states (opened, playing, end of media, ...)
        state = event.getCode();
    }

    /**
     * Public accessor for player state
     * <p>
     * States Codes:
     * <p>
     * 0: OPENING
     * 1: OPENED
     * 2: PLAYING
     * 3: STOPPED
     * 4: PAUSED
     *
     * @return the players state
     */
    public int getState() {
        return state;
    }

    /**
     * A handle to the BasicPlayer, plugins may control the player through
     * the controller (play, stop, ...)
     *
     * @param controller a handle to the player
     */
    public void setController(BasicController controller)
    {
        // System.out.println("setController : " + controller);
    }
}
