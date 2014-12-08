import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import java.io.IOException;

/**
 * Song class represents a song object
 *
 * @author shiTunes inc.
 */
public class Song {

    private String filePath;
    private String artist;
    private String title;
    private String album;
    private String year;
    private String genre;
    private String comment;

    /**
     * Song constructor takes the song file path and utilizes the
     * mp3agic library to grab the song information from it's ID3v1
     * or ID3v2 tag
     *
     * @param filePath the absolute path to the song file
     */
    public Song(String filePath) {
        this.filePath = filePath;
        try {
            Mp3File mp3file = new Mp3File(filePath);
            if (mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                this.artist = id3v1Tag.getArtist();
                this.title = id3v1Tag.getTitle();
                this.album = id3v1Tag.getAlbum();
                this.year = id3v1Tag.getYear();
                this.genre = id3v1Tag.getGenreDescription();
                this.comment = id3v1Tag.getComment();
            } else if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                this.artist = id3v2Tag.getArtist();
                this.title = id3v2Tag.getTitle();
                this.album = id3v2Tag.getAlbum();
                this.year = id3v2Tag.getYear();
                this.genre = id3v2Tag.getGenreDescription();
                this.comment = id3v2Tag.getComment();
            }
        } catch (IOException ioe) {
            System.out.println("Song.java: IOException occurred");
        } catch (Exception e) {
            System.out.println("Song.java: An exception occurred while getting Song tag information");
        }
    }

    /**
     * Returns the Song file path
     *
     * @return the Song file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the Song artist
     *
     * @return the Song artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Returns the Song title
     *
     * @return the Song title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the Song album
     *
     * @return the Song album
     */
    public String getAlbum() {
        return album;
    }

    /**
     * Returns the Song year
     *
     * @return the Song year
     */
    public String getYear() {
        return year;
    }

    /**
     * Returns the Song genre
     *
     * @return the Song genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Returns the Song comment
     *
     * @return the Song comment
     */
    public String getComment() { return comment; }

}

