package com.topmail.model;

import com.secretlib.util.Log;
import com.topmail.events.TopEventDispatcher;
import org.bouncycastle.util.Arrays;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class Settings {

    private static final Log LOG = new Log(Settings.class);

    public static final String KEY_SMTP_HOST = "smtp.host";
    public static final String KEY_SMTP_PORT = "smtp.port";
    public static final String KEY_SMTP_USER = "smtp.user";
    public static final String KEY_SMTP_PASS = "smtp.pass";
    public static final String KEY_EMAIL_FROM = "email.from";
    public static final String KEY_EMAIL_TEST = "email.test";


    private static final String[] allKeys = {KEY_SMTP_HOST,
            KEY_SMTP_PORT,
            KEY_SMTP_USER,
            KEY_SMTP_PASS,
            KEY_EMAIL_FROM,
            KEY_EMAIL_TEST};
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
        Iterator<Object> iter = props.keySet().iterator();
        while (iter.hasNext()) {
            String k = (String)iter.next();

            boolean b = false;
            for (String s : allKeys) {
                if (s.equals(k)) {
                    b = true;
                    break;
                }
            }
            if (!b) {
                iter.remove();
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
