package com.topmail.util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Florent FRADET
 */
public class Utils {

    public static boolean isAlgoSupported(String algo) {
        try {
            MessageDigest md = MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException var4) {
            return false;
        }
        return true;
    }


    public static void addNumericValidator(JTextField source, long minVal, long maxVal) {
        source.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!evt.getPropertyName().equals(CoolJTextField.TEXT_PROPERTY)) {
                    return;
                }
                String newValue = (String) evt.getNewValue();
                if ((newValue.equals("-")) || (newValue.length() == 0)) {
                    return;
                }
                try {
                    long newVal = Long.parseLong(newValue);
                    if ((newVal < minVal) || (newVal > maxVal)) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    source.setText((String) evt.getOldValue());
                }
            }

        });
    }

    public static String getFileExt(File f) {
        String s = null;
        if (f != null && !f.isDirectory()) {
            String sName = f.getName();
            int i = sName.lastIndexOf(".");
            if (i >= 0) {
                s = sName.substring(i + 1);
                s = s.toLowerCase();
            }
        }
        return s;
    }

    public static String fromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            boolean hasString = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
            if (hasString) {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            // NO OP
        }
        return null;
    }

    public static boolean toClipboard(String s) {
        try {
            StringSelection strSel = new StringSelection(s);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(strSel, strSel);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
