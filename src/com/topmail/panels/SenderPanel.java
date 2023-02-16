package com.topmail.panels;

import com.secretlib.util.Log;
import com.topmail.events.*;
import com.topmail.exceptions.NoEmailException;
import com.topmail.exceptions.NoRecipientException;
import com.topmail.sender.MailSender;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.GeneralSecurityException;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;

public class SenderPanel extends JPanel implements TopEventListener {
    private static final Log LOG = new Log(SenderPanel.class);

    JTextField txtSubject;
    MessagePanel msgPanel;

    MailSender sender;

    public SenderPanel(MessagePanel msgPanel) {
        this.msgPanel = msgPanel;
        sender = new MailSender();
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
                try {
                    TopEventDispatcher.dispatch(new TopEventDataSaving());
                    try {
                        sender.sendTest();
                    } catch (GeneralSecurityException ex) {
                        // TODO : log
                    }
                } catch (NoRecipientException ex) {
                    LOG.debug("NoRecipientException");
                    //
                } catch (NoEmailException ex) {
                    LOG.debug("NoEmailException");
                    //
                }
            }
        });


        JButton btnSend = new JButton("Send");
        add(btnSend);

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TopEventDispatcher.dispatch(new TopEventDataSaving());
                    try {
                        sender.send();
                    } catch (GeneralSecurityException ex) {
                        // TODO : log
                    }
                } catch (NoRecipientException ex) {
                    LOG.debug("NoRecipientException");
                    //
                } catch (NoEmailException ex) {
                    LOG.debug("NoEmailException");
                    //
                }
            }
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
