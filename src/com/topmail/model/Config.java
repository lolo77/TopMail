package com.topmail.model;


import com.secretlib.util.Log;

import java.awt.*;
import java.io.File;

public class Config {

    private static final Log LOG = new Log(Config.class);


    private String algo;
    private String lastOpenDir;
    private Rectangle frameRect;
    private String lang;

    public String getAlgo() {
        return algo;
    }

    public void setAlgo(String algo) {
        this.algo = algo;
    }

    public String getLastOpenDir() {
        return lastOpenDir;
    }

    public void setLastOpenDir(String lastOpenDir) {
        this.lastOpenDir = lastOpenDir;
    }

    public Rectangle getFrameRect() {
        return frameRect;
    }

    public void setFrameRect(Rectangle frameRect) {
        this.frameRect = frameRect;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public static String getPath(String filename) {
        int idx = filename.lastIndexOf("/");
        if (idx < 0) {
            idx = filename.lastIndexOf("\\");
        }
        if (idx > 0) {
            return filename.substring(0, idx);
        }
        return "";
    }

    public void updateLastOpenDir(File file) {
        setLastOpenDir(getPath(file.getAbsolutePath()));
    }
}
