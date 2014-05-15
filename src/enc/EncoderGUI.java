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

import java.lang.StringBuilder;

import java.util.ArrayList;

import java.io.File;
import java.io.FileWriter;
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
    JPanel filePanel, fileNamePanel, centerPanel, musicPanel;
    JLabel fileNameLabel;
    JTextArea fileText;
    JButton encButt, decButt, chooseButt, openButt, playButt, pauseButt;
    JFileChooser jFile;
    FileDialog fileChooser;
    File currFile, saveFile;
    Color bckgrndClr;
    String[] files;
    String fileName, fileType;
    ButtonsPanel buttons;
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
        frame = new JFrame();
        filePanel = new JPanel();
        centerPanel = new JPanel();
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
        encButt.addActionListener(new EncryptButtonListener());
        decButt.addActionListener(new DecryptButtonListener());
        chooseButt.addActionListener(new ChooseButtonListener());
        openButt.addActionListener(new OpenButtonListener());
        encButt.setOpaque(true);
        decButt.setOpaque(true);
        chooseButt.setOpaque(true);
        openButt.setOpaque(true);
        encButt.setBackground(bckgrndClr);
        decButt.setBackground(bckgrndClr);
        chooseButt.setBackground(bckgrndClr);
        openButt.setBackground(bckgrndClr);
        buttons = new ButtonsPanel();
        buttons.add(encButt);
        buttons.add(decButt);
        buttons.add(chooseButt);
        centerPanel.add(buttons);
        centerPanel.add(filePanel);

        // Set up music player
        musicPanel = new JPanel();
        BasicPlayer player = new BasicPlayer();
        control = (BasicController) player;
        musicPanel.setLayout(new BoxLayout(musicPanel, BoxLayout.X_AXIS));
        playButt = new JButton("Play");
        pauseButt = new JButton("Pause");
        playButt.addActionListener(new PlayButtonListener());
        pauseButt.addActionListener(new PauseButtonListener());
        musicPanel.add(playButt);
        musicPanel.add(pauseButt);

        // Set up text field
        fileText = new JTextArea(45, 50);
        fileText.setEditable(false);
        JScrollPane scroller = new JScrollPane(fileText);
        fileText.setLineWrap(true);
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
        JOptionPane.showMessageDialog(null, msg, "Success!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void popupErr(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
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
            popupErr("Error: cannot decrypt; file \"" + fileName + "\" is not encrypted.");
        } else if (status == 2) {
            popupErr("Error: encountered IOException.");
        } else if (status == 0) {
            popup("\"" + fileName + tail.toString());
            currFile = saveFile;
            fileChosen();
        }
    }

    /** Stores the bytes of currFile in int[] bytes, sets fileName and fileType,
     *  sets fileNameLabel and fileText. Displays popup message if file cannot be opened,
     *  or encounter IOException while retrieving text.
     */
    private void fileChosen() {
        try {
            FileInputStream file = new FileInputStream(currFile);
            ArrayList<Integer> byteArr = new ArrayList<Integer>();
            int currByte = file.read();
            while (currByte > -1) {
                byteArr.add(currByte);
                currByte = file.read();
            }
            bytes = new int[byteArr.size()];
            byte[] realBytes = new byte[bytes.length];
            for (int i = 0; i < byteArr.size(); i += 1) {
                int val = byteArr.get(i);
                bytes[i] = val;
                realBytes[i] = (byte) val;
            }
            fileName = jFile.getName(currFile);
            String[] nameParts = fileName.split("\\.");
            fileType = nameParts[nameParts.length - 1];
            fileNameLabel.setText(fileName);
            centerPanel.removeAll();
            centerPanel.revalidate();
            centerPanel.repaint();
            centerPanel.add(buttons);
            boolean music = true;
            try {
                control.open(currFile);
                control.play();
                control.setGain(0.85);
                System.out.println("music file");
            } catch (Exception e) {
                music = false;
            }
            if (music) {
                centerPanel.add(musicPanel);
            } else {
                BufferedImage img = ImageIO.read(currFile);
                if (img != null) {
                    JLabel imgLabel = new JLabel(new ImageIcon(img));
                    JScrollPane scroller = new JScrollPane(imgLabel);
                    centerPanel.add(scroller);
                } else {
                    centerPanel.add(filePanel);
                    String text = new String(realBytes, "MacRoman");
                    String testText = wordDocText();
                    if (testText != null) {
                        text = testText;
                    }
                    fileText.setText(text);
                }
            }
            if (firstTime && Desktop.isDesktopSupported()) {
                buttons.add(openButt);
            }
            firstTime = false;
        } catch (FileNotFoundException f) {
            popupErr("Error: file \"" + currFile + "\" could not be opened.");
        } catch (IOException e) {
            popupErr("Error while retrieving file text.");
        }
    }

    /** Uses the Apache POI API to extract the text from a Microsoft Office file.
     *  If currFile is not of any recognized Office format, will return null. */
    private String wordDocText() {
        StringBuilder str = new StringBuilder();
        try {
            POITextExtractor extractor = ExtractorFactory.createExtractor(currFile);
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
        return status;
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
                popupErr("Error: no default application for file type \"." + fileType + "\"");
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
                control.play();
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

    // ----------------------------- Panels -------------------------------

    class FileMenuPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(new Color(175, 238, 238));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    class ButtonsPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(bckgrndClr);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

}
