package com.topmail.panels;

import com.secretlib.util.Log;
import com.topmail.events.*;
import com.topmail.model.DataRepository;
import com.topmail.model.table.DataModelProp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.topmail.Main.getEnv;

public class SettingsPanel extends JPanel implements TopEventListener {
    private static final Log LOG = new Log(SettingsPanel.class);

    public static final String KEY_PREFIX = "table.settings.param.";
    DataModelProp dataModel;

    public SettingsPanel() {
        setLayout(new BorderLayout());

        dataModel = new DataModelProp(getEnv().getRepo().getSettings().getProperties());

        TopEventDispatcher.addListener(this);

        JTable tableData = new JTable(dataModel);

        JScrollPane scrollPane = new JScrollPane(tableData);
        tableData.setFillsViewportHeight(true);
        tableData.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        add(scrollPane, BorderLayout.CENTER);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(100, 250));
/*
        tableData.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Get the row and column at the mouse cursor
                    int row = tableData.rowAtPoint(e.getPoint());
                    handleDoubleClick(row);
                }
            }
        });*/
    }

    @Override
    public void processTopEvent(TopEventBase e) {
        if (e instanceof TopEventDataLoaded) {
            dataModel.updateList();
        }
        if (e instanceof TopEventDataSaving) {
            dataModel.updateProps();
        }
    }
}
