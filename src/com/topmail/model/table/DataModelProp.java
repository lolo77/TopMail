package com.topmail.model.table;


import com.topmail.Main;
import com.topmail.panels.SettingsPanel;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 *
 */
public class DataModelProp implements TableModel {

    private final List<DataItemProp> lstItems = new ArrayList<>();

    private Properties props;

    public DataModelProp(Properties props) {
        this.props = props;
        updateList();
    }

    public void updateList() {
        lstItems.clear();
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            DataItemProp it = new DataItemProp((String) e.getKey(), (String) e.getValue());
            lstItems.add(it);
        }
    }

    public void updateProps() {
        props.clear();
        for (DataItemProp p : lstItems) {
            props.put(p.getKey(), p.getValue());
        }
    }

    public List<DataItemProp> getLstItems() {
        return lstItems;
    }

    @Override
    public int getRowCount() {
        return lstItems.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Main.getString("table.settings.key");
            case 1:
                return Main.getString("table.settings.value");
            default:
                break;
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 1);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DataItemProp item = lstItems.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return Main.getString(SettingsPanel.KEY_PREFIX + item.getKey());
            case 1:
                return item.getValue();
            default:
                break;
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        DataItemProp item = lstItems.get(rowIndex);
        if (columnIndex == 0) {
            item.setKey((String) aValue);
        }
        if (columnIndex == 1) {
            item.setValue((String) aValue);
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}