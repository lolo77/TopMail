package com.topmail.panels;

import com.secretlib.util.Log;
import com.topmail.Main;
import com.topmail.events.TopEventBase;
import com.topmail.events.TopEventDataLoaded;
import com.topmail.events.TopEventDispatcher;
import com.topmail.events.TopEventListener;
import com.topmail.model.table.DataItemCSV;
import com.topmail.model.table.DataModelCSV;
import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableCell;
import com.topmail.transfert.data.TableRow;
import com.topmail.transfert.in.ITableImport;
import com.topmail.transfert.in.TableImportFactory;
import com.topmail.transfert.out.ITableExport;
import com.topmail.transfert.out.TableExportFactory;
import com.topmail.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.topmail.Main.getEnv;
import static com.topmail.Main.getString;

public class MailingPanel extends JPanel implements TopEventListener {

    private static final Log LOG = new Log(MailingPanel.class);

    DataModelCSV dataModel;
    JTable tableData;

    protected void onImport() {
        String s = Utils.fromClipboard();
        if (s != null) {
            try {
                Table tbl = getEnv().getRepo().getMailingList();
                tbl.getRows().clear();
                InputStream is = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
                ITableImport importer = TableImportFactory.buildImporterFromFile(is);
                is = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
                importer.doImport(tbl, is);
                TopEventDispatcher.dispatch(new TopEventDataLoaded());
            } catch (TableImportFactory.UnknownFileTypeException e) {
                LOG.debug("Import Exception : " + e.getMessage());
            } catch (IOException e) {
                LOG.debug("Import IOException : " + e.getMessage());
            }
        }
    }

    private void onAddRow() {
        Table tbl = getEnv().getRepo().getMailingList();
        TableRow header = null;
        if (tbl.getRows().size() > 0) {
            header = tbl.getRows().get(0);
        } else {
            header = tbl.addRow();
            header.addCell(DataItemCSV.FIELD_EMAIL);
        }
        TableRow newRow = tbl.addRow();
        for (TableCell c : header.getCells()) {
            newRow.addCell(); // empty value
        }
        TopEventDispatcher.dispatch(new TopEventDataLoaded());
    }


    private void onExport() {
        Table tbl = getEnv().getRepo().getMailingList();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ITableExport exporter = TableExportFactory.buildExporter(TableExportFactory.ExporterType.CSV_UTF8);
        try {
            exporter.doExport(tbl, os);
            Utils.toClipboard(new String(os.toByteArray(), StandardCharsets.UTF_8));
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        getString("export.message.nb_exported", tbl.getRows().size()),
                        getString("export.title"),
                        JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (IOException e) {
            LOG.debug("Import IOException : " + e.getMessage());
        }
    }


    public MailingPanel() {
        TopEventDispatcher.addListener(this);

        setLayout(new BorderLayout());

        dataModel = new DataModelCSV(getEnv().getRepo().getMailingList());

        tableData = new JTable(dataModel);
        tableData.setAutoCreateColumnsFromModel(true);
        JScrollPane scrollPane = new JScrollPane(tableData);
        tableData.setFillsViewportHeight(true);
        tableData.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        add(scrollPane, BorderLayout.CENTER);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(100, 250));

        JPanel btnPanel = new JPanel();
        add(btnPanel, BorderLayout.SOUTH);

        JButton btnAdd = new JButton("+");
        btnPanel.add(btnAdd);
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddRow();
            }
        });

        JButton btnImport = new JButton("Import");
        btnPanel.add(btnImport);
        btnImport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onImport();
            }
        });

        JButton btnExport = new JButton("Export");
        btnPanel.add(btnExport);
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExport();
            }
        });
    }


    @Override
    public void processTopEvent(TopEventBase e) {
        if (e instanceof TopEventDataLoaded) {
            SwingUtilities.invokeLater(() -> {
                tableData.createDefaultColumnsFromModel();
                MailingPanel.this.revalidate();
            });
        }
    }
}
