package com.topmail.panels;

import com.secretlib.util.Log;
import com.topmail.exceptions.NoEmailException;
import com.topmail.sender.MailSender;
import com.topmail.sender.SenderState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;

public class SenderDialog extends JDialog {

    private static final Log LOG = new Log(SenderDialog.class);

    MailSender sender;
    InternalPanel panel;

    SenderState state;

    boolean test = false;

    Thread daemon;

    private class InternalPanel extends JPanel {

        JLabel lblStatus;
        JButton btnConfirm;
        JButton btnCancel;
        int nbEmails;

        public InternalPanel() {

            nbEmails = 1;
            if (!test) {
                nbEmails = getEnv().getRepo().getMailingList().getRows().size() - 2;
            }

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            lblStatus = new JLabel(getString("lbl.sender.confirmation", String.valueOf(nbEmails)));
            add(lblStatus);

            JPanel panelBtn = new JPanel();
            add(panelBtn);
            btnConfirm = new JButton(getString("btn.sender.start"));
            panelBtn.add(btnConfirm);
            btnConfirm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    btnConfirm.setEnabled(false);
                    btnCancel.setText(getString("btn.sender.interrupt"));
                    onStart();
                }
            });

            btnCancel = new JButton(getString("btn.sender.cancel"));
            panelBtn.add(btnCancel);
            btnCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            });

            if (getEnv().getRepo().getMailingList().getRows().size() < 2) {
                state.setEnded(true);
                state.setException(new NoEmailException());
                btnConfirm.setEnabled(false);
                updateState(state);
            }

        }


        public void updateState(SenderState state) {
            if (state.isEnded()) {
                String msg = getString("lbl.sender.terminated");
                if (state.isInterruptionRequested()) {
                    msg = getString("lbl.sender.interrupted");
                }
                if (state.getException() != null) {
                    Exception e = state.getException();
                    msg = getString("lbl.sender.exception." + e.getClass().getSimpleName(), e.getMessage());
                }
                lblStatus.setText(msg);
                btnCancel.setText(getString("btn.sender.close"));

            } else if (state.isRunning()) {
                String msg = getString("lbl.sender.progress", String.valueOf(state.getNbSent()), String.valueOf(nbEmails));
                lblStatus.setText(msg);
            }
        }

    }


    public SenderDialog(Frame owner, boolean test) {
        super(owner, true);
        this.test = test;
        state = new SenderState();
        sender = new MailSender(state);
        setTitle(getString("lbl.sender.title"));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        panel = new InternalPanel();
        setContentPane(panel);
        pack();

        daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.debug("Thread started");
                while ((!state.isInterruptionRequested()) && (!state.isEnded())) {
                    try {
                        Thread.sleep(1000);
                        LOG.debug("Thread running : " + state.getNbSent());
                        panel.updateState(state);
                    } catch (InterruptedException e) {
                        // NO op
                    }
                }
                panel.updateState(state);
                LOG.debug("Thread terminated");
            }
        });

        daemon.start();

        setVisible(true);
    }

    private void onStart() {
        LOG.debug("onStart");
        new Thread(new Runnable() {
            @Override
            public void run() {
                send();
            }
        }).start();
    }

    private void onCancel() {
        LOG.debug("onCancel");
        if ((state.isInterruptionRequested()) ||
                ((state.isEnded()) && (!state.isInterruptionRequested())) ||
                ((!state.isRunning()) && (!state.isInterruptionRequested()))
        ) {
            state.setInterruptionRequested(true);
            dispose();
        } else {
            state.setInterruptionRequested(true);
            while (state.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // No op
                }
            }
            panel.updateState(state);
        }
    }

    private void send() {
        try {
            state.setRunning(true);
            if (test) {
                sender.sendTest();
            } else {
                sender.send();
            }
            // Simulate
          /*  for (int i = 0; i < 5; i++) {
                if (state.isInterruptionRequested()) {
                    break;
                }
                LOG.debug("Sending message " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // No op
                }
                state.setNbSent(state.getNbSent() + 1);
            }*/
        } catch (Exception e) {
            state.setException(e);
        } finally {
            state.setEnded(true);
            state.setRunning(false);
        }
    }
}
