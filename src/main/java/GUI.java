import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.io.*;
import javax.swing.table.*;
import java.awt.EventQueue;
import java.awt.Dimension;

/**
 * Created by michael on 10/3/2014.
 */
public class GUI {
    //private JTable libraryTable;
    //private JSlider slider1;
    private JButton playButton;
    private JButton pauseButton;
    private JFrame shiTunesFrame;
    private JTable libTable;
    private JPanel panel1;
    private JSlider slider1;
    private Object[] columnNames = {"Title", "Artist", "Album", "Genre", "Length", "Year"};
    private Object[][] dummyData = {
            {"Champions Of Red Wine", "The New Pornographers", "Brill Bruisers", "Alternative", "3:41", "2014"},
            {"Follow Me", "Muse", "The 2nd Law", "Rock", "3:51", "2013"},
            {"Divinity", "Porter Robinson", "Worlds", "Electronic", "6:08", "2014"},
            {"Jealous (I Ain't With It)", "Chromeo", "White Women","Electronic", "3:48","2013"},
            {"Habits (Stay High)", "Tove Lo", "Truth Serum", "Alternative", "4:18","2014"},
            {"Cool Kids", "Echosmith", "Talking Dreams", "Alternative", "3:58","2014"},
            {"Stolen Dance", "Milky Chance", "Stolen Dance", "Rap", "5:11","2014"},
            {"Buddy Holly", "Weezer", "Weezer", "Rock", "2:40","2010"},
            {"My Girls", "Animal Collective", "Merriweather Post Pavillion", "Alternative", "5:41", "2006" }
    };
    private DefaultTableModel model;

    public GUI() {
    }
    public void displayGUI()
    {
        shiTunesFrame = new JFrame();
        shiTunesFrame.setTitle("ShiTunes");
        shiTunesFrame.setSize(600, 400);
        shiTunesFrame.setLocationRelativeTo(null);
        shiTunesFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel panel1 = new JPanel();
        libTable = new JTable(dummyData, columnNames);
        JScrollPane scrollPane = new JScrollPane(libTable);
        libTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        libTable.setFillsViewportHeight(true);
        panel1.add(scrollPane);

        try {
            BufferedImage imgPlay = ImageIO.read(new File("C:\\Users\\micha_000\\Java\\shitunes\\images\\play2.png"));
            ImageIcon playImg = new ImageIcon(imgPlay);
            playButton = new JButton("Play");
            playButton.setIcon(playImg);
            playButton.setHorizontalTextPosition(AbstractButton.LEFT);
            playButton.setVerticalTextPosition(AbstractButton.BOTTOM);

            panel1.add(playButton);
            //JOptionPane.showMessageDialog(null, playButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedImage imgPause = ImageIO.read(new File("C:\\Users\\micha_000\\Java\\shitunes\\images\\pause.png"));
            ImageIcon pauseImg = new ImageIcon(imgPause);
            pauseButton = new JButton("Pause Button");
            //JOptionPane.showMessageDialog(null, pauseButton);
            pauseButton.setIcon(pauseImg);
            pauseButton.setHorizontalTextPosition(AbstractButton.CENTER);
            pauseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            panel1.add(pauseButton);
        } catch (IOException e) {
            e.printStackTrace();
        }

        shiTunesFrame.setContentPane(panel1);
        shiTunesFrame.pack();
        shiTunesFrame.setLocationByPlatform(true);
        shiTunesFrame.setVisible(true);

    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public static void main (String []args)
    {

        GUI gui = new GUI();
        gui.displayGUI();
    }
}


