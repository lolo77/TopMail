package com.topmail.model.table;

import com.topmail.transfert.data.TableRow;

/**
 * @author Florent FRADET
 */
public class DataItemCSV {

    public static final String FIELD_EMAIL = "email";

    private TableRow row;


    public DataItemCSV(TableRow row) {
        this.row = row;
    }

    public TableRow getRow() {
        return row;
    }

}
