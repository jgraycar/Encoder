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

import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v1;
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
import java.io.UnsupportedEncodingException;

/**
 *  @author Joel Graycar
 *  @email jgraycar@berkeley.edu
 */

public class EncoderGUI {

    Encoder enc;
    JFrame frame;
    JPanel filePanel, centerPanel, musicPanel;
    JPanel fileNamePanel;
    JMenuBar menuBar;
    JMenu fileMenu, actionMenu;
    JMenuItem chooseItem, openItem, encryptItem, decryptItem, saveItem;
    JLabel fileNameLabel;
    JProgressBar progBar;
    JTextArea fileTextArea;
    JButton playButt, pauseButt;
    JFileChooser jFile;
    FileDialog fileChooser;
    File currFile, saveFile;
    Color bckgrndClr;
    String[] files;
    String fileName, fileType, fileExt, fileText, fileSize;
    int[] fileBytes;
    int state;
    BufferedImage img;
    boolean firstTime;

    public static void main(String... args) {
        EncoderGUI gui = new EncoderGUI();
        gui.go();
    }

    public void go() {
        enc = new Encoder();
        bckgrndClr = new Color(211, 211, 211);
        progBar = new JProgressBar(0, 100);
        frame = new JFrame();
        filePanel = new ColoredPanel();
        centerPanel = new ColoredPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JButton openButt = new JButton("Select a file");
        openButt.addActionListener(new ChooseItemListener());
        JPanel panel = new ColoredPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalGlue());
        panel.add(openButt);
        panel.add(Box.createHorizontalGlue());
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(panel);
        centerPanel.add(Box.createVerticalGlue());
        fileChooser = new FileDialog(frame);
        jFile = new JFileChooser();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        frame.getContentPane().add(BorderLayout.CENTER, centerPanel);

        // Set up menu bar
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        int cmndKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        chooseItem = new JMenuItem("Open");
        openItem = new JMenuItem("Open Externally");
        saveItem = new JMenuItem("Save As...");
        chooseItem.addActionListener(new ChooseItemListener());
        openItem.addActionListener(new OpenItemListener());
        saveItem.addActionListener(new SaveItemListener());
        chooseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                         cmndKey));
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                       InputEvent.SHIFT_MASK |
                                                       cmndKey));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                       InputEvent.SHIFT_MASK |
                                                       cmndKey));
        saveItem.setEnabled(false);
        menuBar.add(fileMenu);
        fileMenu.add(saveItem);
        fileMenu.add(chooseItem);
        if (Desktop.isDesktopSupported()) {
            fileMenu.add(openItem);
        }
        actionMenu = new JMenu("Convert");
        menuBar.add(actionMenu);
        encryptItem = new JMenuItem("Encrypt");
        decryptItem = new JMenuItem("Decrypt");
        encryptItem.addActionListener(new EncryptItemListener());
        decryptItem.addActionListener(new DecryptItemListener());
        encryptItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                                                         cmndKey));
        decryptItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                                                         cmndKey));
        actionMenu.add(encryptItem);
        actionMenu.add(decryptItem);
        frame.setJMenuBar(menuBar);

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

    /** Display a message indicating success of a task.
     *  @param msg is the message to be displayed. */
    private void popup(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Success!",
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    /** Display an error, with message MSG.
     *  @param msg is the error message to be displayed. */
    private void popupErr(String msg) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, msg, "Error",
                                      JOptionPane.ERROR_MESSAGE);
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
            popupErr("File \"" + fileName + "\" is not encrypted.");
        } else if (status == 2) {
            popupErr("Encountered IOException.");
        } else if (status == 0) {
            popup("\"" + fileName + tail.toString());
            updateDisplay();
        }
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
        int[] transformed;
        if (state == 1) {
            transformed = enc.decode(fileBytes);
            if (transformed == null) {
                return 1;
            }
        } else {
            transformed = enc.encode(fileBytes);
        }
        fileBytes = transformed;
        frame.getRootPane().setCursor(null);
        return status;
    }

    /** Display the new relevant information on centerPanel.
     *  creates a OpenFileTask thread that determines and sets FileType,
     *  then uses FileType to call one of the fileType_____ methods.
     */
    private void updateDisplay() {
        // Need some thread to handle appearance / disappearance of progBar
        centerPanel.removeAll();
        centerPanel.revalidate();
        centerPanel.repaint();
        progBar.setValue(0);
        OpenFileTask task = new OpenFileTask(fileBytes);
        frame.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        task.start();
        try {
            task.join();
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
        } catch (InterruptedException e) {
            popupErr("Thread was interrupted.");
            fileType = "";
        }
        double len = fileBytes.length;
        double kiloB = len / 1024;
        double megaB = kiloB / 1024;
        double gigaB = megaB / 1024;
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
        saveItem.setEnabled(true);
    }

    /** Turn a double into a number to the second decimal point. */
    private BigDecimal roundOff(double d) {
        return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP);
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
        String artist, title, album;
        artist = title = album = "";
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
                    centerPanel.add(Box.createVerticalGlue());
                    centerPanel.add(imgLabel);
                }
                artist = tag.getArtist();
                title = tag.getTitle();
                album = tag.getAlbum();
            } else if (mp3.hasId3v1Tag()) {
                ID3v1 tag = mp3.getId3v2Tag();
                artist = tag.getArtist();
                title = tag.getTitle();
                album = tag.getAlbum();
            }
        } catch (Exception e) {}
        JLabel artistLabel = new JLabel("Artist: " + artist);
        JLabel albumLabel = new JLabel("Album: " + album);
        JLabel titleLabel = new JLabel("Title: " + title);
        centerPanel.add(artistLabel);
        centerPanel.add(albumLabel);
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalGlue());
    }

    private void fileTypeUnknown() {
        fileTextArea.setText(fileText);
        centerPanel.add(filePanel);
    }

    /** Uses the Apache POI API to extract the text from a Microsoft Office file.
     *  If BSTREAM is not of any recognized Office format, will return null.
     *  @param bStream is the bytes of the current file.
     *  @return returns the text of an Office file, or null if the file
     *          represented by bStream is not an Office file.
     */
    private String officeDocText(ByteArrayInputStream bStream) {
        StringBuilder str = new StringBuilder();
        try {
            POITextExtractor extractor =
                ExtractorFactory.createExtractor(bStream);
            str.append(reformat(extractor.getText()));
        } catch (Exception e) {
            return null;
        }
        return str.toString();
    }

    private int saveFile() {
        int status = 0;
        try {
            FileOutputStream fileW = new FileOutputStream(saveFile);
            for (int b : fileBytes) {
                fileW.write(b);
            }
            fileW.close();
        } catch (IOException io) {
            status = 2;
        }
        return status;
    }

    // --------------------------- Threads --------------------------------

    private class OpenFileTask extends Thread {

        private byte[] realBytes;

        public OpenFileTask(int[] newBytes) {
            fileBytes = newBytes;
            this.realBytes = new byte[newBytes.length];
            for (int i = 0; i < newBytes.length; i += 1) {
                this.realBytes[i] = (byte) newBytes[i];
            }
        }

        public void run() {
            try {
                progBar.setValue(10);
                fileName = jFile.getName(currFile);
                String[] nameParts = fileName.split("\\.");
                fileExt = nameParts[nameParts.length - 1];
                img = ImageIO.read(new ByteArrayInputStream(realBytes));
                try {
                    fileText = new String(realBytes, "Cp850");
                } catch (UnsupportedEncodingException u) {
                    fileText = new String(realBytes, "UTF8");
                }
                if (img != null) {
                    progBar.setValue(80);
                    fileType = "image";
                } else {
                    if (state == 0) {
                        progBar.setValue(15);
                        progBar.setValue(90);
                        fileType = "unknown";
                    } else {
                        progBar.setValue(15);
                        String testText = officeDocText(new ByteArrayInputStream(realBytes));
                        progBar.setValue(25);
                        if (testText != null) {
                            progBar.setValue(75);
                            fileText = testText;
                            fileType = "office";
                        } else {
                            progBar.setValue(35);
                            if (fileExt.equals("mp3")) {
                                try {
                                    Mp3File musicFile = new Mp3File(currFile.getAbsolutePath());
                                    progBar.setValue(75);
                                    fileType = "music";
                                } catch (Exception e) {
                                    e.printStackTrace(System.out);
                                    progBar.setValue(90);
                                    fileType = "unknown";
                                }
                            } else {
                                progBar.setValue(90);
                                fileType = "unknown";
                            }
                        }
                    }
                }
            } catch (IOException io) {
                popupErr("Encountered IOException while updating UI");
            }
            progBar.setValue(95);
            firstTime = false;
            progBar.setValue(100);
	}
    }

    // ---------------------------- Listeners -----------------------------

    class SaveItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            fileChooser.setMode(FileDialog.SAVE);
            fileChooser.setTitle("Save text as...");
            fileChooser.setVisible(true);
            File[] files = fileChooser.getFiles();
            if (files.length > 0) {
                saveFile = files[0];
                int status = saveFile();
                if (status != 0) {
                    popupErr("Could not save file.");
                } else {
                    currFile = saveFile;
                    fileName = jFile.getName(currFile);
                    fileNameLabel.setText(fileName + ": " + fileSize);
                    updateDisplay();
                }
            }
        }
    }

    class ChooseItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            state = -1;
            fileChooser.setMode(FileDialog.LOAD);
            fileChooser.setTitle("Select a file to open");
            fileChooser.setVisible(true);
            File[] files = fileChooser.getFiles();
            if (files.length > 0) {
                currFile = files[0];
                try {
                    FileInputStream file = new FileInputStream(currFile);
                    ArrayList<Integer> byteArr = new ArrayList<Integer>();
                    int currByte = file.read();
                    while (currByte > -1) {
                        byteArr.add(currByte);
                        currByte = file.read();
                    }
                    progBar.setValue(5);
                    fileBytes = new int[byteArr.size()];
                    for (int i = 0; i < byteArr.size(); i += 1) {
                        int val = byteArr.get(i);
                        fileBytes[i] = val;
                    }
                    updateDisplay();
                } catch (FileNotFoundException f) {
                    popupErr("File \"" + currFile + "\" could not be opened.");
                } catch (IOException io) {
                    popupErr("While retrieving file text.");
                }
            }
        }
    }

    class EncryptItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (currFile != null) {
                state = 0;
                doAction();
            } else {
                fileChooser.setMode(FileDialog.LOAD);
                fileChooser.setTitle("Select a file to open");
                fileChooser.setVisible(true);
                File[] files = fileChooser.getFiles();
                if (files.length > 0) {
                    currFile = files[0];
                    state = 0;
                    doAction();
                }
            }
        }
    }

    class DecryptItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (currFile != null) {
                state = 1;
                doAction();
            }
        }
    }

    class OpenItemListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (currFile == null) {
                fileChooser.setMode(FileDialog.LOAD);
                fileChooser.setTitle("Select a file to open externally");
                fileChooser.setVisible(true);
                File[] files = fileChooser.getFiles();
                if (files.length > 0) {
                    try {
                        Desktop.getDesktop().open(files[0]);
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
            } else {
                try {
                    Desktop.getDesktop().open(currFile);
                } catch (IOException io) {
                    popupErr("No default application for file type \"." +
                             fileExt + "\"");
                } catch (NullPointerException nll) {
                    popupErr("Null file.");
                } catch (IllegalArgumentException ill) {
                    popupErr("File not found.");
                    } catch (SecurityException sec) {
                    popupErr("Do not have permission to read file.");
                }
            }
        }
    }

    // ----------------------------- Panels -------------------------------

    class ColoredPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(bckgrndClr);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

}
