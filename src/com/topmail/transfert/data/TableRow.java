package com.topmail.transfert.data;

import java.util.ArrayList;
import java.util.List;


/**
 * @author ffradet
 */
public class TableRow {

    /**
     *
     */
    private List<TableCell> cells = new ArrayList<TableCell>();

    /**
     *
     */
    public TableRow() {

    }

    /**
     * @return
     */
    public List<TableCell> getCells() {
        return cells;
    }

    /**
     * @param cells
     */
    public void setCells(List<TableCell> cells) {
        this.cells = cells;
    }

    /**
     * @return
     */
    public TableCell addCell() {
        TableCell cell = new TableCell();
        cells.add(cell);
        return cell;
    }

    /**
     * @param value
     * @return
     */
    public TableCell addCell(String value) {
        TableCell cell = new TableCell(value);
        cells.add(cell);
        return cell;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return "TableRow [cells=" + cells + "]\n";
    }
}
