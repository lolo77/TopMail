package com.topmail.transfert.data;

/**
 * @author ffradet
 */
public class TableCell {

    /**
     *
     */
    private String value;

    /**
     *
     */
    public TableCell() {

    }

    /**
     * @param value
     */
    public TableCell(String value) {
        setValue(value);
    }

    /**
     * @return
     */
    public String getValue() {
        return value != null ? value : "";
    }

    /**
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return "TableCell [value=" + value + "]";
    }
}
