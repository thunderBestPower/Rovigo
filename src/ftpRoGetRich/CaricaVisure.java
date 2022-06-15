package ftpRoGetRich;

import com.ibm.as400.access.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CaricaVisure {

    Logger exceptionLogger;
    Logger infoLogger;
    /**
     * Starts the application.
     * @param args an array of command-line arguments 
     */
    public void carica(String pathVM2) {   
        //creazione dei Logger
        exceptionLogger = Logger.getLogger("exceptions");
        infoLogger = Logger.getLogger("ftpRoGetRich");


        // MIC Legge un file generico AS400 da lib  
        try {
            AS400 as400 = new AS400(Configurazione.getConfig("CARICAAS400"), "DAVIDE", "BRUGOL8");
            try {
                as400.connectService(AS400.RECORDACCESS);
            } catch (Exception e) {
                e.printStackTrace();
                exceptionLogger.log(Level.SEVERE, e.getMessage());
            }

            // File per scrittura su AS400
            SequentialFile writeFile = new SequentialFile(as400, "/QSYS.LIB/" +
                    Configurazione.getConfig("CARICALIBRERIA") + ".LIB/VISSE00F.FILE");
            AS400FileRecordDescription recordWriteDescription =
                    new AS400FileRecordDescription(as400, "/QSYS.LIB/" +
                        Configurazione.getConfig("CARICALIBRERIA") + ".LIB/VISSE00F.FILE");
            RecordFormat[] writeformat = recordWriteDescription.retrieveRecordFormat();
            writeFile.setRecordFormat(writeformat[0]);
            writeFile.open(AS400File.READ_WRITE, 100, AS400File.COMMIT_LOCK_LEVEL_NONE);
 
            String input, name, year, month, day;
            BigDecimal data;
            GregorianCalendar gc = new GregorianCalendar();

            year = "" + gc.get(Calendar.YEAR);

            if (gc.get(Calendar.MONTH) + 1 < 10) {
                month = "0" + (gc.get(Calendar.MONTH) + 1);
            } else {
                month = "" + (gc.get(Calendar.MONTH) + 1);
            }

            if (gc.get(Calendar.DAY_OF_MONTH) < 10) {
                day = "0" + gc.get(Calendar.DAY_OF_MONTH);
            } else {
                day = "" + gc.get(Calendar.DAY_OF_MONTH);
            }
            data = new BigDecimal(year + month + day);

            // Crea cartella iniziale
            File orig, dest;
            //creo un'istanza della cartella del fornitore
            File inidir = new File(pathVM2);
            //leggo i file contenuti nella cartella
            File[] listfile = inidir.listFiles();
            
            BufferedReader br = null;
            //ciclo di caricamento su AS400
            for (int i = 0; i < listfile.length; i++) {
                //leggo solo i file VM2
                if (listfile[i].getName().indexOf("VM2") > -1) {  
                    br = new BufferedReader(new FileReader(pathVM2 +"\\"+ listfile[i].getName()));
                    name = listfile[i].getName();

                    //Loop sulle righe di testo 
                    while ((input = br.readLine()) != null) {
                        try {
                            // scrivo file su AS400
                            Record writeRecord = new Record(writeformat[0]);
                            writeRecord.setField("VSRIGA", input);
                            writeRecord.setField("VSELAB", "N");
                            writeRecord.setField("VSDELA", data);
                            writeRecord.setField("VSRICL", name.substring(0, name.indexOf(".")));
                            writeFile.write(writeRecord);
                        } catch (Exception e) {
                            e.printStackTrace();
                            exceptionLogger.log(Level.SEVERE, e.getMessage());
                        }
                    }
                    br.close();

                    // Cancella il file  
                    infoLogger.log(Level.INFO,"Caricato il file " + name);
                    //cancello il file
                    if (listfile[i].exists()) {
                        listfile[i].delete();
                    }
                }
            }
            writeFile.close();
            as400.disconnectAllServices();
            infoLogger.log(Level.INFO, "Disconnessione da AS400");
        } catch (Exception e) {
            e.printStackTrace();
            exceptionLogger.log(Level.SEVERE, e.getMessage());
        }
    }
}
