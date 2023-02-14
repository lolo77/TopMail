package com.topmail.panels;

import com.topmail.events.*;

import javax.swing.*;

import java.awt.*;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;

public class SenderPanel extends JPanel implements TopEventListener {

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
        JButton btnSend = new JButton("Send");
        add(btnSend);
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
