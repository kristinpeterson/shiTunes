import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The GUI class builds the shiTunes Graphical User Interface
 *
 * @author shiTunes inc.
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
    private JFileChooser chooser = new JFileChooser();
    private static FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "MP3 Files", "mp3");

    private int selectedSongIndex;

    /**
     * The GUI default constructor
     * <p>
     * Initializes and connects to the database (ShiBase),
     * Builds the shiBase GUI, including Music Library table,
     * Main Menu and buttons (previous, play/pause, stop, next)
     *
     */
    public GUI() {
        // Initialize Database
        db = new ShiBase();
        db.connect();
        // TODO: REMOVE loadDummyData() IN PRODUCTION CODE? (or keep it for demo)
        loadDummyData();

        // Initialize Music Player
        player = new MusicPlayer();

        // GUI initialization
        shiTunesFrame = new JFrame();
        shiTunesFrame.setTitle("shiTunes");
        shiTunesFrame.setSize(600, 400);
        shiTunesFrame.setLocationRelativeTo(null);
        shiTunesFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set and add menu bar
        menuBar = new JMenuBar();
        shiTunesFrame.setJMenuBar(menuBar);
        menuBar.add(createFileMenu());

        // Build library table as ScrollPane and add to panel1
        panel1 = new JPanel();
        //libTable = new JTable(db.getAllSongs(), ShiBase.COLUMNS);
        //new DefaultTableModel(db.getAllSongs(), ShiBase.COLUMNS)
        libTable = new JTable(new DefaultTableModel(db.getAllSongs(), ShiBase.MUSIC_COLUMNS));

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

        addUIComponents();
        shiTunesFrame.setContentPane(panel1);
        shiTunesFrame.pack();
        shiTunesFrame.setLocationByPlatform(true);
    }

    /**
     * Displays the shiTunes GUI
     *
     */
    public void displayGUI()
    {
        shiTunesFrame.setVisible(true);
    }

    /**
     * Adds UI Components to shiTunes GUI Panel
     *
     */
    private void addUIComponents() {
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
    }

    /**
     * Gets the currently selected song filename
     *
     * @return the filename for the currently selected song
     */
    private String getSelectedSong() {
        return libTable.getValueAt(selectedSongIndex, 5).toString();
    }

    /**
     * Creates shiTunes file menu
     *
     * @return the shiTunes file menu
     */
    public JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        JMenuItem addItem = new JMenuItem("Add Song");
        JMenuItem deleteItem = new JMenuItem("Delete Song");
        JMenuItem exitItem = new JMenuItem("Exit");

        ActionListener addListener = new AddItemListener();
        ActionListener deleteListener = new DeleteItemListener();
        ActionListener exitListener = new ExitItemListener();

        addItem.addActionListener(addListener);
        deleteItem.addActionListener(deleteListener);
        exitItem.addActionListener(exitListener);

        menu.add(addItem);
        menu.add(deleteItem);
        menu.add(exitItem);
        return menu;
    }

    /**
     * Exit item listener for the Main Menu.
     * <p>
     * When "Exit" is selected from the main menu the database connection
     * is closed and the shiTunes program exits gracefully
     *
     */
    class ExitItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            db.close();
            System.exit(0);
        }
    }

    /**
     * Add item listener for the Main Menu
     * <p>
     * When "Add Song" is selected from the main menu
     * it is added to the database, if there is an error
     * when adding the song to the database (such as the song
     * already existing)
     */
    class AddItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            chooser.setFileFilter(filter);  //filters for mp3 files only
            //file chooser menu
            if (chooser.showOpenDialog(shiTunesFrame) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Song selectedSong = new Song(selectedFile.getPath());
                if(db.insertSong(selectedSong)) {
                    // insert song was successful
                    // Add row to JTable
                    DefaultTableModel model = (DefaultTableModel) libTable.getModel();
                    model.addRow(new Object[]{selectedSong.getArtist(), selectedSong.getTitle(), selectedSong.getAlbum(),
                            selectedSong.getYear(), selectedSong.getGenre(), selectedSong.getFilePath()});
                } else {
                    // TODO: display something that tells the user the song isn't being added
                }
            }
        }
    }

    /**
     * Delete item listener for the Main Menu
     * <p>
     * When "Delete Song" is selected from the main menu
     * the selected song is deleted from the database
     * and removed from the Music Library listing
     *
     */
    class DeleteItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            //Delete row from JTable
            int row = libTable.getSelectedRow();    //row number to be deleted
            DefaultTableModel model = (DefaultTableModel) libTable.getModel();
            String selected = model.getValueAt(row, 5).toString();  // filepath of song @row
            model.removeRow(row);

            //Delete song from database by using filepath as an identifier
            System.out.println(selected);
            System.out.println("delete song?" + db.deleteSong(selected));
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


