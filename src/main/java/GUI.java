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

    private MusicPlayer player;
    private ShiBase db;
    private int selectedSongIndex;

    // GUI Frame and contents
    private JFrame shiTunesFrame;
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JPanel buttonPanel;
    private JPanel libraryPanel;
    private JTable libTable;

    // UI Components
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

    // File Chooser
    private JFileChooser chooser = new JFileChooser();
    private static FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");

    /**
     * The GUI default constructor
     * <p>
     * Initializes and connects to the database (ShiBase),
     * Builds the shiBase GUI, including Music Library table,
     * Main Menu and buttons (previous, play/pause, stop, next)
     *
     */
    public GUI(ShiBase db, MusicPlayer player) {
        // assign db arg to GUI db object
        this.db = db;

        // TODO: REMOVE loadDummyData() IN PRODUCTION CODE? (or keep it for demo)
        loadDummyData();

        // assign player arg to GUI player object
        this.player = player;

        // GUI initialization
        shiTunesFrame = new JFrame();
        shiTunesFrame.setTitle("shiTunes");
        shiTunesFrame.setSize(600, 400);
        shiTunesFrame.setLocationRelativeTo(null);
        shiTunesFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel initialization
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        buttonPanel = new JPanel();
        libraryPanel = new JPanel(new GridLayout(1,1));

        // Menu bar initialization
        menuBar = new JMenuBar();
        menuBar.add(createFileMenu());

        // Build library table
        // instance library table model - this prevents individual cells from being editable
        DefaultTableModel tableModel = new DefaultTableModel(db.getAllSongs(), ShiBase.MUSIC_COLUMNS) {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };

        // Instantiate library table based off table model
        libTable = new JTable(tableModel);

        // Instantiate scroll pane for library
        JScrollPane scrollPane = new JScrollPane(libTable);
        libTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        libTable.setFillsViewportHeight(true);

        // Add scroll pane (library table) to library panel
        libraryPanel.add(scrollPane);

        // Setup Library table listener
        libTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                // Store selected song row index for use in skipping and getting selected song
                selectedSongIndex = libTable.getSelectedRow();
            }
        });

        // Add UI components (buttons)
        addUIComponents();

        // Build main panel
        mainPanel.add(buttonPanel);
        mainPanel.add(libraryPanel);

        // Add all GUI components to shiTunes application frame
        shiTunesFrame.setJMenuBar(menuBar);
        shiTunesFrame.setContentPane(mainPanel);
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
            playResource = ImageIO.read(getClass().getResourceAsStream("/images/play.png"));
            pauseResource = ImageIO.read(getClass().getResourceAsStream("/images/pause.png"));
            stopResource = ImageIO.read(getClass().getResourceAsStream("/images/stop.png"));
            previousResource = ImageIO.read(getClass().getResourceAsStream("/images/previous.png"));
            nextResource = ImageIO.read(getClass().getResourceAsStream("/images/next.png"));
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

            // Set preferred button size
            togglePlayButton.setPreferredSize(new Dimension(40, 40));
            stopButton.setPreferredSize(new Dimension(40, 40));
            previousButton.setPreferredSize(new Dimension(40, 40));
            nextButton.setPreferredSize(new Dimension(40, 40));

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

            // Add buttons to buttonPanel
            buttonPanel.add(previousButton);
            buttonPanel.add(togglePlayButton);
            buttonPanel.add(stopButton);
            buttonPanel.add(nextButton);

            buttonPanel.setMaximumSize(new Dimension(1080, 40));
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
    private void loadDummyData() {
        String music_dir = getClass().getResource("/mp3/").getPath();
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


