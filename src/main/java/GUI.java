import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The GUI class builds the shiTunes Graphical User Interface
 *
 * @author shiTunes inc.
 */
public class GUI extends JFrame{

    private JFrame shiTunesFrame;
    private JPanel controlPanel;
    private JPopupMenu popup;
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
    public GUI() {

        shiTunesFrame = new JFrame();
        shiTunesFrame.setTitle("shiTunes");
        shiTunesFrame.setMinimumSize(new Dimension(900, 600));
        shiTunesFrame.setLocationRelativeTo(null);
        shiTunesFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        JPanel mainLibraryPanel = new JPanel();
        mainLibraryPanel.setLayout(new BoxLayout(mainLibraryPanel, BoxLayout.Y_AXIS));
        controlPanel = new JPanel();
        JPanel libraryPanel = new JPanel(new GridLayout(1, 1));
        JPanel playlistPanel = new JPanel(new GridLayout(2,1));
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());

        //Popup menu initialization
        createPopupMenu();

        // Instantiate scroll pane for library
        JScrollPane scrollPane = new JScrollPane(ShiTunes.library.getTable());
        ShiTunes.library.getTable().setPreferredScrollableViewportSize(new Dimension(500, 200));
        ShiTunes.library.getTable().setFillsViewportHeight(true);

        // Setup Library table listener for selected row(s)
        ShiTunes.library.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                // set the currently selected row as the selected song
                ShiTunes.library.setSelectedSong(ShiTunes.library.getTable().getSelectedRow());
                // set the selection range (min == max if only one selected)
                int min = ((DefaultListSelectionModel)event.getSource()).getMinSelectionIndex();
                int max = ((DefaultListSelectionModel)event.getSource()).getMaxSelectionIndex();
                ShiTunes.library.setSelectedSongRange(min, max);
            }
        });

        // Set double click listener to play selected song.
        ShiTunes.library.getTable().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    // set selected song as currently loaded song for the player
                    ShiTunes.player.setLoadedSong(ShiTunes.library.getTable().getSelectedRow());
                    ShiTunes.player.play();
                }
            }
        });

        // Set drop target on scroll table
        // enabling drag and drop of files into library
        scrollPane.setDropTarget(new DropTarget(){
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Transferable t = dtde.getTransferable();
                List fileList = null;
                String filepath;
                try {
                    fileList = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                    for(int i = 0; i < fileList.size(); i++) {
                        Song song = new Song(fileList.get(i).toString());
                        ShiTunes.library.addSongToLibrary(song);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //creates Library tree and Playlist tree to the side column of shiTunes
        DefaultMutableTreeNode top = new DefaultMutableTreeNode ("Library");
        DefaultMutableTreeNode playlistTreeNode = new DefaultMutableTreeNode ("Playlists");
        JTree playlistTree = new JTree(playlistTreeNode);
        JTree libraryTree = new JTree(top);
        playlistPanel.add(libraryTree);
        playlistPanel.add(playlistTree);
        //playlistPanel.add(scrollPane(playlistTree));

        // Add scroll pane (library table) to library panel
        libraryPanel.add(scrollPane);

        // Add UI components (buttons)
        addUIComponents();

        // Build main panel
        mainLibraryPanel.add(controlPanel);
        mainLibraryPanel.add(libraryPanel);
        mainPanel.add(mainLibraryPanel);
        mainPanel.add(playlistPanel);

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
            BufferedImage playResource = ImageIO.read(getClass().getResourceAsStream("/images/play.png"));
            BufferedImage pauseResource = ImageIO.read(getClass().getResourceAsStream("/images/pause.png"));
            BufferedImage stopResource = ImageIO.read(getClass().getResourceAsStream("/images/stop.png"));
            BufferedImage previousResource = ImageIO.read(getClass().getResourceAsStream("/images/previous.png"));
            BufferedImage nextResource = ImageIO.read(getClass().getResourceAsStream("/images/next.png"));
            ImageIcon stopIcon = new ImageIcon(stopResource);
            ImageIcon pauseIcon = new ImageIcon(pauseResource);
            ImageIcon playIcon = new ImageIcon(playResource);
            ImageIcon previousIcon = new ImageIcon(previousResource);
            ImageIcon nextIcon = new ImageIcon(nextResource);

            // Initialize buttons (toggle play/pause, stop, previous, next)
            // Setting icon during intialization
            JButton playButton = new JButton(playIcon);
            JButton pauseButton = new JButton(pauseIcon);
            JButton stopButton = new JButton(stopIcon);
            JButton previousButton = new JButton(previousIcon);
            JButton nextButton = new JButton(nextIcon);

            // Initialize Volume Slider
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, ShiTunes.player.getVolume());
            slider.setMinorTickSpacing(10);
            slider.setMajorTickSpacing(20);
            slider.setPaintTicks(false);
            slider.setPaintLabels(false);
            slider.setLabelTable(slider.createStandardLabels(10));

            // Set preferred button size
            playButton.setPreferredSize(new Dimension(40, 40));
            pauseButton.setPreferredSize(new Dimension(40, 40));
            stopButton.setPreferredSize(new Dimension(40, 40));
            previousButton.setPreferredSize(new Dimension(40, 40));
            nextButton.setPreferredSize(new Dimension(40, 40));

            // Set action listeners
            playButton.addActionListener(new PlayListener());
            pauseButton.addActionListener(new PauseListener());
            stopButton.addActionListener(new StopListener());
            previousButton.addActionListener(new PreviousListener());
            nextButton.addActionListener(new NextListener());
            slider.addChangeListener(new VolumeListener());

            // Add buttons to controlPanel
            controlPanel.add(previousButton);
            controlPanel.add(playButton);
            controlPanel.add(pauseButton);
            controlPanel.add(stopButton);
            controlPanel.add(nextButton);
            controlPanel.add(slider);

            controlPanel.setMaximumSize(new Dimension(1080, 40));
        } catch (IOException e) {
            // IOException thrown while reading resource files
            e.printStackTrace();
        }
    }

    /**
     * Creates shiTunes file menu
     *
     * @return the shiTunes file menu
     */
    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem addItem = new JMenuItem("Add Song");
        JMenuItem deleteItem = new JMenuItem("Delete Song(s)");
        JMenuItem createPlaylistItem = new JMenuItem("Create Playlist");
        JMenuItem exitItem = new JMenuItem("Exit");

        ActionListener openListener = new OpenItemListener();
        ActionListener addListener = new AddItemListener();
        ActionListener deleteListener = new DeleteItemListener();
        ActionListener createPlaylistListener = new CreatePlaylistListener();
        ActionListener exitListener = new ExitItemListener();

        openItem.addActionListener(openListener);
        addItem.addActionListener(addListener);
        deleteItem.addActionListener(deleteListener);
        createPlaylistItem.addActionListener(createPlaylistListener);
        exitItem.addActionListener(exitListener);

        menu.add(openItem);
        menu.add(addItem);
        menu.add(deleteItem);
        menu.add(createPlaylistItem);
        menu.add(exitItem);
        return menu;
    }

    /**
     * Initializes popup menu for file addition/deletion
     * <p>
     * When user right clicks anywhere on JTable
     * menu items for delete and add popup.
     */
    private void createPopupMenu() {
        popup = new JPopupMenu();

        JMenuItem popupAdd = new JMenuItem("Add Song");
        JMenuItem popupDelete = new JMenuItem("Delete Song(s)");
        ActionListener addListener = new AddItemListener();
        ActionListener deleteListener = new DeleteItemListener();
        popupAdd.addActionListener(addListener);
        popupDelete.addActionListener(deleteListener);

        MouseListener popupListen = new PopupListener();
        ShiTunes.library.getTable().addMouseListener(popupListen);

        popup.add(popupAdd);
        popup.add(popupDelete);
    }

    /* ********* */
    /* Listeners */
    /* ********* */

    /**
     * A Listener for the volume slider
     */
    class VolumeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                // volume converted to double value in range [0.0, 1.0]
                // which is the range required by BasicPlayer setGain() method
                double volume = source.getValue() / 100.00;
                ShiTunes.player.adjustVolume(volume);
            }
        }
    }

    /**
     * A listener for the Previous Song action
     */
    class PreviousListener implements ActionListener {
        /**
         * If player state is currently playing/resumed
         * stop the current song, decrement the song index
         * and play the previous song
         *
         * @param e the ActionEvent object for this event
         */
        public void actionPerformed(ActionEvent e) {
            if(ShiTunes.player.getLoadedSongIndex() != 0) {
                // if not at top of library, skip previous, otherwise do nothing
                if(ShiTunes.player.getState() == 2 || ShiTunes.player.getState() == 5) {
                    // if player is currently playing/resumed
                    // stop current song
                    // decrement player.currentSongIndex
                    // play previous song
                    ShiTunes.player.stop();
                    if(ShiTunes.player.getLoadedSongIndex() > 0) {
                        ShiTunes.player.setLoadedSong(ShiTunes.player.getLoadedSongIndex() - 1);
                    }
                    ShiTunes.player.play();
                }
            }
        }
    }

    /**
     * A listener for the Play Song action
     */
    class PlayListener implements ActionListener {
        /**
         * Calls the MusicPlayer play function when event occurs
         *
         * @param e the ActionEvent object for this event
         */
        public void actionPerformed(ActionEvent e) {
            boolean selectedSongIsCurrent = ShiTunes.library.getSelectedSong().equals(ShiTunes.player.getLoadedSong());
            if (selectedSongIsCurrent && ShiTunes.player.getState() == 4) {
                // if selected song is current song on player
                // and player.state == paused
                ShiTunes.player.resume();
            } else {
                ShiTunes.player.setLoadedSong(ShiTunes.library.getTable().getSelectedRow());
                ShiTunes.player.play();
            }
        }
    }

    /**
     * A listener for the Pause Song action
     */
    class PauseListener implements ActionListener {
        /**
         * Calls the MusicPlayer pause function when event occurs
         *
         * @param e the ActionEvent object for this event
         */
        public void actionPerformed(ActionEvent e) {
            ShiTunes.player.pause();
        }
    }

    /**
     * A listener for the Stop Song action
     */
    class StopListener implements ActionListener {
        /**
         * Calls the MusicPlayer stop function when event occurs
         *
         * @param e the ActionEvent object for this event
         */
        public void actionPerformed(ActionEvent e) {
            ShiTunes.player.stop();
        }
    }

    /**
     * A listener for the Next Song action
     */
    class NextListener implements ActionListener {
        /**
         * If player state is currently playing/resumed
         * stop the current song, increment the song index
         * and play the next song
         *
         * @param e the ActionEvent object for this event
         */
        public void actionPerformed(ActionEvent e) {
             if(ShiTunes.player.getLoadedSongIndex() < ShiTunes.library.getTable().getRowCount() - 1) {
                if(ShiTunes.player.getState() == 2 || ShiTunes.player.getState() == 5) {
                    // if player is currently playing/resumed
                    // stop current song
                    // decriment player.currentSongIndex
                    // play next song
                    ShiTunes.player.stop();
                    ShiTunes.player.setLoadedSong(ShiTunes.player.getLoadedSongIndex() + 1);
                    ShiTunes.player.play();
                }
            }
        }
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
            ShiTunes.db.close();
            System.exit(0);
        }
    }

    /**
     * Open item listener for the Main Menu
     * <p>
     * When "Open" is selected from the main menu,
     * a file chooser opens and whichever song is selected
     * is added to the Music Library (temporarily) and played
     * <p>
     * Note: this is different from the "Add Song" command in that
     * the song is not added to the ShiBase database
     */
    class OpenItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            chooser.setFileFilter(filter);  //filters for mp3 files only
            //file chooser menu
            if (chooser.showDialog(shiTunesFrame, "Open Song") == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Song selectedSong = new Song(selectedFile.getPath());
                if(!ShiTunes.library.songExistsInLibraryTable(selectedSong.getFilePath())) {
                    DefaultTableModel model = (DefaultTableModel) ShiTunes.library.getTable().getModel();
                    model.addRow(new Object[]{selectedSong.getArtist(), selectedSong.getTitle(), selectedSong.getAlbum(),
                            selectedSong.getYear(), selectedSong.getGenre(), selectedSong.getFilePath()});
                    ShiTunes.player.setLoadedSong(model.getRowCount() - 1);
                    ShiTunes.player.play();
                } else {
                    // TODO: display something that tells the user the song isn't being opened
                }
            }
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
            if (chooser.showDialog(shiTunesFrame, "Add Song") == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Song selectedSong = new Song(selectedFile.getPath());
                ShiTunes.library.addSongToLibrary(selectedSong);
            }
        }
    }

    /**
     * Delete item listener for the Main Menu
     * <p>
     * When "Delete Song" is selected from the main menu
     * the selected song is deleted from the database
     * and removed from the Music Library listing
     */
    class DeleteItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // range[0] = min index of selected range
            // range[1] = max index of selected range
            int min = ShiTunes.library.getSelectedSongRange()[0];
            int max = ShiTunes.library.getSelectedSongRange()[1];

            // cycle through all selected songs and delete
            // one at a time
            // Note: starts at the bottom of the selected rows (ie. max index)
            // and works it's way up the list of selected rows
            for(int row = max; row >= min; row--) {
                String selectedSong = ShiTunes.library.getTable().getValueAt(row, 5).toString();

                // Stop player if song being deleted is the current song on the player
                if(selectedSong.equals(ShiTunes.player.getLoadedSong())) {
                    ShiTunes.player.stop();
                }

                DefaultTableModel model = (DefaultTableModel) ShiTunes.library.getTable().getModel();
                model.removeRow(row);

                //Delete song from database by using filepath as an identifier
                ShiTunes.db.deleteSong(selectedSong);
            }
        }
    }

    /**
     * Listener that creates a new Playlist
     * <p>
     * When 'Create Playlist' is selected from main menu,
     * a popup appears to allow user to name their playlist.
     * New, empty playlist is added to database.
     */
    class CreatePlaylistListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            //Display message box with a textfield for user to type into
            //addPlaylistMaster(String userInput)
            JFrame createPLFrame = new JFrame("Create New Playlist");
            String playlistName = (String)JOptionPane.showInputDialog(createPLFrame, "New playlist's name: ",
                    "Create New Playlist", JOptionPane.PLAIN_MESSAGE);
            ShiTunes.db.addPlaylist(playlistName);
        }
    }

    /**
     * Popup listener for the right click menu
     * <p>
     * Interprets right mouse click to trigger
     * showing the popup menu
     */
    class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}