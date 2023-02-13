package com.topmail.transfert.out;

import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableCell;
import com.topmail.transfert.data.TableRow;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;


/**
 * @author ffradet
 */
public class TableExportCSV implements ITableExport {

    /**
     *
     */
    private static final String DEFAULT_SEPARATOR = "\t";
    private static final String DEFAULT_DQUOTE = "\"";

    private Charset charset = Charset.defaultCharset();
    private String separator = DEFAULT_SEPARATOR;

    // MUST BE ESCAPED SEE RFC 4180 (est que la cellule est escaped).
    private boolean addDoubleQuote = true;


    /**
     *
     */
    public TableExportCSV() {

    }


    /**
     * @param charsetName
     */
    public TableExportCSV(String charsetName, final boolean addDoubleQuote) {
        setCharset(charsetName);
        this.addDoubleQuote = addDoubleQuote;
    }


    /**
     * @param cs
     */
    public TableExportCSV(Charset cs, final boolean addDoubleQuote) {
        setCharset(cs);
        this.addDoubleQuote = addDoubleQuote;
    }


    /**
     * @param cs
     */
    public void setCharset(Charset cs) {
        charset = cs;
    }


    /**
     * @param name
     */
    public void setCharset(String name) {
        charset = Charset.forName(name);
    }


    /**
     * @param separator
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }


    // --------------------------
    // Exporter
    // --------------------------

    /**
     *
     */
    public void doExport(Table data, OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();

        Iterator<TableRow> iRow = data.getRows().iterator();
        Iterator<TableCell> iCell;
        while (iRow.hasNext()) {
            TableRow row = iRow.next();
            iCell = row.getCells().iterator();

            while (iCell.hasNext()) {
                TableCell cell = iCell.next();
                final String sValue = cell.getValue();
                final boolean mustBeEscaped = addDoubleQuote || mustBeEscaped(sValue);

                if (mustBeEscaped)
                    sb.append(DEFAULT_DQUOTE);
                sb.append(sValue.replaceAll("\"", "\"\""));
                if (mustBeEscaped)
                    sb.append(DEFAULT_DQUOTE);

                if (iCell.hasNext()) {
                    sb.append(separator);
                }
            }

            sb.append(EOL);

        }

        if (charset.equals(StandardCharsets.UTF_8)) {
            out.write(BOM);
        }
        out.write(sb.toString().getBytes(charset));
    }


    /**
     * @param value
     * @return true when value must be escaped with doubleQuote.
     */
    public boolean mustBeEscaped(final String value) {
        return value.contains(DEFAULT_DQUOTE) || value.contains(separator) || value.contains("\n")
                // EXCEL PASSE EN MODE ESCAPED QUAND PRESENCE D'UN "\t"
                || value.contains("\t");
        // Attention une sauvegarde sous excel (au format CSV) peut changer le fichier exporté
        // EXCEL TRANSFORME LE CHAMP -A => #NOM? (peu importe si escaped ou pas escaped)
        // EXCEL TRANSFORME LE CHAMP +A => #NOM? (peu importe si escaped ou pas escaped)
        // EXCEL TRANSFORME LE CHAMP (=A) => #NOM? (peu importe si escaped ou pas escaped)
    }

}
