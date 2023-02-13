package com.topmail.events;

import java.io.File;

public class TopEventInputFileChanged extends TopEventBase {

    private File f;

    public TopEventInputFileChanged(File f) {
        this.f = f;
    }

    public File getFile() {
        return f;
    }
}
