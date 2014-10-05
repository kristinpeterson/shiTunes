/**
 *
 * This will be the main class of the shiTunes application
 * handling initialization, etc
 *
 */
public class ShiTunes {
    public static void main(String[] args) {
        ShiBase db = new ShiBase();
        String music_dir = System.getProperty("user.dir") + "/mp3/";
        String music_file = "test.mp3";
        System.out.println(music_dir + music_file);
        Song song = new Song(music_dir + music_file);


        db.connect();
        System.out.println("testing isConnected() : " + db.isConnected());
        System.out.println("testing createTable() : " + db.createTable());
        System.out.println("testing insertSong(song) : " + db.insertSong(song));
        System.out.println("testing deleteSong(song) : " + db.deleteSong(song));
        System.out.println("testing dropTable() : " + db.dropTable());

    }
}
