import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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
import java.util.ArrayList;

/**
 * The GUI class builds the shiTunes Graphical User Interface
 *
 * @author shiTunes inc.
 */
public class Window extends JFrame {

    // Window type
    public static int MAIN = 0;
    public static int PLAYLIST = 1;

    private int windowType;
    private JFrame windowFrame;
    private MusicTable musicTable;
    private JPopupMenu musicTablePopupMenu;
    private JMenu addSongToPlaylistSubMenu;
    private DefaultMutableTreeNode playlistNode;
    private String selectedPlaylist;
    private JScrollPane musicTableScrollPane;
    private MusicTablePopupListener musicTablePopupListener = new MusicTablePopupListener();

    /**
     * The Window default constructor
     * <p>
     * Builds the shiTunes main application window
     */
    public Window() {

        // Set this Window instance's type to Window.MAIN
        this.windowType = Window.MAIN;

        // Set this Window instance's table
        this.musicTable = new MusicTable();

        buildWindowLayout("shiTunes");
    }

    /**
     * The Window overloaded constructor
     * <p>
     * Builds a shiTunes playlist window
     *
     * @param playlistName the name of the Playlist
     */
    public Window(String playlistName) {

        // Set this Window instance's type to Window.PLAYLIST
        this.windowType = Window.PLAYLIST;

        // Set this Window instance's table
        this.musicTable = new MusicTable();

        buildWindowLayout(playlistName);
    }

    /**
     * Displays the window
     *
     */
    public void display() {
        windowFrame.setVisible(true);
    }

    /**
     * Builds the Window's layout based on it's type
     *
     * @param windowTitle the title of the window
     */
    private void buildWindowLayout(String windowTitle) {
        // Create outer shiTunes frame and set various parameters
        windowFrame = new JFrame();
        windowFrame.setTitle(windowTitle);
        windowFrame.setMinimumSize(new Dimension(900, 600));
        windowFrame.setLocationRelativeTo(null);
        windowFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Create the main panel that resides within the windowFrame
        // Layout: BoxLayout, X_AXIS
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        // Create the controlTablePanel that will reside within the mainPanel
        // Layout: BoxLayout, Y_AXIS
        JPanel controlTablePanel = new JPanel();
        controlTablePanel.setLayout(new BoxLayout(controlTablePanel, BoxLayout.Y_AXIS));

        // Create menuBar and add File menu
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());

        // Build the music table
        buildMusicTable();

        // Instantiate scroll pane for table
        musicTableScrollPane = new JScrollPane(musicTable.getTable());

        // Build main panel
        controlTablePanel.add(getControlPanel());
        controlTablePanel.add(musicTableScrollPane);
        if(windowType == Window.MAIN) {
            mainPanel.add(getPlaylistPanel());
        }
        mainPanel.add(controlTablePanel);

        // Add all GUI components to shiTunes application frame
        windowFrame.setJMenuBar(menuBar);
        windowFrame.setContentPane(mainPanel);
        windowFrame.pack();
        windowFrame.setLocationByPlatform(true);
    }

    /**
     * Builds the music table:
     * <ul>
     * <li>Sets various viewport parameters</li>
     * <li>Assigns listeners</li>
     * </ul>
     *
     */
    private void buildMusicTable() {
        musicTable.getTable().setPreferredScrollableViewportSize(new Dimension(500, 200));
        musicTable.getTable().setFillsViewportHeight(true);

        /* Add listeners */

        // Add Library table listener for selected row(s)
        musicTable.getTable().getSelectionModel().addListSelectionListener(new TableItemSelectionListener());

        // Create right-click popup menu and set popup listener
        createMusicTablePopupMenu();
        musicTable.getTable().addMouseListener(musicTablePopupListener);

        // Add double click listener to play selected song.
        musicTable.getTable().addMouseListener(new DoubleClickListener());

        // Add drop target on table
        // enabling drag and drop of files into library
        musicTable.getTable().setDropTarget(new AddToTableDropTarget());
    }

    /**
     * Returns the playlist panel
     *
     * @return the playlist panel containing the Library and Playlist branches
     */
    private JScrollPane getPlaylistPanel() {
        // Create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        // Create library and playlist nodes
        DefaultMutableTreeNode libraryNode = new DefaultMutableTreeNode ("Library");
        playlistNode = new DefaultMutableTreeNode ("Playlists");

        updatePlaylistNode();

        // Add library and playlist nodes to the root
        root.add(libraryNode);
        root.add(playlistNode);

        // Create playlist panel tree
        JTree playlistPanelTree = new JTree(root);

        // Make the root node invisible
        playlistPanelTree.setRootVisible(false);

        playlistPanelTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                String selection = e.getPath().getLastPathComponent().toString();
                // If selection is not Playlist
                // ie. "Library" or a playlist name was selected
                if(!selection.equals("Playlists")) {
                    // Update the table model and fire change
                    musicTable.updateTableModel(selection);

                    /*
                     * This block of code is a hackey way of making the popup menu
                     * not display in the playlist table.  The popup menu within the
                     * playlist table is not a requirement, and the options in that menu
                     * do not work properly from within the playlist table, so for now
                     * whenever the user switches to a playlist table the popup menu is removed
                     * and when they switch back to the library table the popup menu is added back
                     */
                    // Determine if popup listener is present
                    boolean listenerFound = false;
                    MouseListener[] mouseListeners = musicTable.getTable().getMouseListeners();
                    for(MouseListener mouseListener : mouseListeners) {
                        if(mouseListener.getClass().equals(musicTablePopupListener.getClass())) {
                            listenerFound = true;
                        }
                    }
                    // Reinstate the popup listener if it was removed previously
                    if(!listenerFound){
                        musicTable.getTable().addMouseListener(musicTablePopupListener);
                    }

                    // If Library is not the selected item, a playlist name was selected
                    // Remove popup menu listener and set selected playlist
                    if(!selection.equals("Library")) {
                        // remove the popup menu listener for playlist table
                        musicTable.getTable().removeMouseListener(musicTablePopupListener);
                        selectedPlaylist = selection;
                    }

                    // Repaint the music table scroll pane
                    musicTableScrollPane.repaint();
                }
            }
        });

        // Set Icons
        try {
            BufferedImage musicResource = ImageIO.read(getClass().getResourceAsStream("/images/music.png"));
            BufferedImage plusResource = ImageIO.read(getClass().getResourceAsStream("/images/plus.png"));
            BufferedImage minusResource = ImageIO.read(getClass().getResourceAsStream("/images/minus.png"));
            ImageIcon music = new ImageIcon(musicResource);
            ImageIcon plus = new ImageIcon(plusResource);
            ImageIcon minus = new ImageIcon(minusResource);
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setOpenIcon(minus);
            renderer.setClosedIcon(plus);
            renderer.setLeafIcon(music);
            playlistPanelTree.setCellRenderer(renderer);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return new JScrollPane(playlistPanelTree);
    }

    /**
     * Updates the playlist node in the playlist panel tree
     * with the most up to date list of playlist names from the database
     *
     */
    private void updatePlaylistNode(){
        ArrayList<String> playlistNames = ShiTunes.db.getPlaylistNames();

        playlistNode.removeAllChildren();

        for(String playlistName : playlistNames) {
            DefaultMutableTreeNode playlist = new DefaultMutableTreeNode(playlistName);
            playlistNode.add(playlist);
        }
    }

    /**
     * Adds UI Components to shiTunes Window
     *
     * @return the control JPanel
     */
    private JPanel getControlPanel() {
        // Instantiate the controlPanel (Buttons and Volume Slider)
        JPanel controlPanel = new JPanel();
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
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
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
        return controlPanel;
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

        addItem.addActionListener(new AddSongListener());
        deleteItem.addActionListener(new DeleteSongListener());
        openItem.addActionListener(new OpenItemListener());
        createPlaylistItem.addActionListener(new CreatePlaylistListener());
        exitItem.addActionListener(new ExitItemListener());

        menu.add(openItem);
        menu.add(addItem);
        menu.add(deleteItem);
        menu.add(createPlaylistItem);
        menu.add(exitItem);
        return menu;
    }

    /**
     * Initializes popup menu for the music table
     * <p>
     * When user right clicks anywhere on music table
     * a popup menu is displayed.
     *
     */
    private void createMusicTablePopupMenu() {
        musicTablePopupMenu = new JPopupMenu();
        JMenuItem addMenuItem = new JMenuItem("Add Song");
        JMenuItem deleteMenuItem = new JMenuItem("Delete Song(s)");
        addSongToPlaylistSubMenu = new JMenu("Add Song to Playlist");

        addMenuItem.addActionListener(new AddSongListener());
        deleteMenuItem.addActionListener(new DeleteSongListener());

        musicTablePopupMenu.add(addMenuItem);
        musicTablePopupMenu.add(deleteMenuItem);
        musicTablePopupMenu.add(addSongToPlaylistSubMenu);
        updateAddPlaylistSubMenu();
    }

    /**
     * Updates the playlist sub menu in the music table's popup menu:
     * <ul>
     *     <li>Gets an updated list of playlist names from database</li>
     *     <li>Removes all items from the playlist sub menu</li>
     *     <li>Repopulate the playlist sub menu</li>
     *     <li>Repaint the music table popup menu (in which the playlist sub menu resides</li>
     * </ul>
     *
     */
    private void updateAddPlaylistSubMenu() {
        // Get updated list of playlist names from database
        ArrayList<String> playlistNames = ShiTunes.db.getPlaylistNames();

        // Remove all items from music table popup menu - playlist sub menu
        addSongToPlaylistSubMenu.removeAll();

        // Repopulate music table popup menu - playlist sub menu
        for (String playlistName : playlistNames) {
            JMenuItem item = new JMenuItem(playlistName);
            item.addActionListener(new AddSongToPlaylistListener(playlistName));
            addSongToPlaylistSubMenu.add(item);
        }

        // Repaint the popup menu
        musicTablePopupMenu.repaint();
    }

    /* ********************* */
    /* Music Table Listeners */
    /* ********************* */

    /**
     * Handles clicks on items within the music table
     *
     */
    class TableItemSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent event) {
            // set the currently selected row as the selected song
            musicTable.setSelectedSong(musicTable.getTable().getSelectedRow());
            // set the selection range (min == max if only one selected)
            int min = ((DefaultListSelectionModel)event.getSource()).getMinSelectionIndex();
            int max = ((DefaultListSelectionModel)event.getSource()).getMaxSelectionIndex();
            musicTable.setSelectedSongRange(min, max);
        }
    }

    /**
     * Double Click Listener for the music table
     * Plays the song that is double clicked.
     */
    class DoubleClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent me) {
            if (me.getClickCount() == 2) {
                // set selected song as currently loaded song for the player
                int index = musicTable.getTable().getSelectedRow();
                ShiTunes.player.setLoadedSong(index,
                        musicTable.getTable().getValueAt(index, 5).toString());
                ShiTunes.player.play();
            }
        }
    }

    /**
     * Popup listener for the right click menu
     * <p>
     * Interprets right mouse click to trigger
     * showing the popup menu
     */
    class MusicTablePopupListener extends MouseAdapter {
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
                musicTablePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * Drop target listener on music table panel
     * when song(s) are dragged from the computer's
     * file browser into the music table they are added to the
     * table
     *
     * Conditions:
     * <ul>
     * <li>If this is the Library table:  add songs to table & database</li>
     * <li>If this is a Playlist table: add songs to table & playlist (& database if not already)</li>
     * </ul>
     */
    class AddToTableDropTarget extends DropTarget {
        @Override
        public synchronized void drop(DropTargetDropEvent dtde) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable t = dtde.getTransferable();
            java.util.List fileList;
            try {
                fileList = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
                for(Object file : fileList) {
                    Song song = new Song(file.toString());

                    if(windowType == Window.MAIN && musicTable.type == MusicTable.LIBRARY) {
                        // If this is the main application window & the music table == library
                        // Only add song to library table if it is not already present in db
                        if (ShiTunes.db.insertSong(song)) {
                            // if song successfully added to database
                            // add song to music library table
                            musicTable.addSongToTable(song);
                        }
                    } else if(windowType == Window.MAIN && musicTable.type == MusicTable.PLAYLIST){
                        // If this is the main application window & the music table == playlist
                        // Try to add song to db (if already in db it won't be added)
                        ShiTunes.db.insertSong(song);
                        // Add song to the playlist
                        ShiTunes.db.addSongToPlaylist(song.getFilePath(), selectedPlaylist);
                        // Add song to playlist table
                        musicTable.addSongToTable(song);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* ****************** */
    /* Playlist Listeners */
    /* ****************** */

    /**
     * Listener that creates a new Playlist
     * <p>
     * When 'Create Playlist' is selected from main menu,
     * a popup appears to allow user to name their playlist.
     * New, empty playlist is added to database.
     * Window is refreshed to reflect changes.
     */
    class CreatePlaylistListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // Display message box with a textfield for user to type into
            JFrame createPLFrame = new JFrame("Create New Playlist");
            String playlistName = (String) JOptionPane.showInputDialog(createPLFrame, "New playlist's name: ",
                    "Create New Playlist", JOptionPane.PLAIN_MESSAGE);
            ShiTunes.db.addPlaylist(playlistName);

            //refresh GUI popupmenu playlist sub menu
            updateAddPlaylistSubMenu();
        }
    }

    /**
     * Listener that deletes an existing Playlist
     * <p>
     * When 'Delete Playlist' is selected from highlighted side panel,
     * a popup appears to confirm deletion.
     * Window is refreshed to reflect changes.
     *
     */
    class DeletePlaylistListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFrame confirmDeleteFrame = new JFrame("Delete Playlist");
            int answer = JOptionPane.showConfirmDialog(confirmDeleteFrame,
                    "Are you sure you want to delete this playlist?");
            if (answer == JOptionPane.YES_OPTION) {
                // Delete selected playlist from library
                ShiTunes.db.deletePlaylist(selectedPlaylist);

                // Refresh playlist panel tree
                updatePlaylistNode();
                // may need to add tree redraw or something

                // Refresh GUI popupmenu playlist sub menu
                updateAddPlaylistSubMenu();
            }
        }
    }

    /**
     * Add song to playlist listener.
     * <p>
     * Adds the selected song to the given playlist
     * Note: the playlist name must be passed as a parameter
     *
     */
    class AddSongToPlaylistListener implements ActionListener {
        private String playlist;

        public AddSongToPlaylistListener(String s) {
            playlist = s;
        }

        public void actionPerformed(ActionEvent event) {
            int min = musicTable.getSelectedSongRange()[0];
            int max = musicTable.getSelectedSongRange()[1];

            for(int row = max; row >= min; row--) {
                String selectedSong = musicTable.getTable().getValueAt(row, 5).toString();
                ShiTunes.db.addSongToPlaylist(selectedSong, playlist);
            }
        }
    }

    /* *********************** */
    /* Control Panel Listeners */
    /* *********************** */

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
                        int index = ShiTunes.player.getLoadedSongIndex() - 1;
                        ShiTunes.player.setLoadedSong(index,
                                musicTable.getTable().getValueAt(index, 5).toString());
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
            boolean selectedSongIsCurrent = musicTable.getSelectedSong().equals(ShiTunes.player.getLoadedSong());
            if (selectedSongIsCurrent && ShiTunes.player.getState() == 4) {
                // if selected song is current song on player
                // and player.state == paused
                ShiTunes.player.resume();
            } else {
                int index = musicTable.getTable().getSelectedRow();
                ShiTunes.player.setLoadedSong(index,
                        musicTable.getTable().getValueAt(index, 5).toString());
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
             if(ShiTunes.player.getLoadedSongIndex() < musicTable.getTable().getRowCount() - 1) {
                if(ShiTunes.player.getState() == 2 || ShiTunes.player.getState() == 5) {
                    // if player is currently playing/resumed
                    // stop current song
                    // decriment player.currentSongIndex
                    // play next song
                    ShiTunes.player.stop();
                    int index = ShiTunes.player.getLoadedSongIndex() + 1;
                    ShiTunes.player.setLoadedSong(index,
                            musicTable.getTable().getValueAt(index, 5).toString());
                    ShiTunes.player.play();
                }
            }
        }
    }

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

    /* ********************* */
    /* Main Menu Listeners */
    /* ********************* */

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
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
            chooser.setFileFilter(filter);  //filters for mp3 files only
            //file chooser menu
            if (chooser.showDialog(windowFrame, "Open Song") == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Song selectedSong = new Song(selectedFile.getPath());
                if(!musicTable.songExistsInTable(selectedSong.getFilePath())) {
                    DefaultTableModel model = (DefaultTableModel) musicTable.getTable().getModel();
                    model.addRow(new Object[]{selectedSong.getArtist(), selectedSong.getTitle(), selectedSong.getAlbum(),
                            selectedSong.getYear(), selectedSong.getGenre(), selectedSong.getFilePath()});
                    int index = model.getRowCount() - 1;
                    ShiTunes.player.setLoadedSong(index,
                            musicTable.getTable().getValueAt(index, 5).toString());
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
    class AddSongListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(filter);  //filters for mp3 files only
            //file chooser menu
            if (chooser.showDialog(windowFrame, "Add Song") == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                Song selectedSong = new Song(selectedFile.getPath());
                if(ShiTunes.db.insertSong(selectedSong)) {
                    // if successfully inserted song into database
                    musicTable.addSongToTable(selectedSong);
                }
            }
        }
    }

    /**
     * Delete item listener for the Main Menu
     * <p>
     * When "Delete Song" is selected from the main menu
     * the selected song is deleted from the database
     * and removed from the music table listing
     */
    class DeleteSongListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // range[0] = min index of selected range
            // range[1] = max index of selected range
            int min = musicTable.getSelectedSongRange()[0];
            int max = musicTable.getSelectedSongRange()[1];

            // cycle through all selected songs and delete
            // one at a time
            // Note: starts at the bottom of the selected rows (ie. max index)
            // and works it's way up the list of selected rows
            for(int row = max; row >= min; row--) {
                String selectedSong = musicTable.getTable().getValueAt(row, 5).toString();

                // Stop player if song being deleted is the current song on the player
                if(selectedSong.equals(ShiTunes.player.getLoadedSong())) {
                    ShiTunes.player.stop();
                }

                DefaultTableModel model = (DefaultTableModel) musicTable.getTable().getModel();
                model.removeRow(row);

                //Delete song from database by using filepath as an identifier
                ShiTunes.db.deleteSong(selectedSong);
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
}