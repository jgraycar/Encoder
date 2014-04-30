package enc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.lang.StringBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * @author Joel Graycar
 */

public class EncoderGUI {

    JFrame frame, pop;
    JFileChooser fileChooser;
    Encoder enc;
    FilePanel filePanel;
    String[] files;
    StringBuilder text;
    File currFile, saveFile;
    String fileName;
    JTextArea fileText;
    ButtonsPanel buttons;
    JButton encButt, decButt, openButt, closeButt;
    boolean nameSet;
    // state: 0 is encrypt, 1 decrypt
    int state;

    public static void main(String... args) {
        EncoderGUI gui = new EncoderGUI();
        gui.enc = new Encoder();
        gui.go();
    }

    public void go() {
        frame = new JFrame();
        filePanel = new FilePanel();
        fileChooser = new JFileChooser();
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
        frame.setSize(1000, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(176, 224, 230));
        frame.setVisible(true);
    }

    private void popup(String msg) {
        pop = new JFrame();
        JPanel popPanel = new JPanel();
        JLabel msgLabel = new JLabel(msg + "   ");
        closeButt = new JButton("Ok");
        closeButt.addActionListener(new CloseButtonListener());
        msgLabel.setFont(new Font("serif", Font.BOLD, 20));
        popPanel.setLayout(new BoxLayout(popPanel, BoxLayout.X_AXIS));
        popPanel.add(Box.createHorizontalGlue());
        popPanel.add(msgLabel);
        popPanel.add(closeButt);
        popPanel.add(Box.createHorizontalGlue());
        pop.getContentPane().add(BorderLayout.CENTER, popPanel);
        pop.setSize(msg.length() * 10 + 75, 200);
        pop.setLocationRelativeTo(null);
        pop.setVisible(true);
        pop.getRootPane().setDefaultButton(closeButt);
    }

    private void queryName() {
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            saveFile = fileChooser.getSelectedFile();
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
            popup("Error: file \"" + currFile + "\" is not encrypted.");
            restoreText();
        } else if (status == 2) {
            popup("Error: encountered IOException.");
        } else if (status == 0) {
            popup("\"" + fileChooser.getName(currFile) + tail.toString());
            currFile = saveFile;
            fileChosen();
        }
    }

    private void restoreText() {
        try {
            FileWriter fileW = new FileWriter(currFile);
            fileW.write(text.toString());
            fileW.close();
            boolean deleted = saveFile.delete();
            if (!deleted) {
                popup("Error: corrupted file could not be deleted.");
            }
        } catch (IOException io) {
            popup("Error: could not restore text.");
        }
    }

    private void fileChosen() {
        try {
            BufferedReader file =
                new BufferedReader(new FileReader(currFile));
            text = new StringBuilder();
            try {
                for (String line = file.readLine(); line != null; line = file.readLine()) {
                    text.append(line + "\n");
                }
                file.close();
                fileText.setText(text.toString());
            } catch (IOException io) {
                popup("Error while retrieving file text.");
            }
        } catch (FileNotFoundException f) {
            popup("Error: file \"" + currFile + "\" not found.\n");
        }
    }

    private int transformText() {
        int status = 0;
        String transformed = "";
        if (state == 1) {
            try {
                transformed = enc.decode(text.toString());
            } catch (NumberFormatException num) {
                status = 1;
            }
        } else {
            transformed = enc.encode(text.toString());
        }
        try {
            FileWriter fileW = new FileWriter(saveFile);
            fileW.write(transformed);
            fileW.close();
        } catch (IOException io) {
            status = 2;
        }
        return status;
    }

    // ---------------------------- Listeners -----------------------------

    class CloseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            pop.dispatchEvent(new WindowEvent(pop, WindowEvent.WINDOW_CLOSING));
        }
    }

    class OpenButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                currFile = fileChooser.getSelectedFile();
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
