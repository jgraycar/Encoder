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

    JFrame frame, nameInquiry;
    JFileChooser fileChooser;
    Encoder enc;
    FilePanel filePanel;
    String[] files;
    StringBuilder text;
    File currFile;
    String fileName;
    JTextArea fileText;
    ButtonsPanel buttons;
    JButton encButt, decButt, openButt;
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
        fileChooser.addActionListener(new FileChooserListener());
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

    private void setFileText(StringBuilder textBuilder) {
        fileText.setText(textBuilder.toString());
        text = textBuilder;
    }

    private void popup(String msg) {
        JFrame pop = new JFrame();
        JPanel popPanel = new JPanel();
        JLabel msgLabel = new JLabel(msg);
        msgLabel.setFont(new Font("serif", Font.BOLD, 20));
        popPanel.setLayout(new BoxLayout(popPanel, BoxLayout.X_AXIS));
        popPanel.add(Box.createHorizontalGlue());
        popPanel.add(msgLabel);
        popPanel.add(Box.createHorizontalGlue());
        pop.getContentPane().add(BorderLayout.CENTER, popPanel);
        pop.setSize(400, 200);
        pop.setLocationRelativeTo(null);
        pop.setVisible(true);
    }

    private void queryName() {
        nameInquiry = new JFrame();
        JLabel question = new JLabel("Save as: ");
        JPanel queryPanel = new JPanel();
        queryPanel.add(question);
        JTextField response = new JTextField(20);
        response.addActionListener(new NamePromptListener());
        queryPanel.add(response);
        response.requestFocus();
        nameInquiry.add(queryPanel);
        nameInquiry.setSize(300, 100);
        nameInquiry.setLocationRelativeTo(null);
        nameInquiry.setVisible(true);
    }

    private void doAction() {
        StringBuilder newText = new StringBuilder();
        StringBuilder tail = new StringBuilder();
        //tail.append(" successfully encrypted!");
        if (state == 1) {
            //tail.replace(0, tail.length(), " successfully decrypted!");
            tail.append(" successfully decrypted!");
        } else {
            tail.append(" successfully encrypted!");
        }
        int status = transformText();
        if (status == 1) {
            popup("Error: file " + currFile + " is not encrypted.");
        } else if (status == 2) {
            popup("Error: encountered IOException.");
        } else if (status == 0) {
            popup(fileChooser.getName(currFile) + tail.toString());
        }
    }

    private void fileChosen() {
        currFile = fileChooser.getSelectedFile();
        try {
            BufferedReader file =
                new BufferedReader(new FileReader(currFile));
            StringBuilder textBuilder = new StringBuilder();
            try {
                for (String line = file.readLine(); line != null; line = file.readLine()) {
                    textBuilder.append(line + "\n");
                }
                file.close();
                setFileText(textBuilder);
            } catch (IOException io) {
                popup("Error while retrieving file text.");
            }
        } catch (FileNotFoundException f) {
            popup("Error: file " + currFile + " not found.\n");
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
            FileWriter fileW = new FileWriter(new File(currFile.getParent(), fileName));
            fileW.write(transformed);
            fileW.close();
        } catch (IOException io) {
            status = 2;
        }
        return status;
    }

    // ---------------------------- Listeners -----------------------------

    class OpenButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                fileChosen();
            }
        }
    }

    class FileChooserListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            currFile = fileChooser.getSelectedFile();
            try {
                BufferedReader file = new BufferedReader(new FileReader(currFile));
                StringBuilder text = new StringBuilder();
                try {
                    for (String line = file.readLine(); line != null; line = file.readLine()) {
                        text.append(line + "\n");
                    }
                    file.close();
                    setFileText(text);
                } catch (IOException io) {
                    popup("Error while retrieving file text.");
                }
            } catch (FileNotFoundException f) {
                popup("Error: file " + currFile + " not found.\n");
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

    class NamePromptListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JTextField label = (JTextField) event.getSource();
            fileName = label.getText();
            nameSet = true;
            doAction();
            nameInquiry.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
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
