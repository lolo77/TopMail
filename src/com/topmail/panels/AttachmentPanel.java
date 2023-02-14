package com.topmail.panels;

import com.secretlib.model.ChunkData;
import com.secretlib.util.Log;
import com.topmail.Main;
import com.topmail.events.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;

public class AttachmentPanel extends JPanel implements TopEventListener {
    private static final Log LOG = new Log(AttachmentPanel.class);

    JPanel attachmentsPanel;

    public AttachmentPanel() {
        TopEventDispatcher.addListener(this);
        init();
        updateAttachments();
    }

    public void updateAttachments() {
        attachmentsPanel.removeAll();
        for (ChunkData cd : getEnv().getRepo().getAttachments()) {
            try {
                attachmentsPanel.add(new Attachment(cd));
            } catch (IOException e) {
                LOG.warn("Attachment not found : " + cd.getName());
                LOG.warn("Path : " + new String(cd.getData(), StandardCharsets.UTF_8));
                // No op
            }
        }
    }

    public void init() {
        setLayout(new BorderLayout());
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        JPanel panelAdd = new JPanel();
        panelAdd.setLayout(new BorderLayout());
        contentPane.add(panelAdd, BorderLayout.NORTH);

        JButton btnAdd = new JButton(Main.getString("btn.attachment.add"));
        panelAdd.add(btnAdd);
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChunkData cd = getEnv().getRepo().addAttachment();
                TopEventDispatcher.dispatch(new TopEventAttachmentChanged());
            }
        });

        attachmentsPanel = new JPanel();
        attachmentsPanel.setLayout(new BoxLayout(attachmentsPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(attachmentsPanel);
        contentPane.add(scroll, BorderLayout.CENTER);
    }


    @Override
    public void processTopEvent(TopEventBase e) {
        if ((e instanceof TopEventDataLoaded) || (e instanceof TopEventAttachmentChanged)) {
            updateAttachments();
        }
    }
}
