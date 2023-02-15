package com.topmail.model;

import com.secretlib.model.AbstractChunk;
import com.secretlib.model.ChunkData;
import com.secretlib.model.HiDataBag;
import com.secretlib.util.Log;
import com.topmail.events.TopEventDataLoaded;
import com.topmail.events.TopEventDataSaving;
import com.topmail.events.TopEventDispatcher;
import com.topmail.exceptions.NoEmailException;
import com.topmail.exceptions.NoRecipientException;
import com.topmail.panels.DataPanel;
import com.topmail.panels.SettingsPanel;
import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableCell;
import com.topmail.transfert.data.TableRow;
import com.topmail.transfert.in.ITableImport;
import com.topmail.transfert.in.TableImportFactory;
import com.topmail.transfert.out.ITableExport;
import com.topmail.transfert.out.TableExportFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.topmail.Main.getEnv;

public class DataRepository {

    private static final Log LOG = new Log(DataRepository.class);

    private static final String CHUNK_SETTINGS = "SETTINGS\u00FF";
    private static final String CHUNK_MAILING = "MAILING\u00FF";
    private static final String CHUNK_SUBJECT = "SUBJECT\u00FF";
    private static final String CHUNK_BODY = "BODY\u00FF";
    private static final String CHUNK_REPORT = "REPORT\u00FF";

    public static final String CHUNK_SECRET = " \u00A0";
    public static final String MAILING_KEY_EMAIL = "email";

    private static final String[] CHUNKS = new String[]{CHUNK_SETTINGS, CHUNK_MAILING, CHUNK_SUBJECT, CHUNK_BODY, CHUNK_REPORT};

    HiDataBag bag = new HiDataBag();
    Settings settings = new Settings();
    Table mailingList = new Table();

    String subject = "";
    String body = "";
    String report = "";

    public DataRepository() {

    }

    public void load(HiDataBag bag) {
        this.bag = bag;
        bag.addHash(null);

        loadSettings();
        loadMailingList();
        loadSubject();
        loadBody();
        loadReport();

        TopEventDispatcher.dispatch(new TopEventDataLoaded());
    }

    public void loadSettings() {
        ChunkData cd = getByName(CHUNK_SETTINGS);
        if (cd != null) {
            try {
                settings.load(cd.getData());
            } catch (Exception e) {
            }
        }
    }

    public void loadMailingList() {
        try {
            ChunkData cd = getByName(CHUNK_MAILING);
            if (cd != null) {
                mailingList.getRows().clear();
                ByteArrayInputStream buf = new ByteArrayInputStream(cd.getData());
                ITableImport importer = TableImportFactory.buildImporterFromFile(buf);
                buf = new ByteArrayInputStream(cd.getData());
                importer.doImport(mailingList, buf);
            }
        } catch (Exception e) {
        }
    }

    public void loadBody() {
        body = "";
        try {
            ChunkData cd = getByName(CHUNK_BODY);
            if (cd != null) {
                body = new String(cd.getData());
            }
        } catch (Exception e) {
        }
    }

    public void loadSubject() {
        subject = "";
        try {
            ChunkData cd = getByName(CHUNK_SUBJECT);
            if (cd != null) {
                subject = new String(cd.getData());
            }
        } catch (Exception e) {
        }
    }


    public void loadReport() {
        report = "";
        try {
            ChunkData cd = getByName(CHUNK_REPORT);
            if (cd != null) {
                report = new String(cd.getData());
            }
        } catch (Exception e) {
        }
    }

    public HiDataBag save() {

        TopEventDispatcher.dispatch(new TopEventDataSaving());

        saveSettings();
        saveMailingList();
        saveSubject();
        saveBody();
        saveReport();

        bag.addHash(null);

        return bag;
    }

    public void saveSettings() {
        ChunkData cd = getByName(CHUNK_SETTINGS);
        if (cd == null) {
            cd = new ChunkData();
            cd.setName(CHUNK_SETTINGS);
            bag.addItem(cd);
        }
        try {
            cd.setData(settings.toByteArray());
        } catch (Exception e) {

        }
    }

    public void saveMailingList() {
        ChunkData cd = getByName(CHUNK_MAILING);
        if (cd == null) {
            cd = new ChunkData();
            cd.setName(CHUNK_MAILING);
            bag.addItem(cd);
        }
        try {
            ITableExport exporter = TableExportFactory.buildExporter(TableExportFactory.ExporterType.CSV_UTF8);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            exporter.doExport(mailingList, buf);
            cd.setData(buf.toByteArray());
        } catch (Exception e) {

        }
    }

    public void saveSubject() {
        ChunkData cd = getByName(CHUNK_SUBJECT);
        if (cd == null) {
            cd = new ChunkData();
            cd.setName(CHUNK_SUBJECT);
            bag.addItem(cd);
        }
        try {
            cd.setData(subject.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {

        }
    }
    public void saveBody() {
        ChunkData cd = getByName(CHUNK_BODY);
        if (cd == null) {
            cd = new ChunkData();
            cd.setName(CHUNK_BODY);
            bag.addItem(cd);
        }
        try {
            cd.setData(body.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {

        }
    }

    public void saveReport() {
        ChunkData cd = getByName(CHUNK_REPORT);
        if (cd == null) {
            cd = new ChunkData();
            cd.setName(CHUNK_REPORT);
            bag.addItem(cd);
        }
        try {
            cd.setData(report.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {

        }
    }

    public ChunkData getByName(String name) {
        for (AbstractChunk c : bag.getItems()) {
            if (c instanceof ChunkData) {
                ChunkData cd = (ChunkData) c;
                if (name.equals(cd.getName())) {
                    return cd;
                }
            }
        }
        return null;
    }

    public Settings getSettings() {
        return settings;
    }

    public Table getMailingList() {
        return mailingList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public boolean isAttachement(ChunkData cd) {
        for (String reservedName : CHUNKS) {
            if (reservedName.equals(cd.getName())) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<ChunkData> getAttachments() {
        ArrayList<ChunkData> lst = new ArrayList<>();
        for (AbstractChunk c : bag.getItems()) {
            if (c instanceof ChunkData) {
                ChunkData cd = (ChunkData) c;
                if (isAttachement(cd)) {
                    lst.add(cd);
                }
            }
        }
        return lst;
    }

    public ChunkData addAttachment() {
        ChunkData cd = new ChunkData();
        bag.addItem(cd);
        return cd;
    }

    public void removeAttachment(ChunkData cd) {
        bag.removeById(cd.getId());
    }

    public int getEmailFieldIndex() throws NoEmailException, NoRecipientException {
        Table tbl = mailingList;
        if (tbl.getRows().size() < 2) {
            throw new NoRecipientException();
        }
        TableRow header = tbl.getRows().get(0);
        int iCell = 0;
        while (iCell < header.getCells().size()) {
            TableCell c = header.getCells().get(iCell);
            String s = c.getValue();
            if ((s != null) && (s.equals(DataRepository.MAILING_KEY_EMAIL))) {
                return iCell;
            }

            iCell++;
        }
        throw new NoEmailException();
    }
}
