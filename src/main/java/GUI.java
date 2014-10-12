import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by michael on 10/3/2014.
 */
public class GUI extends JFrame{

    private static String RESOURCES_DIR = System.getProperty("user.dir") + "/resources/";

    private static String PLAY_RESOURCE = "play.png";
    private static String PAUSE_RESOURCE = "pause.png";
    private static String STOP_RESOURCE = "stop.png";
    private static String PREVIOUS_RESOURCE = "previous.png";
    private static String NEXT_RESOURCE = "next.png";

    private static BufferedImage playResource;
    private static BufferedImage pauseResource;
    private static BufferedImage stopResource;
    private static BufferedImage previousResource;
    private static BufferedImage nextResource;

    private static ImageIcon stopIcon;
    private static ImageIcon pauseIcon;
    private static ImageIcon playIcon;
    private static ImageIcon previousIcon;
    private static ImageIcon nextIcon;

    private JButton togglePlayButton;
    private JButton stopButton;
    private JButton previousButton;
    private JButton nextButton;

    private ShiBase db;
    private MusicPlayer player;

    private JFrame shiTunesFrame;
    private JTable libTable;
    private JPanel panel1;
    private JMenuBar menuBar;

    private int selectedSongIndex;

    public GUI() {
        // Initialize Database
        db = new ShiBase();
        db.connect();
        loadDummyData();

        // Initialize Music Player
        player = new MusicPlayer();

        // GUI initialization
        shiTunesFrame = new JFrame();
        shiTunesFrame.setTitle("ShiTunes");
        shiTunesFrame.setSize(600, 400);
        shiTunesFrame.setLocationRelativeTo(null);
        shiTunesFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set and add menu bar
        menuBar = new JMenuBar();
        shiTunesFrame.setJMenuBar(menuBar);
        menuBar.add(createFileMenu());

        // Build library table as ScrollPane and add to panel1
        panel1 = new JPanel();
        libTable = new JTable(db.getAllSongs(), ShiBase.COLUMNS);
        JScrollPane scrollPane = new JScrollPane(libTable);
        libTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        libTable.setFillsViewportHeight(true);
        panel1.add(scrollPane);

        // Setup Library table
        libTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                // Store selected song row index for use in skipping and getting selected song
                selectedSongIndex = libTable.getSelectedRow();
            }
        });

        // Add buttons to panel1
        try {
            // Initialize resources
            playResource = ImageIO.read(new File(RESOURCES_DIR + PLAY_RESOURCE));
            pauseResource = ImageIO.read(new File(RESOURCES_DIR + PAUSE_RESOURCE));
            stopResource = ImageIO.read(new File(RESOURCES_DIR + STOP_RESOURCE));
            previousResource = ImageIO.read(new File(RESOURCES_DIR + PREVIOUS_RESOURCE));
            nextResource = ImageIO.read(new File(RESOURCES_DIR + NEXT_RESOURCE));
            stopIcon = new ImageIcon(stopResource);
            pauseIcon = new ImageIcon(pauseResource);
            playIcon = new ImageIcon(playResource);
            previousIcon = new ImageIcon(previousResource);
            nextIcon = new ImageIcon(nextResource);

            // Initialize buttons (toggle play/pause, stop, previous, next)
            // Setting icon during intialization
            togglePlayButton = new JButton(playIcon);
            stopButton = new JButton(stopIcon);
            previousButton = new JButton(previousIcon);
            nextButton = new JButton(nextIcon);

            // Set action listener for play/pause toggle button
            togglePlayButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(getSelectedSong() != null && !getSelectedSong().isEmpty()) {
                        if(player.playing) {
                            // pause: toggle icon, pause song
                            togglePlayButton.setIcon(playIcon);
                            player.pause();
                        } else if(player.paused) {
                            // resume: toggle icon, resume song
                            togglePlayButton.setIcon(pauseIcon);
                            player.resume();
                        } else if(player.stopped){
                            // play: toggle icon, play song
                            togglePlayButton.setIcon(pauseIcon);
                            player.play(getSelectedSong());
                        }
                    } else {
                        // do nothing, there is no selected song
                    }
                }
            });

            // Set action listener for stop button
            stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    player.stop();
                    togglePlayButton.setIcon(playIcon);
                }
            });

            // Set action listener for previous button
            previousButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(selectedSongIndex == 0) {
                        // at top of libTable, do nothing
                    } else {
                        // stop current song
                        // decriment selectedSongIndex
                        // play previous song
                        player.stop();
                        selectedSongIndex--;
                        player.play(getSelectedSong());
                    }
                }
            });

            // Set action listener for next button
            nextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(selectedSongIndex == libTable.getRowCount() - 1) {
                        // at end of libTable, do nothing
                    } else {
                        // stop current song
                        // decriment selectedSongIndex
                        // play next song
                        player.stop();
                        selectedSongIndex++;
                        player.play(getSelectedSong());
                    }
                }
            });

            // Add play/pause toggle and stop buttons to panel1
            panel1.add(previousButton);
            panel1.add(togglePlayButton);
            panel1.add(stopButton);
            panel1.add(nextButton);
        } catch (IOException e) {
            // IOException thrown while reading resource files
            e.printStackTrace();
        }

        shiTunesFrame.setContentPane(panel1);
        shiTunesFrame.pack();
        shiTunesFrame.setLocationByPlatform(true);
    }

    public void displayGUI()
    {
        shiTunesFrame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        ActionListener listener = new ExitItemListener();
        exitItem.addActionListener(listener);
        menu.add(exitItem);
        return menu;
    }

    private String getSelectedSong() {
        return libTable.getValueAt(selectedSongIndex, 5).toString();
    }

    class ExitItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            db.close();
            System.exit(0);
        }
    }

    /* TODO: Delete this in production code */
    public void loadDummyData() {
        String music_dir = System.getProperty("user.dir") + "/mp3/";
        Song song = new Song(music_dir + "1.mp3");
        db.insertSong(song);
        song = new Song(music_dir + "2.mp3");
        db.insertSong(song);
        song = new Song(music_dir + "3.mp3");
        db.insertSong(song);
        song = new Song(music_dir + "4.mp3");
        db.insertSong(song);
    }

}


