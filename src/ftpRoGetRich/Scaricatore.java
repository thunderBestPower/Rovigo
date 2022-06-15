/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpRoGetRich; 

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;
import com.enterprisedt.net.ftp.*;  
import java.util.Locale;
import java.util.logging.Level;

/**
 *
 * @author koalacurioso
 */
public class Scaricatore {

    private static Logger infoLogger,  exceptionLogger;
    private static Hashtable ftpconf;
    
    private static FTPClient ftp = null;

    private static void openConnection(){
        try {
            // Crea nuova connessione
            ftp = new FTPClient();
            UnixFileParser unixFileParser = new UnixFileParser();
            unixFileParser.setLocale(Locale.US);
            ftp.setFTPFileFactory(new FTPFileFactory(unixFileParser));
            ftp.setParserLocale(Locale.US);
            ftp.setRemoteHost(Configurazione.getConfig("host"));
            FTPMessageCollector listener = new FTPMessageCollector();
            ftp.setMessageListener(listener);
            ftp.connect();
            ftp.login(Configurazione.getConfig("user"),
                        Configurazione.getConfig("password"));

            ftp.setConnectMode(FTPConnectMode.PASV);
            ftp.setType(FTPTransferType.BINARY);
            infoLogger.log(Level.INFO, "Connesso al server FTP");

        } catch (Exception e) {
                infoLogger.log(Level.WARNING, "Impossibile connettersi al server DATAHOUSE!");
                infoLogger.log(Level.WARNING, "Riprovare tra due minuti...");
        }
    }
    
    private static void closeConnection(){
        try {
            // Chiude la connessione FTP
            ftp.quit();
            infoLogger.log(Level.INFO, "Chiusa la connessione FTP");
        } catch (IOException ex) {
            Logger.getLogger(Scaricatore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FTPException ex) {
            Logger.getLogger(Scaricatore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void getRichieste(){

        //creazione dei Logger
        exceptionLogger = Logger.getLogger("exceptions");
        infoLogger = Logger.getLogger("ftpRoGetRich");
        
        openConnection();

        String[] dirs = Configurazione.getConfig("ftpdirs").split(",");
        for(int i=0; i<dirs.length; i++){
            try {
                String[] dir = dirs[i].split(" ");
                getFiles(dir[0], dir[1]);
            } catch (Exception ex) {
                Logger.getLogger(Scaricatore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        closeConnection();
    }

    private static void getFiles(String ftpdir, String localdir) throws Exception{
        
           ftp.chdir("/home/"+ftpdir.trim());
            //ftp.chdir("/"+ftpdir.trim());
            // Recupera tutti i file dal sito FTP
            // li salva nella cartella temporanea
            // filePath, li elimina dal server
            FTPFile[] files = ftp.dirDetails("*.VM2");
            for (int i = 0; i < files.length; i++) {
                //recupero il file nella cartella di salvataggio generale
                ftp.get(Configurazione.getConfig("richpath")+"\\INARRIVO\\"+
                        localdir + "\\" + files[i].getName(), files[i].getName());
                //cancello il file in remoto
                ftp.delete(files[i].getName());
                infoLogger.log(Level.INFO, "Recuperato file richieste " + files[i].getName());
            }       
    }

}
