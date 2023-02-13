package com.topmail.transfert.in;

import com.topmail.transfert.data.Table;
import com.topmail.transfert.data.TableCell;
import com.topmail.transfert.data.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * @author ffradet
 */
public class TableImportCSV implements ITableImport {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(TableImportCSV.class);

    private static final String EOL = "\n";
    private static final char DELIM_DEFAULT = '\t';
    private static final char QUOTE_DEFAULT = '\"';

    private char delim = DELIM_DEFAULT;
    private char quote = QUOTE_DEFAULT;
    private final String encoding;


    /**
     * @param encoding
     */
    public TableImportCSV(String encoding) {
        this.encoding = encoding;
    }


    /**
     * @return
     */
    public String getEncoding() {
        return encoding;
    }


    /**
     * @return
     */
    public char getDelim() {
        return delim;
    }


    /**
     * @param delim
     */
    public void setDelim(char delim) {
        this.delim = delim;
    }


    /**
     * @return
     */
    public char getQuote() {
        return quote;
    }


    /**
     * @param quote
     */
    public void setQuote(char quote) {
        this.quote = quote;
    }

    private enum State {
        // State création d'un nouveau ROW ou CELL
        NEW_ROW_CELL,
        // State dans une cell avec quote (Attente QUOTE + DELIM ou QUOTE + EOL) pour fermer la cell.
        CELL_WITHQUOTE,
    }


    /**
     * Remplit la table de données avec le contenu du flux CSV
     *
     * @param data une table de données
     * @param in   le flux CSV
     */
    public void doImport(Table data, InputStream in) throws IOException {

        BufferedReader bufRdr = new BufferedReader(new InputStreamReader(in, encoding));

        boolean addLastCell = true;
        State state = State.NEW_ROW_CELL;
        String line;
        // Pour éviter les warnings
        TableRow row = new TableRow();
        StringBuilder cellValue = new StringBuilder();
        //
        while ((line = bufRdr.readLine()) != null) {
            addLastCell = true;

            // log.debug("line = " + line);
            if (State.NEW_ROW_CELL == state) {
                row = data.addRow();
            }

            for (int i = 0; i < line.length(); ++i) {
                final char curCar = line.charAt(i);

                if (curCar == UTF8_BOM) {
                    // Suppression du caractère UTF8_BOM
                    continue;
                }
                if (curCar == quote) {
                    if (State.NEW_ROW_CELL == state) {
                        state = State.CELL_WITHQUOTE;
                        continue;
                    }

                    final boolean hasNextCar = (i + 1) < line.length();
                    if (!hasNextCar) {
                        addLastCell = false;
                    }
                    final char nextCar = hasNextCar ? line.charAt(++i) : delim;
                    if (nextCar == quote) {
                        // "" => "
                        cellValue.append(quote);
                        continue;
                    } else if (nextCar == delim) {
                        // new CELL
                        TableCell cell = row.addCell();
                        cell.setValue(cellValue.toString());
                        // log.debug("cell = " + cellValue.toString());
                        cellValue = new StringBuilder();
                        state = State.NEW_ROW_CELL;
                        continue;
                    }
                }

                if (curCar == delim && !State.CELL_WITHQUOTE.equals(state)) {
                    TableCell cell = row.addCell();
                    cell.setValue(cellValue.toString());
                    // log.debug("cell = " + cellValue.toString());
                    cellValue = new StringBuilder();
                    continue;
                }

                // Ajout du caractère
                cellValue.append(curCar);
            }

            if (State.CELL_WITHQUOTE.equals(state)) {
                // FIN DE LIGNE
                cellValue.append(EOL);
            } else if (addLastCell) {
                // Ajoute la dernière colonne (si besoin)
                TableCell cell = row.addCell();
                cell.setValue(cellValue.toString());
                // log.debug("cell = " + cellValue.toString());
                cellValue = new StringBuilder();
                state = State.NEW_ROW_CELL;
            }
        }
    }


    /**
     *
     */
    @Override
    public String toString() {
        return "TableImportCSV [delim='" + delim + "', encoding='" + encoding + "']";
    }
}
