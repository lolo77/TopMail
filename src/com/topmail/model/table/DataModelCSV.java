package com.topmail.model.table;


import com.topmail.Main;
import com.topmail.panels.SettingsPanel;
import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableRow;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 *
 */
public class DataModelCSV implements TableModel {

    private Table table;

    public DataModelCSV(Table table) {
        this.table = table;
    }

    @Override
    public int getRowCount() {
        return Math.max(0, table.getRows().size()-1);
    }

    @Override
    public int getColumnCount() {
        if (table.getRows().size() > 0) {
            return table.getRows().get(0).getCells().size();
        }
        return 0;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (table.getRows().size() > 0) {
            TableRow header = table.getRows().get(0);
            if (header.getCells().size() > columnIndex) {
                return header.getCells().get(columnIndex).getValue();
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (table.getRows().size()-1 > rowIndex) {
            TableRow row = table.getRows().get(rowIndex+1);
            if (row.getCells().size() > columnIndex) {
                return row.getCells().get(columnIndex).getValue();
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (table.getRows().size()-1 > rowIndex) {
            TableRow row = table.getRows().get(rowIndex+1);
            if (row.getCells().size() > columnIndex) {
                row.getCells().get(columnIndex).setValue((String)aValue);
            }
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}