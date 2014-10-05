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

        System.out.println("Testing play(song) : ");
        MusicPlayer player = new MusicPlayer();
        player.play(song);

        try {
            System.out.println("Sleeping 10 seconds : ");
            Thread.sleep(10000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Testng pause() : ");
        player.pause();

        try {
            System.out.println("Sleeping 5 seconds : ");
            Thread.sleep(5000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Testng resume() : ");
        player.resume();

        try {
            System.out.println("Sleeping 10 seconds : ");
            Thread.sleep(10000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Testing stop() : ");
        player.stop();

        db.connect();
        System.out.println("testing isConnected() : " + db.isConnected());
        System.out.println("testing createTable() : " + db.createTable());
        System.out.println("testing insertSong(song) : " + db.insertSong(song));
        System.out.println("testing deleteSong(song) : " + db.deleteSong(song));
        System.out.println("testing dropTable() : " + db.dropTable());

    }
}
