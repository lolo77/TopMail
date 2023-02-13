package com.topmail.transfert.out;

import java.util.HashMap;


/**
 * Définit le wrapper d'exportation CSV, XLS, PDF, etc...<br>
 * Construit une instance implémentant ITableExport selon le format ExporterType demandé.<br>
 *
 * @author ffradet
 */
public class TableExportFactory {

    /**
     *
     */
    private static final HashMap<ExporterType, ITableExport> hmExporters = new HashMap<ExporterType, ITableExport>();

    /**
     * @author ffradet
     */
    public enum ExporterType {
        CSV_ISO,
        CSV_UTF8
    }

    /**
     *
     */
    static {
        hmExporters.put(ExporterType.CSV_ISO, new TableExportCSV("CP1252", false));
        hmExporters.put(ExporterType.CSV_UTF8, new TableExportCSV("UTF-8", false));
    }


    /**
     * @param type ExporterType
     * @return
     */
    public static ITableExport buildExporter(ExporterType type) {
        return hmExporters.get(type);
    }
}
