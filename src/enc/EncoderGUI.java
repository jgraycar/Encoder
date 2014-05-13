package enc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

    JFrame frame;
    FileDialog fileChooser;
    JFileChooser jFile;
    Encoder enc;
    FilePanel filePanel;
    String[] files;
    StringBuilder text;
    File currFile, saveFile;
    String fileName;
    JLabel fileNameLabel;
    JPanel fileNamePanel;
    JTextArea fileText;
    ButtonsPanel buttons;
    JButton encButt, decButt, openButt;
    boolean nameSet;
    // state: 0 is encrypt, 1 decrypt
    int state;
    int[] bytes;
    

    public static void main(String... args) {
        EncoderGUI gui = new EncoderGUI();
        gui.enc = new Encoder();
        gui.go();
    }

    public void go() {
        frame = new JFrame();
        filePanel = new FilePanel();
        fileChooser = new FileDialog(frame);
        jFile = new JFileChooser();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        frame.getContentPane().add(BorderLayout.CENTER, filePanel);

        // Set up enc/dec & open buttons
        encButt = new JButton("Encrypt");
        decButt = new JButton("Decrypt");
        openButt = new JButton("Open file");
        encButt.addActionListener(new EncryptButtonListener());
        decButt.addActionListener(new DecryptButtonListener());
        openButt.addActionListener(new OpenButtonListener());
        encButt.setOpaque(true);
        decButt.setOpaque(true);
        openButt.setOpaque(true);
        encButt.setBackground(new Color(176, 224, 230));
        decButt.setBackground(new Color(176, 224, 230));
        openButt.setBackground(new Color(176, 224, 230));
        buttons = new ButtonsPanel();
        buttons.add(encButt);
        buttons.add(decButt);
        buttons.add(openButt);
        filePanel.add(buttons);

        // Set up text field
        fileText = new JTextArea(45, 50);
        fileText.setEditable(false);
        JScrollPane scroller = new JScrollPane(fileText);
        fileText.setLineWrap(false);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
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
    }

    private void popup(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Success!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void popupErr(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void queryName() {
        fileChooser.setMode(FileDialog.SAVE);
        fileChooser.setVisible(true);
        File[] files = fileChooser.getFiles();
        if (files.length > 0) {
            saveFile = files[0];
            doAction();
        }
    }

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
            popupErr("Error: file \"" + fileName + "\" is not encrypted.");
        } else if (status == 2) {
            popupErr("Error: encountered IOException.");
        } else if (status == 0) {
            popup("\"" + fileName + tail.toString());
            currFile = saveFile;
            fileChosen();
        }
    }

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
            fileText.setText(new String(realBytes, "MacRoman"));
            fileName = jFile.getName(currFile);
            fileNameLabel.setText(fileName);
        } catch (FileNotFoundException f) {
            popup("Error: file \"" + currFile + "\" not found.\n");
        } catch (IOException e) {
            popup("Error while retrieving file text.");
        }
    }

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

    class OpenButtonListener implements ActionListener {
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

    // ----------------------------- Panels -------------------------------

    class FileMenuPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(new Color(175, 238, 238));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

    class FilePanel extends JPanel {
        public void paintComponent(Graphics g) {

        }
    }

    class ButtonsPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(new Color(176, 224, 230));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }

}
