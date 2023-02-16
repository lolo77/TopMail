package com.topmail.panels;

import com.secretlib.exception.NoBagException;
import com.secretlib.io.stream.HiDataAbstractInputStream;
import com.secretlib.io.stream.HiDataStreamFactory;
import com.secretlib.model.AbstractChunk;
import com.secretlib.model.ChunkData;
import com.secretlib.model.HiDataBag;
import com.secretlib.util.HiUtils;
import com.secretlib.util.Log;
import com.secretlib.util.Parameters;
import com.topmail.Main;
import com.topmail.exceptions.NoEmailException;
import com.topmail.exceptions.NoRecipientException;
import com.topmail.model.DataRepository;
import com.topmail.model.Env;
import com.topmail.model.Settings;
import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableRow;
import com.topmail.transfert.data.TableCell;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;

public class CheckPanel extends JPanel {

    private static final Log LOG = new Log(CheckPanel.class);

    private JTextField txtAttachment;

    private JTextArea txtResult;

    public CheckPanel() {
        init();
    }


    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel panBrowse = new JPanel();
        add(panBrowse);
        JLabel lbl = new JLabel(Main.getString("lbl.check.browse"));
        panBrowse.add(lbl);
        JButton btnBrowse = new JButton(Main.getString("btn.check.browse"));
        panBrowse.add(btnBrowse);
        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onBrowse();
            }
        });

        txtAttachment = new JTextField(30);
        txtAttachment.setEditable(false);
        txtAttachment.setEditable(true);
        add(txtAttachment);

        JLabel lblResult = new JLabel(getString("lbl.check.result"));
        add(lblResult);

        txtResult = new JTextArea(30,10);
        txtResult.setEditable(false);
        txtResult.setEditable(true);
        add(txtResult);
    }


    private void onBrowse() {
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
            check(f);
        }
    }


    private void check(File f) {
        txtAttachment.setText(f.getName());
        txtResult.setText("");
        boolean bFound = false;
        try {
            InputStream is = new FileInputStream(f);
            HiDataAbstractInputStream stream = HiDataStreamFactory.createInputStream(is, getEnv().getParams());
            if (stream == null) {
                throw new NoBagException();
            }
            HiDataBag bag = stream.getBag();
            bag.decryptAll(getEnv().getParams());
            for (AbstractChunk c : bag.getItems()) {
                if (c instanceof ChunkData) {
                    ChunkData cd = (ChunkData) c;
                    if ((cd.getName() != null) && (cd.getName().equals(DataRepository.CHUNK_SECRET))) {
                        TableRow r = decode(cd);
                        if (r != null) {
                            updateResult(r);
                            bFound = true;
                            break;
                        }
                    }
                }
            }
            if (!bFound) {
                updateResult(null);
            }
        } catch (FileNotFoundException e) {
            txtResult.setText(getString("lbl.check.ioError"));
        } catch (NoRecipientException e) {
            txtResult.setText(getString("lbl.check.noRecipient"));
        } catch (NoEmailException e) {
            txtResult.setText(getString("lbl.check.noEmail", DataRepository.MAILING_KEY_EMAIL));
        } catch (NoBagException e) {
            txtResult.setText(getString("lbl.check.noBag"));
        } catch (Exception e) {
            txtResult.setText(getString("lbl.check.genericError", e.getMessage()));
        }
    }


    private TableRow decode(ChunkData cd) throws NoRecipientException, NoEmailException {
        byte[] hash = cd.getData();
        Table tbl = getEnv().getRepo().getMailingList();
        if (tbl.getRows().size() < 2) {
            throw new NoRecipientException();
        }
        TableRow header = tbl.getRows().get(0);
        int iCell = 0;
        while (iCell < header.getCells().size()) {
            TableCell c = header.getCells().get(iCell);
            if ((c.getValue() != null) && (c.getValue().equals(DataRepository.MAILING_KEY_EMAIL))) {
                break;
            }
            iCell++;
        }
        if (iCell >= header.getCells().size()) {
            throw new NoEmailException();
        }
        Parameters p = getEnv().getParams();
        for (TableRow r : tbl.getRows()) {
            String email = r.getCells().get(iCell).getValue();
            email += getEnv().getParams().getHashAlgo() + getEnv().getParams().getKm() + getEnv().getParams().getKd();
            byte[] hTest = HiUtils.genHash(email.getBytes(StandardCharsets.UTF_8), p.getHashAlgo());
            if (HiUtils.arraysEquals(hash, 0, hTest, 0, hash.length)) {
                return r;
            }
        }
        return null;
    }


    private void updateResult(TableRow r) {
        if (r == null) {
            txtResult.setText(Main.getString("lbl.check.notfound"));
        } else {
            Table tbl = getEnv().getRepo().getMailingList();
            TableRow header = tbl.getRows().get(0);

            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < header.getCells().size(); i++) {
                sb.append(header.getCells().get(i).getValue());
                sb.append(" : ");
                sb.append(r.getCells().get(i).getValue());
                sb.append("\n");
            }
            txtResult.setText(sb.toString());
        }
    }
}
