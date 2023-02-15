package com.topmail.panels;

import com.secretlib.io.stream.HiDataStreamFactory;
import com.secretlib.model.ChunkData;
import com.secretlib.util.Log;
import com.topmail.Main;
import com.topmail.events.TopEventAttachmentChanged;
import com.topmail.events.TopEventDispatcher;
import com.topmail.sender.MailSender;
import com.topmail.util.Utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.topmail.Main.getEnv;

public class Attachment extends JPanel {

    private static final Log LOG = new Log(Attachment.class);


    JTextField txtName;
    JTextField txtPath;
    JLabel lblSize;
    String filePath;

    ChunkData cd;

    public Attachment() {
        init();
    }

    public Attachment(ChunkData cd) throws IOException {
        this.cd = cd;
        init();
        txtName.setText(cd.getName());
        setFilePath(new String(cd.getData(), StandardCharsets.UTF_8));
    }

    protected void init() {
        setLayout(new FlowLayout());
        txtName = new JTextField(10);
        add(txtName);
        txtName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                chg();
            }
            public void removeUpdate(DocumentEvent e) {
                chg();
            }
            public void insertUpdate(DocumentEvent e) {
                chg();
            }

            public void chg() {
                cd.setName(txtName.getText());
            }
        });

        lblSize = new JLabel("", SwingConstants.CENTER);
        add(lblSize);
        updateSize(null);
        JButton browse = new JButton(Main.getString("btn.attachment.browse"));
        add(browse);

        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onBrowse();
            }
        });

        JButton remove = new JButton(Main.getString("btn.attachment.remove"));
        add(remove);

        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getEnv().getRepo().removeAttachment(cd);
                TopEventDispatcher.dispatch(new TopEventAttachmentChanged());
            }
        });

        txtPath = new JTextField(30);
        txtPath.setEditable(false);
        txtPath.setEnabled(true);
        add(txtPath);
        Dimension size = new Dimension(250, 40);
        setMinimumSize(size);
        setPreferredSize(size);
        size = new Dimension(1500, 100);
        setMaximumSize(size);
    }

    protected void onBrowse() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (getEnv().getCfg().getLastOpenDir() != null) {
            File lastDir = new File(getEnv().getCfg().getLastOpenDir());
            fileChooser.setCurrentDirectory(lastDir);
        }

        // Show save file dialog
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            try {
                setFile(f);
                String path = f.getPath();
                getEnv().getCfg().setLastOpenDir(path);
            } catch (IOException e) {
                // No op
            }
        }
    }

    public void setFilePath(String s) throws IOException {
        setFile(new File(s));
    }

    protected void updateSize(Long size) {
        if (size == null) {
            size = 0L;
        }
        String sSize = Main.getString("format.size", size);
        lblSize.setText(sSize);
    }

    protected void updateName(String name) {
        cd.setName(name);
        txtName.setText(name);
    }

    public void setFile(File f) throws IOException {
        if (f.exists() && f.isFile()) {
            filePath = f.getAbsolutePath();
            txtPath.setText(filePath);
            if ((cd.getName() == null) || (cd.getName().length() == 0)) {
                updateName(f.getName());
            }
            updateSize(Files.size(Paths.get(filePath)));
            String ext = Utils.getFileExt(f);
            boolean bExtOk = (HiDataStreamFactory.isSupportedInputExtension(ext)
                    && HiDataStreamFactory.isSupportedOutputExtension(ext));
            cd.setData(filePath.getBytes(StandardCharsets.UTF_8));
            setBorder(new LineBorder(bExtOk ? Color.GREEN : Color.BLUE, 3, true));
        } else {
            filePath = null;
            txtPath.setText("");
            cd.setData("".getBytes(StandardCharsets.UTF_8));
            updateSize(null);
            setBorder(new LineBorder(Color.RED, 3, true));
        }
    }

}
