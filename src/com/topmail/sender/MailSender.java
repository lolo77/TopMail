package com.topmail.sender;

import com.secretlib.util.Log;
import com.topmail.Main;
import com.topmail.exceptions.NoEmailException;
import com.topmail.exceptions.NoRecipientException;
import com.topmail.model.Settings;
import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableCell;
import com.topmail.transfert.data.TableRow;

import java.util.Calendar;

import static com.topmail.Main.getEnv;

public class MailSender {


    private static final Log LOG = new Log(MailSender.class);

    public static abstract class Placeholder {
        private String name;

        public Placeholder(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public abstract String getValue();
    }

    public static final String PLACEHOLDER_DATE = "date";
    public static Placeholder P_DATE = new Placeholder(PLACEHOLDER_DATE) {
        @Override
        public String getValue() {
            Calendar cal = Calendar.getInstance();
            return String.format("%02d/%02d/%04d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
        }
    };

    public static Placeholder[] PLACEHOLDERS = {P_DATE};

    public MailSender() {

    }

    private TableRow getTestRow() throws NoEmailException, NoRecipientException {
        String testEmail = getEnv().getRepo().getSettings().getProperties().getProperty(Settings.KEY_EMAIL_TEST);
        if ((testEmail == null) || (testEmail.length() == 0)) {
            throw new NoEmailException();
        }
        int idxEmail = getEnv().getRepo().getEmailFieldIndex();
        Table tbl = getEnv().getRepo().getMailingList();
        for (TableRow r : tbl.getRows()) {
            String email = r.getCells().get(idxEmail).getValue();
            if ((email != null) && (email.equals(testEmail))) {
                return r;
            }
        }

        return null;
    }

    public void sendTest() throws NoRecipientException, NoEmailException {
        TableRow r = getTestRow();
        if (r != null) {
            sendTo(r);
        } else {
            throw new NoRecipientException();
        }
    }

    public void sendTo(TableRow r) throws NoRecipientException {
        String subject = getTransformedSubject(r);
        LOG.debug("Transformed Subject : " + subject);

        String body = getTransformedBody(r);
        LOG.debug("Transformed Body : " + body);
    }

    private String getTransformedSubject(TableRow r) throws NoRecipientException {
        return transform(Main.getEnv().getRepo().getSubject(), r);
    }

    private String getTransformedBody(TableRow r) throws NoRecipientException {
        return transform(Main.getEnv().getRepo().getBody(), r);
    }

    private String transform(String in, TableRow r) throws NoRecipientException {
        Table tbl = getEnv().getRepo().getMailingList();
        if (tbl.getRows().size() < 2) {
            throw new NoRecipientException();
        }
        TableRow header = tbl.getRows().get(0);
        int iCell = 0;
        String sOut = new String(in);
        // Replace with CSV values
        while (iCell < header.getCells().size()) {
            TableCell c = header.getCells().get(iCell);
            String placeHolder = "${" + c.getValue() + "}";

            sOut = sOut.replace(placeHolder, r.getCells().get(iCell).getValue());
            iCell++;
        }
        // Replace with placeholders values
        for (Placeholder p : PLACEHOLDERS) {
            String placeHolder = "${" + p.getName() + "}";

            sOut = sOut.replace(placeHolder, p.getValue());
        }
        return sOut;
    }
}
