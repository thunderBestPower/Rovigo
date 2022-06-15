/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpRoGetRich;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.Record;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author koalacurioso
 */
public class TracciatoUnix {

    ArrayList<String> rows;
    private Logger infoLogger,  exceptionLogger;
    File fileTracciato;

    public TracciatoUnix(String fileTracciato) throws Exception {
        this.fileTracciato = new File(fileTracciato);
        exceptionLogger = Logger.getLogger("exceptions");
        infoLogger = Logger.getLogger("ftpRoGetRich");

        BufferedReader br = null;
        rows = new ArrayList();
        br = new BufferedReader(new FileReader(fileTracciato));
        String input;
        while ((input = br.readLine()) != null) {
            rows.add(input);
        }
        br.close();
    }

    public void writeOnAS400(String address, String library, String filename) {
        try {
            AS400 as400 = new AS400(address, "DAVIDE", "BRUGOL8");
            as400.connectService(AS400.RECORDACCESS);

            String filePath = "/QSYS.LIB/" + library + ".LIB/" + filename + ".FILE";
            // File per scrittura su AS400
            SequentialFile writeFile = new SequentialFile(as400, filePath);
            AS400FileRecordDescription recordWriteDescription = new AS400FileRecordDescription(as400, filePath);
            RecordFormat[] writeformat = recordWriteDescription.retrieveRecordFormat();
            writeFile.setRecordFormat(writeformat[0]);
            writeFile.open(AS400File.READ_WRITE, 100, AS400File.COMMIT_LOCK_LEVEL_NONE);

            Iterator<String> i = rows.iterator();
            String tempRow = null;
            while (i.hasNext()) {
                //scrivo riga per riga nell'AS400
                tempRow = i.next();
                
                // scrivo file su AS400
                Record writeRecord = new Record(writeformat[0]);
                writeRecord.setField("KACCOR", tempRow.substring(0, 5));
                writeRecord.setField("KACCON", tempRow.substring(5, 10));
                writeRecord.setField("KACODI", tempRow.substring(10, 20));
                writeRecord.setField("KADTIN", tempRow.substring(20, 28));
                writeRecord.setField("KATREC", tempRow.substring(28, 30));
                writeRecord.setField("KATEXT", tempRow.substring(30));
                writeFile.write(writeRecord);
            }

            infoLogger.info("Terminata scrittura del file " + filename);
            //cancello il file
            if (fileTracciato.exists()) {
                if(fileTracciato.delete())
                    infoLogger.info("eliminato il File " + fileTracciato);
                else
                    infoLogger.severe("ERRORE nella cancellazione del file " 
                            + fileTracciato);
            }else{
                infoLogger.severe("il file "+fileTracciato+" non esiste");
            }
            
            //chiudo connessioni con AS400
            writeFile.close();
            as400.disconnectAllServices();
            infoLogger.log(Level.INFO, "Disconnessione da AS400");

        } catch (Exception e) {
            e.printStackTrace();
            exceptionLogger.log(Level.SEVERE, e.getMessage());
        }
    }
}
