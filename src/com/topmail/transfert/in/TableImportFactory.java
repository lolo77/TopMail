package com.topmail.transfert.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


/**
 * Définit le wrapper d'importation CSV, XLS, PDF, etc...<br>
 * Construit une instance implémentant ITableImport selon le format ImporterType demandé.<br>
 *
 * @author ffradet
 */
public class TableImportFactory {
    //
    private static final Logger log = LoggerFactory.getLogger(TableImportFactory.class);

    public static class UnknownFileTypeException extends Exception {
        private static final long serialVersionUID = -2184087347745216245L;


        public UnknownFileTypeException() {
            super();
        }


        public UnknownFileTypeException(Throwable e) {
            super(e);
        }


        public UnknownFileTypeException(String msg) {
            super(msg);
        }

    }

    public enum ImporterType {
        CSV_ISO, CSV_UTF_8
    }

    private static final HashMap<ImporterType, ITableImport> hmImporters = new HashMap<ImporterType, ITableImport>();

    static {
        hmImporters.put(ImporterType.CSV_ISO, new TableImportCSV("CP1252"));
        hmImporters.put(ImporterType.CSV_UTF_8, new TableImportCSV("UTF-8"));
    }


    public static ITableImport buildImporter(ImporterType type) {
        return hmImporters.get(type);
    }


    /**
     * @param is InputStream à analyser ; attention, il est totalement lu et il faudra le "rembobiner" avant l'import.
     * @return l'importateur
     * @throws UnknownFileTypeException si format de fichier inconnu
     * @throws IOException              si erreur de lecture du stream
     */
    public static ITableImport buildImporterFromFile(InputStream is) throws UnknownFileTypeException, IOException {
        BufferedInputStream bufis = new BufferedInputStream(is, 65536);
        ImporterType type = autoDetectFromSample(bufis);
        return buildImporter(type);
    }


    // Les espaces de la javadoc ci-dessous sont des 0xff pour garder l'alignement.

    /**
     * Cette fonction analyse le codage des caractères contenus dans le buffer. Si des caractères CP1252 sont présents avec des caractères UTF-8, le codage est ISO.<br>
     * <b>Attention : </b>Accédé par JUnit par réflexion (fr.stime.alta.altacommon.tool.transfert.TableImportFactoryTest#getMethod()).<br>
     * <br>
     * <code>Représentation binaire UTF-8       </code> Signification<br>
     * <code>0xxxxxxx                           </code> 1 oct codant 07 bits (=ASCII)<br>
     * <code>110xxxxx 10xxxxxx                  </code> 2 oct codant 11 bits<br>
     * <code>1110xxxx 10xxxxxx 10xxxxxx         </code> 3 oct codant 16 bits<br>
     * <code>11110xxx 10xxxxxx 10xxxxxx 10xxxxxx</code> 4 oct codant 21 bits<br>
     * <br>
     *
     * @param is le flux d'entrée
     * @return le type de flux détecté
     * @throws UnknownFileTypeException
     */
    public static ImporterType autoDetectFromSample(InputStream is) throws UnknownFileTypeException, IOException {

        boolean debug = false;
        boolean isASCII = true;
        boolean isText = true;
        boolean isUTF8 = true;
        int i = 0;
        int oct = 0;
        int line = 1;
        //
        while (oct >= 0) {
            oct = is.read();
            i++;
            //
            if (oct == -1) {
                break;
            }
            //
            if (debug) {
                log.debug("caractère trouvé [" + i + "] : " + oct);
            }

            // Incremente le numero de ligne
            if (oct == 0x0A) {
                line++;
            }

            // Caractère non-ASCII et ni CR/LF/TAB -> pas du CSV.
            // Vérification de la cohérence ASCII
            if ((oct < 0x20) && (oct != 0x0D) && (oct != 0x0A) && (oct != 0x09)) {
                if (debug) {
                    log.debug("Not Text");
                }
                isText = false;
                break;
            }

            // ASCII = 7 bits
            if (isASCII && ((oct & 0x80) != 0)) {
                if (debug) {
                    log.debug("Not ASCII");
                }
                isASCII = false;
                // Pas de break : c'est peut-être ISO ou UTF.
            }

            // Vérification de la cohérence UTF-8
            if (isUTF8) {
                // Nombre d'octets pour le caractère UTF-8 = nombre de bits forts à 1 (4 maxi) suivi d'un bit à 0
                int iNbCarsUTF = ((oct & 0xF8) == 0xF0) ? 4 : ((oct & 0xF0) == 0xE0) ? 3 : ((oct & 0xE0) == 0xC0) ? 2 : ((oct & 0xC0) == 0x80) ? 1 : 0;
                //
                if (debug) {
                    log.debug("caractère trouvé [" + i + "] : " + oct + " ; iNbCarsUTF = " + iNbCarsUTF);
                }

                int iCnt = 1;
                //
                for (; iCnt < iNbCarsUTF; iCnt++) {
                    oct = is.read();
                    i++;
                    //
                    if (oct == -1) {
                        // Caractère UTF-8 coupé : ce n'est donc pas de l'UTF ou le stream est coupé.
                        isUTF8 = false;
                        break;
                    }
                    //
                    if (debug) {
                        log.debug("Next caractère trouvé [" + i + "] : " + oct);
                    }
                    // Tous les caractères qui suivent doivent commencer par 10xxxxxx
                    if ((oct & 0xC0) != 0x80) {
                        //
                        if (debug) {
                            log.debug("Not UTF-8");
                        }

                        isUTF8 = false;
                        break;
                    }
                }
            }
        }
        //
        if (isText) {
            //
            if (isASCII) {
                return ImporterType.CSV_UTF_8; // Retourne le format UTF-8 par défaut si ASCII
            }
            //
            if (isUTF8) {
                return ImporterType.CSV_UTF_8;
            }

            return ImporterType.CSV_ISO;
        }

        // Test des autres formats ici, à implémenter

        // // XLS
        // if (isXls(is))
        // return ImporterType.XLS;
        //
        // // XLSX
        // if (isXlsx(is))
        // return ImporterType.XLSX;
        //

        throw new UnknownFileTypeException("Ligne: " + line + ", un caractère du fichier qui a pour valeur " + oct + " n'est pas valide pour l'import !");
    }

}
