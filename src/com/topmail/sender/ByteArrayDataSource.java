package com.topmail.sender;

import javax.activation.DataSource;
import java.io.*;

public class ByteArrayDataSource implements DataSource {

    private byte[] buf;
    private String name;

    public ByteArrayDataSource(String name, byte[] buf) {
        this.buf = buf;
        this.name = name;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(buf);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new ByteArrayOutputStream();
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
