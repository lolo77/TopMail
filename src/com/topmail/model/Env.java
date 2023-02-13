package com.topmail.model;

import com.secretlib.util.Log;
import com.secretlib.util.Parameters;

import java.io.File;

public class Env {

    private static final Log LOG = new Log(Env.class);


    Config cfg = new Config();
    DataRepository repo = new DataRepository();

    Parameters params = new Parameters();

    private File inputFile = null;

    private boolean showDlg = true;

    private int[] spaceCapacity = new int[8];

    public Env() {

    }

    public Config getCfg() {
        return cfg;
    }

    public void setCfg(Config cfg) {
        this.cfg = cfg;
    }

    public DataRepository getRepo() {
        return repo;
    }

    public void setRepo(DataRepository repo) {
        this.repo = repo;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public boolean isShowDlg() {
        return showDlg;
    }

    public void setShowDlg(boolean showDlg) {
        this.showDlg = showDlg;
    }

    public int[] getSpaceCapacity() {
        return spaceCapacity;
    }

    public void setSpaceCapacity(int[] spaceCapacity) {
        this.spaceCapacity = spaceCapacity;
    }

    public Parameters getParams() {
        return params;
    }

    public void setParams(Parameters params) {
        this.params = params;
    }
}
