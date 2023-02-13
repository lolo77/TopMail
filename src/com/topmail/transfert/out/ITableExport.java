package com.topmail.transfert.out;


import com.topmail.transfert.data.Table;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Exportateur technique<br>
 * Les classes qui implémentent cette interface doivent pouvoir traduire un Table en données sérialisées.
 *
 * @author ffradet
 */
public interface ITableExport {

    // UTF-8 Byte Order Mark
    byte[] BOM = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
    // Default EndOfLine for CSV files (Excel saves \r\n)
    String EOL = "\r\n";


    /**
     * @param data
     * @param out
     * @throws IOException
     */
    void doExport(Table data, OutputStream out) throws IOException;

}
