package enc;

import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.imageio.*;

import java.net.MalformedURLException;
import java.net.URL;

import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerException;

import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.mpatric.mp3agic.InvalidDataException;

import java.lang.StringBuilder;

import java.util.ArrayList;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.io.File;
import java.io.FileWriter;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * @author Joel Graycar
 */

public class EncoderGUI {

    Encoder enc;
    JFrame frame;
    ColoredPanel filePanel, centerPanel, musicPanel;
    JPanel fileNamePanel;
    JLabel fileNameLabel;
    JProgressBar progBar;
    JTextArea fileTextArea;
    JButton encButt, decButt, chooseButt, openButt;
    RoundButton playButt, pauseButt, colorButt;
    JFileChooser jFile;
    FileDialog fileChooser;
    File currFile, saveFile;
    Color bckgrndClr;
    String[] files;
    String fileName, fileType, fileExt, fileText;
    ButtonsPanel buttons;
    BufferedImage img;
    boolean firstTime;
    int state;
    int[] bytes;
    BasicController control;

    public static void main(String... args) {
        EncoderGUI gui = new EncoderGUI();
        gui.go();
    }

    public void go() {
        enc = new Encoder();
        bckgrndClr = new Color(176, 224, 230);
        progBar = new JProgressBar(0, 100);
        frame = new JFrame();
        filePanel = new ColoredPanel();
        centerPanel = new ColoredPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        fileChooser = new FileDialog(frame);
        jFile = new JFileChooser();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        frame.getContentPane().add(BorderLayout.CENTER, centerPanel);

        // Set up enc/dec & choose/open buttons
        encButt = new JButton("Encrypt");
        decButt = new JButton("Decrypt");
        chooseButt = new JButton("Choose file");
        openButt = new JButton("Open file");
        try {
            BufferedImage colorIcon =
                ImageIO.read(new File("/Users/Joel/CompSci/Encoder/src/color.gif"));
            colorButt = new RoundButton(new ImageIcon(colorIcon));
            colorButt.setBorder(BorderFactory.createEmptyBorder());
            colorButt.setContentAreaFilled(false);
        } catch (IOException io) {
            colorButt = new RoundButton("color");
        }
        encButt.addActionListener(new EncryptButtonListener());
        decButt.addActionListener(new DecryptButtonListener());
        chooseButt.addActionListener(new ChooseButtonListener());
        openButt.addActionListener(new OpenButtonListener());
        colorButt.addActionListener(new ColorButtonListener());
        encButt.setOpaque(true);
        decButt.setOpaque(true);
        chooseButt.setOpaque(true);
        openButt.setOpaque(true);
        colorButt.setOpaque(true);
        encButt.setBackground(bckgrndClr);
        decButt.setBackground(bckgrndClr);
        chooseButt.setBackground(bckgrndClr);
        openButt.setBackground(bckgrndClr);
        colorButt.setBackground(bckgrndClr);
        buttons = new ButtonsPanel();
        buttons.add(encButt);
        buttons.add(decButt);
        buttons.add(chooseButt);
        buttons.add(openButt);
        openButt.setVisible(false);
        buttons.add(colorButt);
        centerPanel.add(buttons);
        centerPanel.add(filePanel);

        // Set up music player
        musicPanel = new ColoredPanel();
        BasicPlayer player = new BasicPlayer();
        control = (BasicController) player;
        musicPanel.setLayout(new BoxLayout(musicPanel, BoxLayout.X_AXIS));
        //Box box = Box.createHorizontalBox();
        try {
            BufferedImage playIcon =
                ImageIO.read(new File("/Users/Joel/CompSci/Encoder/src/play.gif"));
            BufferedImage pauseIcon =
                ImageIO.read(new File("/Users/Joel/CompSci/Encoder/src/pause.png"));
            playButt = new RoundButton(new ImageIcon(playIcon));
            pauseButt = new RoundButton(new ImageIcon(pauseIcon));
            playButt.setBorder(BorderFactory.createEmptyBorder());
            pauseButt.setBorder(BorderFactory.createEmptyBorder());
            playButt.setContentAreaFilled(false);
            pauseButt.setContentAreaFilled(false);
            playButt.setSize(50, 50);
            pauseButt.setSize(50, 50);
        } catch (IOException io) {
            playButt = new RoundButton("Play");
            pauseButt = new RoundButton("Pause");
        }
        playButt.addActionListener(new PlayButtonListener());
        pauseButt.addActionListener(new PauseButtonListener());
        playButt.setOpaque(true);
        pauseButt.setOpaque(true);
        playButt.setBackground(bckgrndClr);
        pauseButt.setBackground(bckgrndClr);
        //box.add(playButt);
        //box.add(pauseButt);
        //box.setOpaque(true);
        //box.setBackground(bckgrndClr);
        //musicPanel.add(box);
        musicPanel.add(playButt);
        musicPanel.add(pauseButt);

        // Set up text field
        fileTextArea = new JTextArea(45, 50);
        fileTextArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(fileTextArea);
        fileTextArea.setLineWrap(true);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        filePanel.add(scroller);

        // Set up fileNameLabel and fileNamePanel
        fileNamePanel = new JPanel();
        fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.X_AXIS));
        fileNameLabel = new JLabel("");
        fileNamePanel.add(Box.createHorizontalGlue());
        fileNamePanel.add(fileNameLabel);
        fileNamePanel.add(Box.createHorizontalGlue());
        frame.getContentPane().add(BorderLayout.SOUTH, fileNamePanel);

        frame.setSize(1000, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(176, 224, 230));
        frame.setVisible(true);
        firstTime = true;
    }

    private void popup(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Success!",
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    private void popupErr(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error",
                                      JOptionPane.ERROR_MESSAGE);
    }

    /** Query user for desired file to save transformed text in. */
    private void queryName() {
        fileChooser.setMode(FileDialog.SAVE);
        fileChooser.setVisible(true);
        File[] files = fileChooser.getFiles();
        if (files.length > 0) {
            saveFile = files[0];
            doAction();
        }
    }

    /** Handle the implementation of the encrypt/decrypt action.
     *  state = 1 means decrypt, 0 means encrypt.
     *  State is set by the encrypt/decrypt buttons. */
    private void doAction() {
        StringBuilder newText = new StringBuilder();
        StringBuilder tail = new StringBuilder();
        if (state == 1) {
            tail.append("\" successfully decrypted!");
        } else {
            tail.append("\" successfully encrypted!");
        }
        int status = transformText();
        if (status == 1) {
            popupErr("Error: cannot decrypt; file \"" +
                     fileName + "\" is not encrypted.");
        } else if (status == 2) {
            popupErr("Error: encountered IOException.");
        } else if (status == 0) {
            popup("\"" + fileName + tail.toString());
            currFile = saveFile;
            fileChosen();
        }
    }

    /** Stores the bytes of currFile in int[] bytes, sets fileName and fileType,
     *  sets fileNameLabel and fileText. Displays popup message if file cannot
     *  be opened, or encounter IOException while retrieving text.
     */
    private void fileChosen() {
        // Need some thread to handle appearance / disappearance of progBar
        try {
            control.stop();
        } catch (BasicPlayerException b) {}
        progBar.setValue(0);
        OpenFileTask task = new OpenFileTask();
        frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        task.start();
        try {
            task.join();
        } catch (InterruptedException e) {
            popupErr("Error: thread was interrupted.");
            fileType = "";
        }
        switch (fileType) {
        case "image":
            fileTypeImageFile();
            break;
        case "office":
            fileTypeOfficeFile();
            break;
        case "music":
            fileTypeMusicFile();
            break;
        case "unknown":
            fileTypeUnknown();
            break;
        default:
            popupErr("Error: file was not read correctly.");
            break;
        }
        double len = currFile.length();
        double kiloB = len / 1024;
        double megaB = kiloB / 1024;
        double gigaB = megaB / 1024;
        String fileSize;
        if (gigaB >= 1) {
            fileSize = roundOff(gigaB) + " GB";
        } else if (megaB >= 1) {
            fileSize = roundOff(megaB) + " MB";
        } else if (kiloB >= 1) {
            fileSize = roundOff(kiloB) + " KB";
        } else {
            fileSize = len + " bytes";
        }
        fileNameLabel.setText(fileName + ": " + fileSize);
        frame.getRootPane().setCursor(null);
    }

    private BigDecimal roundOff(double d) {
        return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP);
    }

    /** Uses the Apache POI API to extract the text from a Microsoft Office file.
     *  If currFile is not of any recognized Office format, will return null. */
    private String officeDocText() {
        StringBuilder str = new StringBuilder();
        try {
            POITextExtractor extractor =
                ExtractorFactory.createExtractor(currFile);
            str.append(reformat(extractor.getText()));
        } catch (Exception e) {
            return null;
        }
        return str.toString();
    }
    /** Adds an extra newline between each paragraph of TEXT.
     *  @param text is the output of extractor.getText().
     *  @return returns TEXT with an extra newline after each paragraph.
     */
    private String reformat(String text) {
        String[] parts = text.split("\n");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < parts.length; i += 1) {
            str.append(parts[i] + "\n" + "\n");
        }
        return str.toString();
    }

    /** Do the work of encrypting/decrypting text. 
     *  int[] bytes is the bytes of the current file, as found by fileChosen().
     *  @return 0 if text successfully transformed and saved,
     *          1 if op is decode and text not encrypted,
     *          2 if could not write to saveFile.
     */
    private int transformText() {
        frame.getRootPane().setCursor((Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
        int status = 0;
        int[] transformed = null;
        boolean worked = true;
        if (state == 1) {
            transformed = enc.decode(bytes);
            if (transformed == null) {
                return 1;
            }
        } else {
            transformed = enc.encode(bytes);
        }
        try {
            FileOutputStream fileW = new FileOutputStream(saveFile);
            for (int b : transformed) {
                fileW.write(b);
            }
            fileW.close();
        } catch (IOException io) {
            status = 2;
        }
        frame.getRootPane().setCursor(null);
        return status;
    }

    private void fileTypeOfficeFile() {
        fileTextArea.setText(fileText);
        centerPanel.add(filePanel);
    }

    private void fileTypeImageFile() {
        JLabel imgLabel = new JLabel(new ImageIcon(img));
        JScrollPane scroller = new JScrollPane(imgLabel);
        centerPanel.add(scroller);
    }

    private void fileTypeMusicFile() {
        try {
            Mp3File mp3 = new Mp3File(currFile.getAbsolutePath());
            if (mp3.hasId3v2Tag()) {
                ID3v2 tag = mp3.getId3v2Tag();
                byte[] imageData = tag.getAlbumImage();
                if (imageData != null) {
                    String mimeType = tag.getAlbumImageMimeType();
                    BufferedImage img =
                        ImageIO.read(new ByteArrayInputStream(imageData));
                    JLabel imgLabel = new JLabel(new ImageIcon(img));
                    centerPanel.add(imgLabel);
                }
            }
        } catch (Exception e) {}
        centerPanel.add(musicPanel);
        centerPanel.add(Box.createVerticalGlue());
        try {
            control.play();
            control.setGain(1);
        } catch (BasicPlayerException err) {
            popupErr("Error: could not play music file.");
        }
    }

    private void fileTypeUnknown() {
        fileTextArea.setText(fileText);
        centerPanel.add(filePanel);
    }

    private class OpenFileTask extends Thread {

        public OpenFileTask() {

        }

        public void run() {
            try {
                FileInputStream file = new FileInputStream(currFile);
                ArrayList<Integer> byteArr = new ArrayList<Integer>();
                int currByte = file.read();
                while (currByte > -1) {
                    byteArr.add(currByte);
                    currByte = file.read();
                }
                progBar.setValue(5);
                bytes = new int[byteArr.size()];
                byte[] realBytes = new byte[bytes.length];
                for (int i = 0; i < byteArr.size(); i += 1) {
                    int val = byteArr.get(i);
                    bytes[i] = val;
                    realBytes[i] = (byte) val;
                }
                progBar.setValue(10);
                fileName = jFile.getName(currFile);
                String[] nameParts = fileName.split("\\.");
                fileExt = nameParts[nameParts.length - 1];
                centerPanel.removeAll();
                centerPanel.revalidate();
                centerPanel.repaint();
                centerPanel.add(buttons);
                img = ImageIO.read(currFile);
                if (img != null) {
                    progBar.setValue(80);
                    fileType = "image";
                } else {
                    progBar.setValue(15);
                    fileText = new String(realBytes, "MacRoman");
                    String testText = officeDocText();
                    progBar.setValue(25);
                    if (testText != null) {
                        progBar.setValue(75);
                        fileText = testText;
                        fileType = "office";
                    } else {
                        progBar.setValue(35);
                        if (fileExt.equals("mp3")) {
                            try {
                                control.open(currFile);
                                progBar.setValue(75);
                                fileType = "music";
                            } catch (Exception e) {
                                progBar.setValue(90);
                                fileType = "unknown";
                            }
                        } else {
                            progBar.setValue(90);
                            fileType = "unknown";
                        }
                    }
                }
                progBar.setValue(95);
                if (firstTime && Desktop.isDesktopSupported()) {
                    openButt.setVisible(true);
                }
                firstTime = false;
                progBar.setValue(100);
            } catch (FileNotFoundException f) {
                popupErr("Error: file \"" + currFile + "\" could not be opened.");
            } catch (IOException e) {
                popupErr("Error while retrieving file text.");
            }

        }

    }

    // ---------------------------- Listeners -----------------------------

    class ChooseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            fileChooser.setMode(FileDialog.LOAD);
            fileChooser.setVisible(true);
            File[] files = fileChooser.getFiles();
            if (files.length > 0) {
                currFile = files[0];
                fileChosen();
            }
        }
    }

    class EncryptButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            state = 0;
            queryName();
        }
    }

    class DecryptButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            state = 1;
            queryName();
        }
    }

    class OpenButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                Desktop.getDesktop().open(currFile);
            } catch (IOException io) {
                popupErr("Error: no default application for file type \"." +
                         fileExt + "\"");
            } catch (NullPointerException nll) {
                popupErr("Error: null file.");
            } catch (IllegalArgumentException ill) {
                popupErr("Error: file not found.");
            } catch (SecurityException sec) {
                popupErr("Error: do not have permission to read file.");
            }
        }
    }

    class PlayButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                control.resume();
            } catch (BasicPlayerException p) {
                p.printStackTrace();
            }
        }
    }

    class PauseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                control.pause();
            } catch (BasicPlayerException p) {
                p.printStackTrace();
            }
        }
    }

    class ColorButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            int r = (int) (Math.random() * 255);
            int g = (int) (Math.random() * 255);
            int b = (int) (Math.random() * 255);
            bckgrndClr = new Color(r, g, b);
            for (JButton button : buttons.getButtons()) {
                button.setBackground(bckgrndClr);
            }
            playButt.setBackground(bckgrndClr);
            pauseButt.setBackground(bckgrndClr);
            frame.repaint();
        }
    }

    // ----------------------------- Panels -------------------------------

    class ColoredPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(bckgrndClr);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    class ButtonsPanel extends JPanel {

        private ArrayList<JButton> butts;

        public ButtonsPanel() {
            butts = new ArrayList<JButton>();
        }

        public void paintComponent(Graphics g) {
            g.setColor(bckgrndClr);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

        public void add(JButton butt) {
            super.add(butt);
            butts.add(butt);
        }

        public ArrayList<JButton> getButtons() {
            return butts;
        }

    }

}
