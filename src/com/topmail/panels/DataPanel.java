package com.topmail.panels;

import com.secretlib.exception.NoBagException;
import com.secretlib.exception.TruncatedBagException;
import com.secretlib.io.stream.HiDataAbstractInputStream;
import com.secretlib.io.stream.HiDataAbstractOutputStream;
import com.secretlib.io.stream.HiDataStreamFactory;
import com.secretlib.model.HiDataBag;
import com.secretlib.model.IProgressCallback;
import com.secretlib.model.ProgressMessage;
import com.secretlib.model.ProgressStepEnum;
import com.secretlib.util.Log;
import com.secretlib.util.Parameters;
import com.topmail.events.*;
import com.topmail.model.Config;
import com.topmail.util.SpringUtilities;
import com.topmail.util.Utils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;

public class DataPanel extends JPanel implements TopEventListener {
    private static final Log LOG = new Log(DataPanel.class);

    JButton btnSelectCover;
    JButton btnLoad;
    JButton btnSave;

    JPasswordField passMaster;
    JPasswordField passData;
    JComboBox<String> algo;

    JProgressBar progress;

    private int defaultAlgoIdx;


    private class ProgressCB implements IProgressCallback {

        private final HashMap<ProgressStepEnum, String> msgs = new HashMap<>();
        private ProgressMessage lastMsg = null;

        public ProgressCB() {
            msgs.put(ProgressStepEnum.DECODE, getString("process.decode"));
            msgs.put(ProgressStepEnum.ENCODE, getString("process.encode"));
            msgs.put(ProgressStepEnum.READ, getString("process.read"));
            msgs.put(ProgressStepEnum.WRITE, getString("process.write"));
        }

        @Override
        public void update(ProgressMessage progressMessage) {
            lastMsg = progressMessage;

            SwingUtilities.invokeLater(() -> {
                if (!progress.isVisible()) {
                    progress.setVisible(true);
                }
                progress.setValue((int) (progressMessage.getProgress() * 100.0));
                TopEventDispatcher.dispatch(new TopEventProgressStateChanged(msgs.get(progressMessage.getStep())));
            });
        }

        public ProgressMessage getLastMsg() {
            return lastMsg;
        }
    }


    public int getSpaceCapacity() {
        int cap = 0;
        int[] spaceCapacity = getEnv().getSpaceCapacity();
        for (int i = 0; i <= 7; i++) {
            if (spaceCapacity[i] == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            cap += spaceCapacity[i];
        }
        return cap;
    }


    public DataPanel() {
        TopEventDispatcher.addListener(this);
        Arrays.fill(getEnv().getSpaceCapacity(), 0);
        initialize();
    }


    public boolean validateInputs() {

        String sAlgo = (String) algo.getSelectedItem();
        if (!Utils.isAlgoSupported(sAlgo)) {
            algo.setSelectedIndex(defaultAlgoIdx);
        }

        return true;
    }

    private Parameters buildParams() {
        Parameters p = new Parameters();
        ProgressCB progCB = new ProgressCB();
        p.setProgressCallBack(progCB);
        p.setKm(String.copyValueOf(passMaster.getPassword()));
        p.setKd(String.copyValueOf(passData.getPassword()));
        p.setHashAlgo((String) algo.getSelectedItem());
        p.setBitStart(0);
        p.setAutoExtendBit(true);

        getEnv().setParams(p);

        return p;
    }

    private void loadSource(File file) throws TruncatedBagException {
        Parameters p = buildParams();

        SwingUtilities.invokeLater(() -> {
            progress.setMinimum(0);
            progress.setMaximum(100);
            progress.setValue(0);
            progress.setVisible(true);
        });
        FileInputStream fis = null;
        boolean truncated = false;
        try {
            fis = new FileInputStream(file);
            HiDataAbstractInputStream hdis = HiDataStreamFactory.createInputStream(fis, p);
            if (hdis == null)
                throw new Exception(getString("input.err.file.format", file.getName()));
            HiDataBag newBag = hdis.getBag();
            if (!newBag.isEmpty()) {
                // Replace the displayed bag by the one found in the source image
                if (newBag.verifyHash()) {
                    newBag.decryptAll(p);
                    getEnv().getRepo().load(newBag);
                    getEnv().setInputFile(file);

                    if (getEnv().isShowDlg()) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this,
                                    getString("input.info.found"),
                                    getString("input.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                } else {
                    throw new NoBagException();
                }
            }
        } catch (TruncatedBagException e) {
            truncated = true;
        } catch (NoBagException | IllegalArgumentException e) {
            // No bag
            if (getEnv().isShowDlg()) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            getString("input.info.notFound"),
                            getString("input.title"),
                            JOptionPane.INFORMATION_MESSAGE);
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    getString("input.err.file.data", e.getMessage()),
                    getString("input.title"),
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // NO OP
                }
            }
        }
        SwingUtilities.invokeLater(() -> {
            ProgressCB progCB = (ProgressCB) p.getProgressCallBack();
            if (progCB.getLastMsg() != null) {
                getEnv().setSpaceCapacity(progCB.getLastMsg().getNbBitsTotals());
            }
            progress.setVisible(false);
            TopEventDispatcher.dispatch(new TopEventProgressStateChanged(file.getName()));
        });
        if (truncated)
            throw new TruncatedBagException();
    }


    public void onOpenSource() {
        if (!validateInputs()) {
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        Config cfg = getEnv().getCfg();
        if (cfg.getLastOpenDir() != null) {
            File fDir = new File(cfg.getLastOpenDir());
            if (fDir.isDirectory()) {
                fileChooser.setCurrentDirectory(fDir);
            }
        }
        // Set extension filter
        fileChooser.setAcceptAllFileFilterUsed(true);
        List<String> lstExts = HiDataStreamFactory.getSupportedInputExtensions();
        String sExts = String.join(", ", lstExts);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String ext = Utils.getFileExt(f);
                if (ext == null)
                    return false;
                ext = ext.toLowerCase(Locale.ROOT);

                return lstExts.contains(ext);
            }

            @Override
            public String getDescription() {
                return getString("input.filter.ext", sExts);
            }
        });

        // Show save file dialog
        int res = fileChooser.showOpenDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            onRefreshCover(file);
        }
    }


    public void onRefreshCover(File file) {
        if ((file == null) || (!file.exists()) || (file.isDirectory()))
            return;

        String sExt = Utils.getFileExt(file);
        List<String> lstExts = HiDataStreamFactory.getSupportedInputExtensions();
        String sExts = String.join(", ", lstExts);
        if (!lstExts.contains(sExt)) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        getString("input.err.ext", sExts),
                        getString("input.title"),
                        JOptionPane.ERROR_MESSAGE);
            });
            return;
        }

        btnSelectCover.setEnabled(false);
        btnLoad.setEnabled(false);
        btnSave.setEnabled(false);

        new Thread() {
            public void run() {
                try {
                    loadSource(file);
                    getEnv().getCfg().updateLastOpenDir(file);
                    getEnv().setInputFile(file);
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        String msg = (e.getMessage() != null) ? getString("input.err.file.data", e.getMessage()) : getString("input.err.file.data");
                        if (e instanceof TruncatedBagException) {
                            msg = getString("input.err.trunc");
                        }
                        JOptionPane.showMessageDialog(DataPanel.this,
                                msg,
                                getString("input.title"),
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
                SwingUtilities.invokeLater(() -> {
                    btnSelectCover.setEnabled(true);
                    btnLoad.setEnabled(true);
                    btnSave.setEnabled(true);
                });
            }
        }.start();
    }


    public void initAlgos() {
        String[] tabAlgos = new String[4];
        int idx = 0;
        tabAlgos[idx++] = "MD5";
        tabAlgos[idx++] = "SHA-256";
        tabAlgos[idx++] = "SHA-512";

        Config cfg = getEnv().getCfg();
        defaultAlgoIdx = 2;
        int iSel = defaultAlgoIdx;
        if (cfg.getAlgo() != null) {
            int i = 0;
            for (; i < idx; i++) {
                if (tabAlgos[i].equals(cfg.getAlgo())) {
                    iSel = i;
                    break;
                }
            }
            if (i == idx) {
                if (Utils.isAlgoSupported(cfg.getAlgo())) {
                    tabAlgos[idx++] = cfg.getAlgo();
                    iSel = i;
                }
            }
        }

        algo = new JComboBox<>(Arrays.copyOfRange(tabAlgos, 0, idx));
        algo.setSelectedIndex(iSel);
    }


    public void onSave(boolean to) {

        if (!validateInputs()) {
            return;
        }

        File file = getEnv().getInputFile();

        if (to) {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            dirChooser.setCurrentDirectory(file);

            // Show save file dialog
            int res = dirChooser.showSaveDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                file = dirChooser.getSelectedFile();
            }
        }

        if (file != null) {
            final File fFile = file;
            SwingUtilities.invokeLater(() -> {
                progress.setMinimum(0);
                progress.setMaximum(100);
                progress.setValue(0);
            });

            Thread runner = new Thread() {
                public void run() {
                    File fTemp = null;
                    try {
                        Parameters p = buildParams();
                        String sExt = Utils.getFileExt(fFile);
                        File outputFile = new File(fFile.getAbsolutePath());
                        if (fFile.equals(getEnv().getInputFile())) {
                            fTemp = new File(fFile.getAbsolutePath() + ".tmp");
                            fTemp.delete();
                            if (!fFile.renameTo(fTemp)) {
                                throw new RuntimeException("Could not create " + fTemp.getAbsolutePath());
                            }
                        } else {
                            fTemp = getEnv().getInputFile();
                        }
                        // in and out must not be the same file or the input file would be squizzed (length set to 0).
                        FileInputStream fis = new FileInputStream(fTemp);
                        FileOutputStream fos = new FileOutputStream(outputFile);
                        HiDataAbstractOutputStream out = HiDataStreamFactory.createOutputStream(fis, fos, p, sExt);
                        if (out == null)
                            throw new Exception(getString("encoder.error.ext", sExt));
                        HiDataBag bag = getEnv().getRepo().save();
                        bag.encryptAll(p);
                        out.write(bag.toByteArray());
                        out.close();
                    } catch (Exception e) {
                        LOG.error(e.getMessage());
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(DataPanel.this,
                                    getString("encoder.error.tryAgain"),
                                    getString("encoder.error"),
                                    JOptionPane.ERROR_MESSAGE);
                        });
                    } finally {
                        if ((fTemp != null) && (!fTemp.equals(getEnv().getInputFile()))) {
                            fTemp.delete();
                        }
                        getEnv().setInputFile(fFile);
                    }
                    SwingUtilities.invokeLater(() -> {
                        progress.setVisible(false);
                        TopEventDispatcher.dispatch(new TopEventProgressStateChanged(getEnv().getInputFile().getName()));
                    });
                }

            };
            runner.start();
        }
    }


    public void initialize() {
        setLayout(new BorderLayout());
        JPanel credPanel = new JPanel(new SpringLayout());
        add(credPanel, BorderLayout.NORTH);
        JLabel lbAlgo = new JLabel(getString("label.hash"), JLabel.RIGHT);
        credPanel.add(lbAlgo);

        initAlgos();
        credPanel.add(algo);
        algo.setEditable(true);
        algo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sAlgo = (String) algo.getSelectedItem();
                if (!Utils.isAlgoSupported(sAlgo)) {
                    JOptionPane.showMessageDialog(DataPanel.this,
                            getString("hash.notSupported", sAlgo));
                    algo.setSelectedIndex(defaultAlgoIdx);
                    sAlgo = (String) algo.getSelectedItem();
                }
                getEnv().getCfg().setAlgo(sAlgo);
            }
        });

        JLabel lbPassMaster = new JLabel(getString("label.pass.master"), JLabel.RIGHT);
        credPanel.add(lbPassMaster);

        passMaster = new JPasswordField(10);
        credPanel.add(passMaster);
        passMaster.setText(getEnv().getParams().getKm());

        JLabel lbPassData = new JLabel(getString("label.pass.data"), JLabel.RIGHT);
        credPanel.add(lbPassData);

        passData = new JPasswordField(10);
        credPanel.add(passData);
        passData.setText(getEnv().getParams().getKd());

        SpringUtilities.makeGrid(credPanel,
                3, 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad

        JPanel panelSelect = new JPanel();
        add(panelSelect, BorderLayout.SOUTH);
        panelSelect.setLayout(new GridLayout(0, 1, 0, 5));

        btnSelectCover = new JButton(getString("btn.image.select"));
        panelSelect.add(btnSelectCover);
        btnSelectCover.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSelectCover.addActionListener((actionEvent) -> {
            onOpenSource();
        });

        btnLoad = new JButton(getString("btn.image.reload"));
        panelSelect.add(btnLoad);
        btnLoad.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLoad.setEnabled(false);
        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRefreshCover(getEnv().getInputFile());
            }
        });

        btnSave = new JButton(getString("btn.image.save"));
        panelSelect.add(btnSave);
        btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSave.setEnabled(true);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSave(false);
            }
        });

        progress = new JProgressBar();
        panelSelect.add(progress);
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);
        progress.setVisible(false);

        buildParams();
    }

    @Override
    public void processTopEvent(TopEventBase e) {
        if (e instanceof TopEventInputFileChanged) {
            TopEventInputFileChanged o = (TopEventInputFileChanged) e;
            try {
                loadSource(o.getFile());
            } catch (TruncatedBagException ex) {
                // NO OP
            }
        }
        if (e instanceof TopEventClosing) {
            onSave(false);
        }
    }
}
