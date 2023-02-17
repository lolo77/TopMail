package com.topmail.panels;

import com.secretlib.util.Log;
import com.topmail.events.*;
import com.topmail.exceptions.NoEmailException;
import com.topmail.exceptions.NoRecipientException;
import com.topmail.sender.MailSender;

import javax.mail.MessagingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;
import static java.awt.Frame.getFrames;

public class SenderPanel extends JPanel implements TopEventListener {
    private static final Log LOG = new Log(SenderPanel.class);

    JTextField txtSubject;
    MessagePanel msgPanel;


    public SenderPanel(MessagePanel msgPanel) {
        this.msgPanel = msgPanel;
        TopEventDispatcher.addListener(this);
        setLayout(new FlowLayout());
        JLabel lblSubject = new JLabel(getString("message.subject"));
        add(lblSubject);
        txtSubject = new JTextField(30);
        add(txtSubject);
        JButton btnSendTest = new JButton("Send Test");
        add(btnSendTest);

        btnSendTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSend(true);
            }
        });


        JButton btnSend = new JButton("Send");
        add(btnSend);

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSend(false);
            }
        });
    }

    private void onSend(boolean test) {
        TopEventDispatcher.dispatch(new TopEventDataSaving());
        SwingUtilities.invokeLater(() -> {
            SenderDialog dlg = new SenderDialog(getFrames()[0], test);
        });
    }

    @Override
    public void processTopEvent(TopEventBase e) {
        if (e instanceof TopEventDataSaving) {
            getEnv().getRepo().setSubject(txtSubject.getText());
        }
        if (e instanceof TopEventDataLoaded) {
            txtSubject.setText(getEnv().getRepo().getSubject());
        }
    }
}
