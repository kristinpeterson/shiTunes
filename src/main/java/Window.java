import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * The GUI class builds the shiTunes Graphical User Interface
 *
 * @author shiTunes inc.
 */
public class Window
       extends JFrame
       implements BasicPlayerListener {

    // Window types
    public static int MAIN = 0;
    public static int PLAYLIST = 1;

    /*
     * Indicates the player state
     * <p>
     * State Codes:
     * <p>
     * -1: UNKNOWN
     * 0: OPENING
     * 1: OPENED
     * 2: PLAYING
     * 3: STOPPED
     * 4: PAUSED
     * 5: RESUMED
     * 6: SEEKING
     * 7: SEEKED
     * 8: EOM
     * 9: PAN
     * 10: GAIN
     *
     */
    private int playerState;
    private int loadedSongBytes;
    private int windowType;
    private JFrame windowFrame;
    private JScrollPane musicTableScrollPane;
    private MusicTable musicTable;
    private JPopupMenu musicTablePopupMenu;
    private JMenu addSongToPlaylistSubMenu;
    private MusicTablePopupListener musicTablePopupListener = new MusicTablePopupListener();
    private ColumnDisplayPopupListener columnDisplayPopupListener = new ColumnDisplayPopupListener();
    private JPopupMenu playlistPopupMenu;
    private JPopupMenu showColumnsPopupMenu;
    private JTree playlistPanelTree;
    private DefaultMutableTreeNode playlistNode;
    private String selectedPlaylist;
    private MusicPlayer player;
    private int[] showColumnList = new int[5];

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

        // Set selected playlist to "Library" (the default table in Window.MAIN)
        this.selectedPlaylist = "Library";

        // Set this Window instance's player
        player = new MusicPlayer();
        player.getPlayer().addBasicPlayerListener(this);

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

        // Set selected playlist
        this.selectedPlaylist = playlistName;

        // Set this Window instance's table
        this.musicTable = new MusicTable(playlistName);

        // Set this Window instance's player
        player = new MusicPlayer();
        player.getPlayer().addBasicPlayerListener(this);

        // Add this window to list of application windows
        ShiTunes.windows.add(this);

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

        if(windowType == Window.MAIN) {
            windowFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } else {
            if (windowType == Window.PLAYLIST) {
                windowFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                windowFrame.addWindowListener(new PlaylistWindowListener());
            }
        }

        // Create the main panel that resides within the windowFrame
        // Layout: BoxLayout, X_AXIS
        JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainPanel.setDividerLocation(150);

        // Instantiate scroll pane for table
        musicTableScrollPane = new JScrollPane(musicTable.getTable());

        // Create the controlTablePanel that will reside within the mainPanel
        // Layout: BoxLayout, Y_AXIS
        JPanel controlTablePanel = new JPanel();
        controlTablePanel.setLayout(new BoxLayout(controlTablePanel, BoxLayout.Y_AXIS));
        controlTablePanel.add(getControlPanel());
        controlTablePanel.add(musicTableScrollPane);
        controlTablePanel.setMinimumSize(new Dimension(500, 600));

        // Create menuBar and add File menu
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());

        // Build the music table
        buildMusicTable();

        //creates the right click menu for the playlists
        createPlaylistPopupMenu();

        // Build main panel
        if(windowType == Window.MAIN) {
            mainPanel.add(getPlaylistPanel());
        }
        mainPanel.add(controlTablePanel);

        // Add all GUI components to shiTunes application frame
        windowFrame.setJMenuBar(menuBar);
        windowFrame.setContentPane(mainPanel);
        /***///windowFrame.setIconImage();
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

        // Create right-click popup menu and set popup listener up JTable
        createMusicTablePopupMenu();
        musicTable.getTable().addMouseListener(musicTablePopupListener);

        //creates the right click menu for column displays
        createShowColumnsPopupMenu();
        musicTable.getTable().getTableHeader().addMouseListener(columnDisplayPopupListener);

        // Add double click listener to play selected song.
        musicTable.getTable().addMouseListener(new DoubleClickListener());

        // Add drop target on table
        // enabling drag and drop of files into table
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
        playlistPanelTree = new JTree(root);

        // Add mouse listener: manages left and right click
        playlistPanelTree.addMouseListener(new PlaylistPanelMouseListener());

        // Make the root node invisible
        playlistPanelTree.setRootVisible(false);

        // Expand playlist node (index 1)
        playlistPanelTree.expandRow(1);

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

        // Instantiate playlist panel pane to be returned
        // and set minimum dimensions
        JScrollPane playlistPanelPane = new JScrollPane(playlistPanelTree);
        playlistPanelPane.setMinimumSize(new Dimension(150, 600));

        return playlistPanelPane;
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
        if(windowType == Window.MAIN) {
            menu.add(createPlaylistItem);
        }
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
        if(windowType == Window.MAIN) {
            musicTablePopupMenu.add(addSongToPlaylistSubMenu);
        }
        updateAddPlaylistSubMenu();
    }
    /**
     * Initializes a popup menu when a user right clicks for the playlist nodes.
     *
     * When the user right clicks on a tree node, the menu appears.
     */
    private void createPlaylistPopupMenu() {
        playlistPopupMenu = new JPopupMenu();
        JMenuItem deletePlaylist = new JMenuItem("Delete Playlist");
        JMenuItem newWindow = new JMenuItem("Open Playlist in New Window");
        deletePlaylist.addActionListener(new DeletePlaylistListener());
        newWindow.addActionListener(new NewWindowListener());
        playlistPopupMenu.add(deletePlaylist);
        playlistPopupMenu.add(newWindow);
    }

    /**
     * Initializes a popup menu when a user right clicks library columns.
     * Shows checkboxes to allow user to select which columns to display.
     */
    private void createShowColumnsPopupMenu() {
        showColumnsPopupMenu = new JPopupMenu();
        final JCheckBoxMenuItem showArtist = new JCheckBoxMenuItem("Artist");
        final JCheckBoxMenuItem showAlbum = new JCheckBoxMenuItem("Album");
        final JCheckBoxMenuItem showYear = new JCheckBoxMenuItem("Year");
        final JCheckBoxMenuItem showGenre = new JCheckBoxMenuItem("Genre");
        final JCheckBoxMenuItem showComment = new JCheckBoxMenuItem("Comment");

        String readLine;

        try {
            FileReader fileReader = new FileReader("src/main/resources/txt/displayColumnsData.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            readLine = bufferedReader.readLine();
            String[] sList = readLine.split(",");
            bufferedReader.close();

            for (int i = 0; i < sList.length; i++) {
                showColumnList[i] = Integer.parseInt(sList[i]);
            }
        }
        catch (FileNotFoundException ex) {
            System.out.println("ERROR: File not found.");
        }
        catch (IOException ex) {
            System.out.println("ERROR: Error reading file.");
        }

        //Read in previous state of column display and update to reflect columns' state
        if (showColumnList[0] == 1) {showArtist.setSelected(true);}
        else {musicTable.hide(1);}
        if (showColumnList[1] == 1) {showAlbum.setSelected(true);}
        else {musicTable.hide(3);}
        if (showColumnList[2] == 1) {showYear.setSelected(true);}
        else {musicTable.hide(4);}
        if (showColumnList[3] == 1) {showGenre.setSelected(true);}
        else {musicTable.hide(5);}
        if (showColumnList[4] == 1) {showComment.setSelected(true);}
        else {musicTable.hide(7);}

        showColumnsPopupMenu.add(showArtist);
        showColumnsPopupMenu.add(showAlbum);
        showColumnsPopupMenu.add(showYear);
        showColumnsPopupMenu.add(showGenre);
        showColumnsPopupMenu.add(showComment);

        ActionListener artistCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showArtist.isSelected()) {
                    musicTable.show(1);
                    showColumnList[0] = 1;
                }
                else {
                    musicTable.hide(1);
                    showColumnList[0] = 0;
                }
                SaveColumnsDisplay();
            }
        };

        ActionListener albumCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showAlbum.isSelected()) {
                    musicTable.show(3);
                    showColumnList[1] = 1;
                }
                else {
                    musicTable.hide(3);
                    showColumnList[1] = 0;
                }
                SaveColumnsDisplay();
            }
        };

        ActionListener yearCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showYear.isSelected()) {
                    musicTable.show(4);
                    showColumnList[2] = 1;
                }
                else {
                    musicTable.hide(4);
                    showColumnList[2] = 0;
                }
                SaveColumnsDisplay();
            }
        };

        ActionListener genreCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showGenre.isSelected()) {
                    musicTable.show(5);
                    showColumnList[3] = 1;
                }
                else {
                    musicTable.hide(5);
                    showColumnList[3] = 0;
                }
                SaveColumnsDisplay();
            }
        };

        ActionListener commentCheckboxListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (showComment.isSelected()) {
                    musicTable.show(7);
                    showColumnList[4] = 1;
                }
                else {
                    musicTable.hide(7);
                    showColumnList[4] = 0;
                }
                SaveColumnsDisplay();
            }
        };

        showArtist.addActionListener(artistCheckboxListener);
        showAlbum.addActionListener(albumCheckboxListener);
        showYear.addActionListener(yearCheckboxListener);
        showGenre.addActionListener(genreCheckboxListener);
        showComment.addActionListener(commentCheckboxListener);
    }

    public void SaveColumnsDisplay() {
        try {
            FileWriter fileWriter = new FileWriter("src/main/resources/txt/displayColumnsData.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(showColumnList[0] + "," + showColumnList[1] + "," + showColumnList[2] + "," +
                    showColumnList[3] + "," + showColumnList[4]);
            bufferedWriter.close();
        }
        catch (IOException ex) {
            System.out.println("ERROR: Error writing to file.");
        }
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

        // Add terminating "Create Playlist" item to "Add Song to Playlist Menu"
        JMenuItem item = new JMenuItem("*Create Playlist*");
        item.addActionListener(new CreatePlaylistListener());
        addSongToPlaylistSubMenu.add(item);

        // Repaint the popup menu
        musicTablePopupMenu.repaint();

    }

    /* ********************* */
    /* Music Table Listeners */
    /* ********************* */

    /**
     * Double Click Listener:
     * <p>
     * Plays the song that is double clicked.
     */
    class DoubleClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent me) {
            if (me.getClickCount() == 2) {
                // set loaded song and play
                int index = musicTable.getTable().getSelectedRow();
                int songId = Integer.parseInt(musicTable.getTable().getValueAt(index, 0).toString());
                player.setLoadedSongId(songId);
                player.play(ShiTunes.db.getSongFilePath(player.getLoadedSongId()));
            }
        }
    }

    /**
     * Popup listener for the right click menu on Music Table
     *
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
     * Popup listener for the right click menu on Column Titles
     *
     */
    class ColumnDisplayPopupListener extends MouseAdapter {
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
                showColumnsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
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

                    if(musicTable.getType() == MusicTable.LIBRARY) {
                        // If this is the main application window & the music table == library
                        // Only add song to library table if it is not already present in db
                        int id = ShiTunes.db.insertSong(song);
                        if (id != -1) {
                            // if song successfully added to database
                            // add song to music library table
                            musicTable.addSongToTable(id, song);
                        }
                    } else if(musicTable.getType() == MusicTable.PLAYLIST) {

                        // If the music table == playlist
                        // Try to add song to db (if already in db it won't be added)
                        int id = ShiTunes.db.insertSong(song);

                        // Add song to the playlist
                        ShiTunes.db.addSongToPlaylist(song.getFilePath(), selectedPlaylist);

                        // Get song id if the song was already in library
                        // (ie. id in previous assignment == -1)
                        if(id == -1) {
                            id = ShiTunes.db.getSongId(song.getFilePath());
                        }

                        // Add song to playlist table
                        musicTable.addSongToTable(id, song);

                        // Notify main application window table of change
                        // if this is a separate playlist window
                        if(windowType == Window.PLAYLIST) {
                            ShiTunes.mainWindow.musicTable.updateTableModel("Library");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* ************************ */
    /* Playlist Panel Listeners */
    /* ************************ */

    class NewWindowListener implements  ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Switch back to Library table in MAIN app window
            musicTable.updateTableModel("Library");

            // Set highlighted node in playlist panel to "Library"
            playlistPanelTree.setSelectionRow(0);

            // Open new window for selected playlist
            Window newWindow = new Window(selectedPlaylist);
            newWindow.display();
        }
    }

        /**
         * Mouse listener to handle left and right clicks within
         * the Playlist Panel
         *
         */
    class PlaylistPanelMouseListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            JTree tree = (JTree) e.getSource();
            String selection = tree.getSelectionPath().getLastPathComponent().toString();

            if(selection.equals(null)) {
                selection = "Library";  // set selection to "Library" if null, as default
            }

            if(SwingUtilities.isRightMouseButton(e) && !selection.equals("Library")
                    && !selection.equals("Playlists")) {
                // An individual playlist was right clicked,

                // Set selected playlist
                selectedPlaylist = selection;

                // show popup menu
                maybeShowPopup(e);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            JTree tree = (JTree) e.getSource();
            String selection = tree.getSelectionPath().getLastPathComponent().toString();

            if(selection.equals(null)) {
                selection = "Library";  // set selection to "Library" if null, as default
            }

            // highlight selected row
            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);

            if(SwingUtilities.isLeftMouseButton(e)) {
                // left click pressed

                // If selection is not Playlist
                // ie. "Library" or a playlist name was selected
                if(!selection.equals("Playlists")) {
                    // Update the table model
                    musicTable.updateTableModel(selection);

                    // If library selected: ensure add song to playlist sub menu gets added back
                    // Else if individual playlist selected: remove the add song to playlist sub menu, set selectedPlaylist
                    if(selection.equals("Library")) {
                        musicTablePopupMenu.add(addSongToPlaylistSubMenu);
                    } else {
                        selectedPlaylist = selection;
                        musicTablePopupMenu.remove(addSongToPlaylistSubMenu);
                    }

                    // Repaint the music table scroll pane
                    musicTableScrollPane.repaint();
                }
            }
        }

        public void maybeShowPopup(MouseEvent e) {
            playlistPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

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

            // Refresh GUI popupmenu playlist sub menu
            updateAddPlaylistSubMenu();
            updatePlaylistNode();
            ((DefaultTreeModel)playlistPanelTree.getModel()).reload();

            // Expand playlist node (index 1)
            playlistPanelTree.expandRow(1);

            // Select playlist node just created
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) playlistPanelTree.getModel().getRoot();
            DefaultMutableTreeNode playlistsNode = (DefaultMutableTreeNode) playlistPanelTree.getModel().getChild(rootNode, 1);
            TreePath path = new TreePath(rootNode);
            path = path.pathByAddingChild(playlistsNode);
            int numPlaylists = playlistPanelTree.getModel().getChildCount(playlistsNode);
            for(int i = 0; i < numPlaylists; i++) {
                String node = playlistPanelTree.getModel().getChild(playlistsNode, i).toString();
                if(node.equals(playlistName)) {
                    path = path.pathByAddingChild(playlistsNode.getChildAt(i));
                    playlistPanelTree.addSelectionPath(path);
                }
            }

            // Update selected playlist
            selectedPlaylist = playlistName;

            // Update table model
            musicTable.updateTableModel(playlistName);
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
                ((DefaultTreeModel)playlistPanelTree.getModel()).reload(playlistNode);

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
            int min = musicTable.getMinSelectedRow();
            int max = musicTable.getMaxSelectedRow();

            for(int row = max; row >= min; row--) {
                String selectedSong = musicTable.getTable().getValueAt(row, 5).toString();
                ShiTunes.db.addSongToPlaylist(selectedSong, playlist);
            }
            // Expand playlist node (index 1)
            playlistPanelTree.expandRow(1);
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
            int previousSongIndex = musicTable.getTable().getSelectedRow() - 1;

            // Only skip to previous if the loaded song is not the first item in the table
            // and the loaded song is not set to -1 flag (which indicates that the
            // loaded song was opened via the File->Open menu)
            if(previousSongIndex >= 0) {
                if(playerState == BasicPlayerEvent.PLAYING ||
                   playerState == BasicPlayerEvent.RESUMED) {
                    // if player is currently playing/resumed
                    // stop current song
                    // decrement player.currentSongIndex
                    // play previous song
                    player.stop();
                    int songId = Integer.parseInt(musicTable.getTable().getValueAt(previousSongIndex, 0).toString());
                    player.setLoadedSongId(songId);
                    musicTable.getTable().setRowSelectionInterval(previousSongIndex, previousSongIndex);
                    player.play(ShiTunes.db.getSongFilePath(player.getLoadedSongId()));
                }
            }
        }
    }

    /**
     * Play Listener:
     * <p>
     * Plays the selected song loaded, if the conditions are right.
     *
     */
    class PlayListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // boolean indicator, true if selected song is currently loaded to player
            boolean selectedSongIsLoaded =
                    musicTable.getTable().getSelectedRow() == player.getLoadedSongId();
            if (selectedSongIsLoaded && playerState == BasicPlayerEvent.PAUSED) {
                // if selected song is current song on player
                // and player.state == paused
                player.resume();
            } else {
                // Get the index of the selected row
                int index = musicTable.getTable().getSelectedRow();
                if(index == -1) {
                    // if no row selected:
                    // set loaded song to first song in table
                    int songId = Integer.parseInt(musicTable.getTable().getValueAt(0, 0).toString());
                    musicTable.getTable().setRowSelectionInterval(0, 0);
                    player.setLoadedSongId(songId);
                } else {
                    // selected song found:
                    // set loaded song to selected song
                    int songId = Integer.parseInt(musicTable.getTable().getValueAt(index, 0).toString());
                    musicTable.getTable().setRowSelectionInterval(index, index);
                    player.setLoadedSongId(songId);
                }
                if (playerState == BasicPlayerEvent.PLAYING ||
                    playerState == BasicPlayerEvent.RESUMED ||
                    playerState == BasicPlayerEvent.PAUSED) {
                    // stop player
                    player.stop();
                }
                player.play(ShiTunes.db.getSongFilePath(player.getLoadedSongId()));
            }
        }
    }

    /**
     * Pause Listener:
     * <p>
     * Pauses the currently playing song
     *
     */
    class PauseListener implements ActionListener {
        /**
         * Calls the MusicPlayer pause function when event occurs
         *
         * @param e the ActionEvent object for this event
         */
        public void actionPerformed(ActionEvent e) {
            if(playerState == BasicPlayerEvent.PLAYING ||
               playerState == BasicPlayerEvent.RESUMED) {
                player.pause();
            }
        }
    }

    /**
     * Stops Listener:
     * <p>
     * Stops currently playing song
     *
     */
    class StopListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            player.stop();
        }
    }

    /**
     * Next Listener:
     * <p>
     * Highlights and plays the next song in the table (if there is one)
     *
     */
    class NextListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int nextSongIndex = musicTable.getTable().getSelectedRow() + 1;
            int lastItemInTable = musicTable.getTable().getRowCount() - 1;

            // Only skip to next if the loaded song is not the last item in the table
            if(nextSongIndex <= lastItemInTable) {
                if(playerState == BasicPlayerEvent.PLAYING ||
                   playerState == BasicPlayerEvent.RESUMED) {
                    player.stop();  // stop currently playing song
                    int songId = Integer.parseInt(musicTable.getTable().getValueAt(nextSongIndex, 0).toString());
                    player.setLoadedSongId(songId); // set player loaded song to next song
                    musicTable.getTable().setRowSelectionInterval(nextSongIndex, nextSongIndex); // highlight next song
                    player.play(ShiTunes.db.getSongFilePath(player.getLoadedSongId())); // play next song
                }
            }
        }
    }

    /**
     * Volume listener:
     * <p>
     * Takes value from volume slider
     * and converts to double in range [0.0, 1.0] to
     * set basic player gain (volume) to a value it understands.
     *
     */
    class VolumeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                // volume converted to double value in range [0.0, 1.0]
                // which is the range required by BasicPlayer setGain() method
                double volume = source.getValue() / 100.00;
                player.adjustVolume(volume);
            }
        }
    }

    /* ********************* */
    /* Main Menu Listeners */
    /* ********************* */

    /**
     * Open Item listener:
     * <p>
     * Opens and plays the selected song using
     * "quickPlay" method, which
     *
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
                    if (playerState == BasicPlayerEvent.PLAYING ||
                        playerState == BasicPlayerEvent.RESUMED ||
                        playerState == BasicPlayerEvent.PAUSED) {
                        // player.state == playing/resumed/paused
                        // stop player
                        player.stop();
                    }
                    player.play(selectedSong.getFilePath());
                }
            }
    }

    /**
     * Add Song Listener:
     * <p>
     * Opens a file chooser allowing user to select a song file
     * to add to the library/playlist.  The selected song is then added
     * to the library/playlist.
     * <p>
     * Then all application Windows tables are updated in the
     * event that the song(s) being removed from the table is
     * also present in another window/table.
     *
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
                int id = ShiTunes.db.insertSong(selectedSong);  // -1 if failure

                if(musicTable.getType() == MusicTable.LIBRARY) {
                    // If the music table == library
                    // Only add song to library table if it is not already present in db
                    if (id != -1) {
                        // if song successfully added to database
                        // add song to music library table
                        musicTable.addSongToTable(id, selectedSong);
                    }
                } else if(musicTable.getType() == MusicTable.PLAYLIST){
                    // If the music table == playlist
                    // Add song to the playlist
                    ShiTunes.db.addSongToPlaylist(selectedSong.getFilePath(), selectedPlaylist);
                    // Add song to playlist table
                    musicTable.addSongToTable(id, selectedSong);
                }

                ShiTunes.updateAllWindows();
            }
        }
    }

    /**
     * Delete Song Listener:
     * <p>
     * Deletes the selected range of songs from the table &
     * the database (if MusicTable.LIBRARY) or playlist
     * (if MusicTable.PLAYLIST).
     * <p>
     * Then all application Windows tables are updated in the
     * event that the song(s) being removed from the table is
     * also present in another window/table.
     *
     */
    class DeleteSongListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // range[0] = min index of selected range
            // range[1] = max index of selected range
            int min = musicTable.getMinSelectedRow();
            int max = musicTable.getMaxSelectedRow();

            DefaultTableModel model = (DefaultTableModel) musicTable.getTable().getModel();

            /*
            * Cycle through all selected songs and delete
            * one at a time
            *
            * Note: starts at the bottom of the selected rows (ie. max index)
            * and works it's way up the list of selected rows
            *
            */
            for(int row = max; row >= min; row--) {
                int selectedSongId = Integer.parseInt(musicTable.getTable().getValueAt(row, 0).toString());

                // Stop player if song being deleted is the current song on the player
                if(selectedSongId == player.getLoadedSongId()) {
                    player.stop();
                }

                model.removeRow(row);

                if(musicTable.getType() == MusicTable.LIBRARY) {
                    // Delete song from database by using filepath as an identifier
                    ShiTunes.db.deleteSong(selectedSongId);
                } else if(musicTable.getType() == MusicTable.PLAYLIST){
                    ShiTunes.db.deleteSongFromPlaylist(selectedSongId, selectedPlaylist);
                }
            }

            // Update all windows in the event that the song(s) being removed from the table
            // is also present in another window/table
            ShiTunes.updateAllWindows();
        }
    }

    /**
     * Exit item listener:
     * <p>
     * Closes the database connection and
     * exit the shiTunes program gracefully
     *
     */
    class ExitItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if(windowType == Window.MAIN) {
                ShiTunes.db.close();
                System.exit(0);
            } else if(windowType == Window.PLAYLIST) {
                windowFrame.dispatchEvent(new WindowEvent(windowFrame, WindowEvent.WINDOW_CLOSING));
            }
        }
    }

    /* *********************** *
     * Player Callback Methods *
     * *********************** */

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
        loadedSongBytes = Integer.parseInt(properties.get("audio.length.bytes").toString());
    }

    /**
     * Progress callback while playing.
     *
     * This method is called several time per seconds while playing.
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

        /*
        * TODO: This is an attempt to skip to next song immediately after current song ends
        *       but it isn't working properly, sound is funky for some reason, this is probably the wrong way to do this
        */
        if(bytesread == loadedSongBytes) {
            NextListener nextListener = new NextListener();
            nextListener.actionPerformed(null);
        }
    }

    /**
     * Notification callback for basicplayer events such as opened
     * <p>
     * States Codes - see state variable comment
     *
     * @param event the basicplayer event (OPENED, PAUSED, PLAYING, SEEKING...)
     */
    public void stateUpdated(BasicPlayerEvent event)
    {
        // Notification of BasicPlayer states (opened, playing, end of media, ...)
        if(event.getCode() != BasicPlayerEvent.GAIN) {
            // if state is not GAIN (due to volume change)
            // update state code
            playerState = event.getCode();
        } else {
            // do nothing, retain previous state
        }
    }

    /**
     * Public accessor for player state
     * <p>
     * States Codes - see state variable comment
     *
     * @return the players state
     */
    public int getState() {
        return playerState;
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

    /* ********************* *
    * Window related methods *
    * ********************** */

     /**
     * Window listener for Window.type == PLAYLIST
     *
     */
    class PlaylistWindowListener implements WindowListener {
         @Override
         public void windowActivated(WindowEvent e) {
         }

         @Override
         public void windowClosed(WindowEvent e) {
             // Remove window from list of application windows
             ShiTunes.windows.remove(this);
         }

         @Override
         public void windowClosing(WindowEvent e) {
         }

         @Override
         public void windowDeactivated(WindowEvent e) {
         }

         @Override
         public void windowDeiconified(WindowEvent e) {
         }

         @Override
         public void windowOpened(WindowEvent e) {
         }

         @Override
         public void windowIconified(WindowEvent e) {
         }
    }

    /**
     * Gets this Window object's type (MAIN or PLAYLIST)
     *
     * @return the window type of this Window
     */
    public int getWindowType() {
        return windowType;
    }

    /**
     * Gets the MusicTable associated with this Window
     *
     * @return the MusicTable associated with this Window
     */
    public MusicTable getMusicTable() {
        return musicTable;
    }

    /**
     * Gets the name of the playlist that this current window is displaying
     * (returns "Library" if it is the "Library" table)
     *
     * @return the MusicTable associated with this Window
     */
    public String getSelectedPlaylist() {
        return selectedPlaylist;
    }
}