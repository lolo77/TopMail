package com.topmail.panels;

import com.secretlib.util.Log;
import com.topmail.events.*;
import net.atlanticbb.tantlinger.shef.HTMLEditorPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import static com.topmail.Main.getEnv;

public class MessagePanel extends JPanel implements TopEventListener {

    private static final Log LOG = new Log(MessagePanel.class);

    private HTMLEditorPane editor;

    public MessagePanel() {
        TopEventDispatcher.addListener(this);
        setLayout(new BorderLayout());

        JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split1.setContinuousLayout(true);
        add(split1, BorderLayout.CENTER);
        SenderPanel senderPanel = new SenderPanel(this);
        split1.setTopComponent(senderPanel);
        senderPanel.setMinimumSize(new Dimension(100, 80));
        /**
         * Using the EXCELLENT SHEF-master HTMLEditorPane !
         * @author Bob Tantlinger
         */
        JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split2.setContinuousLayout(true);
        split1.setBottomComponent(split2);
        editor = new HTMLEditorPane(true);
        split2.setTopComponent(editor);
        split2.setDividerLocation(300);
        AttachmentPanel attachmentPanel = new AttachmentPanel();
        split2.setBottomComponent(attachmentPanel);
        attachmentPanel.setMinimumSize(new Dimension(100, 100));
    }

    public String getHTML() {
        return editor.getText();
    }

    public void updateEnv() {
        getEnv().getRepo().setBody(editor.getText());
    }

    @Override
    public void processTopEvent(TopEventBase e) {
        if (e instanceof TopEventDataSaving) {
            updateEnv();
        }
        if (e instanceof TopEventDataLoaded) {
            editor.setText(getEnv().getRepo().getBody());
        }
        if (e instanceof TopEventAttachmentChanged) {
            revalidate();
            repaint();
        }
    }
}
