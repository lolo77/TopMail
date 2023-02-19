package com.topmail.sender;

import com.secretlib.exception.HiDataEncoderException;
import com.secretlib.io.stream.HiDataAbstractOutputStream;
import com.secretlib.io.stream.HiDataStreamFactory;
import com.secretlib.model.ChunkData;
import com.secretlib.model.HiDataBag;
import com.secretlib.util.HiUtils;
import com.secretlib.util.Log;
import com.topmail.Main;
import com.topmail.exceptions.NoEmailException;
import com.topmail.exceptions.NoRecipientException;
import com.topmail.exceptions.NoTestEmailException;
import com.topmail.model.DataRepository;
import com.topmail.model.Settings;
import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableCell;
import com.topmail.transfert.data.TableRow;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

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

    private SenderState state;


    public MailSender(SenderState state) {
        this.state = state;
    }

    private TableRow getTestRow() throws NoEmailException, NoRecipientException, NoTestEmailException {
        String testEmail = getEnv().getRepo().getSettings().getProperties().getProperty(Settings.KEY_EMAIL_TEST);
        if ((testEmail == null) || (testEmail.length() == 0)) {
            throw new NoTestEmailException();
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

    public void sendTest() throws NoRecipientException, NoEmailException, GeneralSecurityException, MessagingException, IOException, NoTestEmailException {
        TableRow r = getTestRow();
        if (r != null) {
            int idxEmail = getEnv().getRepo().getEmailFieldIndex();
            String email = r.getCells().get(idxEmail).getValue();
            try {
                sendTo(r);
                state.getHmReport().put(email, new SenderState.EmailState(null));
            } catch (Exception e) {
                state.getHmReport().put(email, new SenderState.EmailState(e));
                throw e;
            }
        } else {
            throw new NoRecipientException();
        }
    }


    public void send() throws NoRecipientException, NoEmailException, GeneralSecurityException, MessagingException, IOException, NoTestEmailException {
        String testEmail = getEnv().getRepo().getSettings().getProperties().getProperty(Settings.KEY_EMAIL_TEST);
        if ((testEmail == null) || (testEmail.length() == 0)) {
            throw new NoTestEmailException();
        }
        int idxEmail = getEnv().getRepo().getEmailFieldIndex();
        Table tbl = getEnv().getRepo().getMailingList();
        Iterator<TableRow> iter = tbl.getRows().iterator();
        iter.next(); // skip header
        while (iter.hasNext()) {
            if (state.isInterruptionRequested()) {
                break;
            }
            TableRow r = iter.next();
            String email = r.getCells().get(idxEmail).getValue();
            if ((email != null) && (!email.equals(testEmail))) {
                try {
                    sendTo(r);
                    state.getHmReport().put(email, new SenderState.EmailState(null));
                } catch (Exception e) {
                    state.getHmReport().put(email, new SenderState.EmailState(e));
                    throw e;
                }
            }
        }
    }


    public void sendTo(TableRow r) throws NoRecipientException, NoEmailException, GeneralSecurityException, IOException, MessagingException {
        int idxEmail = getEnv().getRepo().getEmailFieldIndex();

        Properties settings = getEnv().getRepo().getSettings().getProperties();
        String to = r.getCells().get(idxEmail).getValue();

        HiDataBag secret = new HiDataBag();
        ChunkData cdSecret = new ChunkData();
        cdSecret.setName(DataRepository.CHUNK_SECRET);
        String toSalted = to + getEnv().getParams().getHashAlgo() + getEnv().getParams().getKm() + getEnv().getParams().getKd();
        byte[] secretData = HiUtils.genHash(toSalted.getBytes(StandardCharsets.UTF_8), getEnv().getParams().getHashAlgo());
        cdSecret.setData(secretData);
        secret.addItem(cdSecret);
        secret.encryptAll(getEnv().getParams());

        byte[] secretBytes = secret.toByteArray();


        String subject = getTransformedSubject(r);
//        LOG.debug("Transformed Subject : " + subject);

        String body = getTransformedBody(r);
//        LOG.debug("Transformed Body : " + body);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", settings.get(Settings.KEY_SMTP_HOST));
        props.put("mail.smtp.port", settings.get(Settings.KEY_SMTP_PORT));

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        String user = (String) settings.get(Settings.KEY_SMTP_USER);
                        String pwd = (String) settings.get(Settings.KEY_SMTP_PASS);
                        return new PasswordAuthentication(user, pwd);
                    }
                });

        // Create a default MimeMessage object.
        Message message = new MimeMessage(session);

        String from = settings.getProperty(Settings.KEY_EMAIL_FROM);
        // Set From: header field of the header.
        message.setFrom(new InternetAddress(from));

        // Set To: header field of the header.
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));

        // Set Subject: header field
        message.setSubject(subject);

        // Create the message part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Now set the actual message
        messageBodyPart.setContent(body, "text/html");

        // Create a multipar message
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        List<ChunkData> lst = getEnv().getRepo().getAttachments();
        for (ChunkData cd : lst) {
            String filepath = new String(cd.getData(), StandardCharsets.UTF_8);
            String filename = cd.getName();
            File f = new File(filepath);
            if ((f.exists()) && (f.isFile())) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(filepath);
                } catch (FileNotFoundException e) {
                    continue;
                }
                MimeBodyPart attach = new MimeBodyPart();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                HiDataAbstractOutputStream hdo = null;
                try {
                    hdo = HiDataStreamFactory.createOutputStream(fis, out, getEnv().getParams(), HiUtils.getFileExt(f));
                } catch (Exception e) {
                    throw new HiDataEncoderException(e);
                }
                DataSource source = null;
                if (hdo != null) {
                    try {
                        // Write the encrypted marker
                        hdo.write(secretBytes);
                        hdo.close();
                    } catch (IOException e) {
                        throw new HiDataEncoderException(e);
                    }
                    byte[] data = out.toByteArray();
                    String type = FileTypeMap.getDefaultFileTypeMap().getContentType(f);
                    source = new ByteArrayDataSource(new ByteArrayInputStream(data), type);
                } else {
                    // No encrypted marker
                    source = new FileDataSource(f);
                }
                attach.setDataHandler(new DataHandler(source));
                attach.setFileName(filename);
                multipart.addBodyPart(attach);
            } else {
                LOG.info("File ignored (" + filename + ") : " + filepath);
            }
        }

        // Send the complete message parts
        message.setContent(multipart);

        // Send message
        Transport.send(message);

        // Add counter
        state.setNbSent(state.getNbSent() + 1);
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
