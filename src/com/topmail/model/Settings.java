package com.topmail.model;

import com.secretlib.util.Log;
import com.topmail.events.TopEventDispatcher;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {

    private static final Log LOG = new Log(Settings.class);

    public static final String KEY_SMTP_HOST = "smtp.host";
    public static final String KEY_SMTP_PORT = "smtp.port";
    public static final String KEY_SMTP_USER = "smtp.user";
    public static final String KEY_SMTP_PASS = "smtp.pass";
    public static final String KEY_EMAIL_TEST = "email.test";
    public static final String KEY_MARK_SALT = "mark.salt";


    private static final String[] allKeys = {KEY_SMTP_HOST,
            KEY_SMTP_PORT,
            KEY_SMTP_USER,
            KEY_SMTP_PASS,
            KEY_EMAIL_TEST,
            KEY_MARK_SALT};
    Properties props = new Properties();

    public Settings() {
        checkKeys();
    }

    public void load(byte[] data) throws IOException {
        props.clear();
        props.load(new ByteArrayInputStream(data));
        checkKeys();
    }

    protected void checkKeys() {
        for (String key : allKeys) {
            if (props.getProperty(key) == null) {
                props.setProperty(key, "");
                LOG.debug("Added missing key : " + key);
            }
        }
    }

    public Properties getProperties() {
        return props;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        props.store(buf, null);
        return buf.toByteArray();
    }

}
