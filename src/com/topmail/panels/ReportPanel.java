package com.topmail.panels;

import com.topmail.Main;
import com.topmail.events.TopEventBase;
import com.topmail.events.TopEventDispatcher;
import com.topmail.events.TopEventListener;
import com.topmail.events.TopEventReportChanged;
import com.topmail.model.table.DataModelCSV;
import com.topmail.sender.SenderState;
import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableCell;
import com.topmail.transfert.data.TableRow;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static com.topmail.Main.getString;

public class ReportPanel extends JPanel implements TopEventListener {

    JTable tableView;
    Table table;
    DataModelCSV model;

    public ReportPanel() {
        setLayout(new BorderLayout());
        table = new Table();
        TableRow header = table.addRow();
        TableCell cell = header.addCell();
        cell.setValue(Main.getString("lbl.report.email"));
        cell = header.addCell();
        cell.setValue(Main.getString("lbl.report.cause"));
        model = new DataModelCSV(table) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        tableView = new JTable(model);
        add(tableView, BorderLayout.CENTER);
        tableView.setAutoCreateColumnsFromModel(true);
        JScrollPane scrollPane = new JScrollPane(tableView);
        tableView.setFillsViewportHeight(true);
        tableView.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        add(scrollPane, BorderLayout.CENTER);

        TopEventDispatcher.addListener(this);
    }

    private void updateReport(SenderState state) {
        for (Map.Entry<String, SenderState.EmailState> e : state.getHmReport().entrySet()) {
            TableRow row = table.addRow();
            TableCell cell = row.addCell();
            cell.setValue(e.getKey());
            String cause = Main.getString("lbl.sender.success");
            Exception ex = e.getValue().getCause();
            if (ex != null) {
                cause = ex.getMessage();
                if ((cause == null) || (cause.length() == 0)) {
                    cause = getString("lbl.sender.exception." + ex.getClass().getSimpleName(), ex.getMessage());
                    if ((cause == null) || (cause.length() == 0)) {
                        cause = Main.getString("lbl.sender.exception.MessagingException");
                    }
                }
            }
            cell = row.addCell();
            cell.setValue(cause);
        }
    }

    @Override
    public void processTopEvent(TopEventBase e) {
        if (e instanceof TopEventReportChanged) {
            TopEventReportChanged es = (TopEventReportChanged) e;
            updateReport(es.getState());
            SwingUtilities.invokeLater(() -> {
                tableView.createDefaultColumnsFromModel();
                ReportPanel.this.revalidate();
            });
        }
    }
}
