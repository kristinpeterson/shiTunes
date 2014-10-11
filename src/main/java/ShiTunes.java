/**
 *
 * This will be the main class of the shiTunes application
 * handling initialization, etc
 *
 * As of right now this class has a ton of test code in it that
 * will need to be removed.
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
        Object[][] allSongs = db.getAllSongs();
        System.out.println("testing getAllSongs() : " + allSongs.toString());
        System.out.println("ALL SONGS IN DB :");
        System.out.println();
        for(int i = 0; i < allSongs.length; i++) {
            for(int j = 0; j < allSongs[i].length; j++) {
                System.out.print(allSongs[i][j].toString() + " - ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("testing deleteSong(song) : " + db.deleteSong(song));
        System.out.println("testing dropTable() : " + db.dropTable());

    }
}
