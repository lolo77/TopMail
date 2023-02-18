package com.topmail;

import com.secretlib.util.HiUtils;
import com.secretlib.util.Log;
import com.topmail.panels.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;


/**
 * @author Florent FRADET
 * <p>
 * Main Swing GUI class
 */
public class MainPanel extends JPanel {

    private static final Log LOG = new Log(MainPanel.class);

    JFrame frame;

    JTabbedPane tabbedPane;


    public MainPanel(JFrame frame) {
        this.frame = frame;
    }

    public void onAbout() {

        JDialog dlg = new JDialog(frame, getString("about.title"), true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane();
        dlg.add(scroll, BorderLayout.CENTER);
        JPanel p = new JPanel();
        dlg.add(p, BorderLayout.NORTH);
        p.setLayout(new BorderLayout());
        JLabel img = new JLabel();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("topsecret_logo.png");
            img.setIcon(new ImageIcon(ImageIO.read(is).getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
            is.close();
        } catch (IOException e) {
        }
        p.add(img, BorderLayout.WEST);
        String sMsg = getString("about.message");
        String sHtml = "<html><div style='width:100%;text-align:center'>";
        sHtml += getString("about.message");
        sHtml += "<br/><br/>";
        sHtml += getString("about.author") + " Florent FRADET<br/><a href='mailto:top-secret.dao@ud.me'>top-secret.dao@ud.me</a>";
        sHtml += "<br/><br/>";
        sHtml += getString("about.link.github") + "<br/><a href='https://github.com/lolo77'>https://github.com/lolo77</a>";
        sHtml += "<br/><br/>";
        sHtml += getString("about.link.site") + "<br/><a href='http://top-secret.dao'>top-secret.dao</a>";
        sHtml += "<br/><br/>";
        sHtml += getString("about.link.telegram") + "<br/><a href='https://t.me/s/topsecret_projects'>topsecret_projects</a>";
        sHtml += "<br/><br/>";
        sHtml += getString("about.link.paypal") + "<br/><a href='https://www.paypal.com/donate/?hosted_button_id=BVBEEHRLYHFH6'>Paypal</a>";
        sHtml += "<br/><br/>";
        sHtml += "</div></html>";
        JEditorPane lblCopy = new JEditorPane("text/html", sHtml);
        p.add(lblCopy, BorderLayout.CENTER);
        lblCopy.setEditable(false);
        lblCopy.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        // NO OP
                    }
                }
            }
        });
        lblCopy.setFont(getFont().deriveFont(Font.BOLD, 15));
        String s = "Visit *** https://github.com/lolo77 *** for more features !\n\n";
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("legal.txt");
            s += new String(HiUtils.readAllBytes(is));
            is.close();
        } catch (IOException e) {

        }
        JTextArea lbl = new JTextArea(s);
        lbl.setEditable(false);
        scroll.setViewportView(lbl);

        dlg.setSize(800, 600);
        dlg.toFront();
        dlg.setVisible(true);
    }

    public void initialize() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        DataPanel datapan = new DataPanel();
        tabbedPane.add(getString("tab.data"), datapan);

        SettingsPanel settingsPanel = new SettingsPanel();
        tabbedPane.add(getString("tab.settings"), settingsPanel);

        MailingPanel mailpan = new MailingPanel();
        tabbedPane.add(getString("tab.mailing"), mailpan);

        MessagePanel msgpan = new MessagePanel();
        tabbedPane.add(getString("tab.message"), msgpan);

        ReportPanel reportpan = new ReportPanel();
        tabbedPane.add(getString("tab.report"), reportpan);

        CheckPanel checkpan = new CheckPanel();
        tabbedPane.add(getString("tab.check"), checkpan);

        JButton btnAbout = new JButton(getString("btn.about"));
        add(btnAbout, BorderLayout.SOUTH);
        btnAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAbout();
            }
        });
    }



}
