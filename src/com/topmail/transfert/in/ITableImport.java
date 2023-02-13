package com.topmail.transfert.in;


import com.topmail.transfert.data.Table;

import java.io.IOException;
import java.io.InputStream;


/**
 * Importateur technique<br>
 * Les classes qui implémentent cette interface doivent pouvoir traduire des données sérialisées en un Table.
 *
 * @author ffradet
 */
public interface ITableImport {
    char UTF8_BOM = 65279;


    /**
     * @param data
     * @param in
     * @throws IOException
     */
    void doImport(Table data, InputStream in) throws IOException;
}
