package com.topmail.transfert.data;

import java.util.ArrayList;
import java.util.List;


/**
 * @author ffradet
 */
public class Table implements Cloneable {

    /**
     *
     */
    private String name;

    /**
     *
     */
    private List<TableRow> rows = new ArrayList<TableRow>();


    /**
     *
     */
    public Table() {

    }


    /**
     * @param name
     */
    public Table(String name) {
        this.name = name;
    }


    /**
     * @return
     */
    public String getName() {
        return name;
    }


    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return
     */
    public List<TableRow> getRows() {
        return rows;
    }


    /**
     * @param rows
     */
    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }


    /**
     * @return
     */
    public TableRow addRow() {
        TableRow row = new TableRow();
        rows.add(row);
        return row;
    }


    /**
     *
     */
    @Override
    public String toString() {
        return "Table [name=" + name + ", rows=\n" + rows + "]";
    }

    @Override
    public Table clone() {
        Table t = new Table(this.getName());
        int index = 0;
        for (TableRow row : this.getRows().subList(index, this.getRows().size())) {
            TableRow tr = t.addRow();
            for (TableCell cell : row.getCells()) {
                tr.addCell(cell.getValue());
            }
        }
        return t;
    }
}
