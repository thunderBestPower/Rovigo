/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpRoGetRich;

import com.ibm.as400.access.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.as400.access.Record;
import richiestedh.ConverterDH;

public class Smistatore extends TimerTask {

    public int x,y;
    public String input,  kcons,  dcons,  kdric,  kdsca, kdagg,
            kcodi, pdir, rifac, preve, datana,tiporic,
            numNote, codcli, note, cfpiva, auth;
    public String[] knome;
    private Logger infoLogger,  exceptionLogger;

    private SequentialFile writeFile; 
    private RecordFormat[] writeformat;
    private AS400FileRecordDescription recordWriteDescription;
    private AS400 as400;
    private String richPath;

    public void run() {
        try {
            //creazione dei Logger
            exceptionLogger = Logger.getLogger("exceptions");
            infoLogger = Logger.getLogger("ftpRoGetRich");

            //carico i parametri più usati nelle variabili locali
            richPath = Configurazione.getConfig("richpath");

            // Crea collegamento all'AS400
            as400 = new AS400(Configurazione.getConfig("AS400"), "SISTPDF", "BRUGOLA");
            as400.connectService(AS400.RECORDACCESS);

            //Scarico i file dal server ftp
            Scaricatore.getRichieste();

            //scrivo le richieste arrivate su AS400
            richiesteSuAs400();

            
            infoLogger.log(Level.INFO, "Terminato scarico richieste");

            writeFile = new SequentialFile(as400, "/QSYS.LIB/" + Configurazione.getConfig("LIBRERIA") + ".LIB/CTRDH00F.FILE");
            recordWriteDescription = new AS400FileRecordDescription(as400, "/QSYS.LIB/" + Configurazione.getConfig("LIBRERIA") + ".LIB/CTRDH00F.FILE");
            writeformat = recordWriteDescription.retrieveRecordFormat();
            writeFile.setRecordFormat(writeformat[0]);
            writeFile.open(AS400File.READ_WRITE, 100, AS400File.COMMIT_LOCK_LEVEL_NONE);

            //controllo in AS400 se ci sono file da spostare
            Record record = writeFile.readFirst();
            while (record != null) {
                String flag = record.getField("CTFLGE").toString().toLowerCase();
                if (flag.contains("d")) {
                    String rifacFlag;
                    if(record.getField("CTTIPO").toString().toLowerCase().contains("r")){
                        rifacFlag = "R";
                    }else{
                        rifacFlag = "D";
                    }
                    String ctcodi = record.getField("CTCODI").toString().trim().substring(3);
                    //Modifica Mattia, leggo il codice se è minore di 5000000 (7 cifre) aggiungo 1 davanti al codice.
                    if(ctcodi.compareTo("5000000") < 0){
                        ctcodi = "1" + ctcodi;
                    }
                    // Fine Modifica
                    String nomeFile = rifacFlag + ctcodi + ".VM2";
                    //recupero il file da smistare
                    String filein = richPath + "\\DASMISTARE\\"+ nomeFile;
                    //recupero la cartella di destinazione
                    String dstFolder = record.getField("CTFLDR").toString().trim();
                    //creo un istanza del file di destinazione
                    File fileout = new File(richPath + "\\" + dstFolder + "\\" + nomeFile);
                    //richiamo la funzione di smistamento
                    try {
                        moveInFolder(new File(filein), richPath + "\\" + dstFolder);
                        record.setField("CTFLGE", "C");
                        infoLogger.log(Level.INFO, "filein: "+filein);
                        infoLogger.log(Level.INFO, "fileout: "+fileout);
                    } catch (Exception e) {
                        e.printStackTrace();
                        exceptionLogger.log(Level.SEVERE, e.getMessage());
                    }

                }
                writeFile.update(record);
                record = writeFile.readNext();
            }
            writeFile.close();  
            as400.disconnectAllServices();
            infoLogger.log(Level.INFO, "Disconnessione da AS400");

            //converto le richieste per i fornitori unix
            ConverterDH converterDh = new ConverterDH();
            CaricaVisure caricaVisure = new CaricaVisure();
            ArrayList unix = Configurazione.getUnix();
            for (int i = 0; i < unix.size(); i++) {
                //leggo il fornitore successivo
                String fornitoreUnix = (String) unix.get(i);
                //determino la cartella del fornitore
                String pathFornitoreUnix = richPath+"\\"+fornitoreUnix;
                //converto le richieste in formato unix nel file richieste.txt
                converterDh.convert(pathFornitoreUnix);
                //carico le richieste in As400 per la successiva elaborazione
                if(!fornitoreUnix.equals("UNIXPROVA")){
                    caricaVisure.carica(pathFornitoreUnix);
                }
                //carico il tracciato con le visure elaborate dal fornitore

                String pathTracciato = pathFornitoreUnix + "\\Visu\\" +
                        Configurazione.getConfig("filenameTracciato");
                try {
                    if (new File(pathTracciato).exists()) {
                        TracciatoUnix tracciato = new TracciatoUnix(pathTracciato);
                        tracciato.writeOnAS400(Configurazione.getConfig("CARICAAS400"),
                                Configurazione.getConfig("libreriaUnixAS400"),
                                Configurazione.getConfig("fileUnixAS400"));
                    }else{
                        infoLogger.info("Nessun tracciato presente in"+
                                pathFornitoreUnix + "\\Visu\\");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionLogger.severe("Errore nella lettura del Tracciato " + pathTracciato);
                }
            }
            
            infoLogger.log(Level.WARNING, "---- TERMINATO LO SMISTAMENTO ----");
            
            closeHandlers(infoLogger.getHandlers());
            closeHandlers(exceptionLogger.getHandlers());

        } catch (Exception e) {
            e.printStackTrace();
            exceptionLogger.log(Level.SEVERE, e.getMessage());
        }

    }


    public void richiesteSuAs400() throws Exception{
        // File per scrittura su AS400
        writeFile = new SequentialFile(as400, "/QSYS.LIB/" + Configurazione.getConfig("LIBRERIA") + ".LIB/SACCO00F.FILE");
        recordWriteDescription = new AS400FileRecordDescription(as400, "/QSYS.LIB/" + Configurazione.getConfig("LIBRERIA") + ".LIB/SACCO00F.FILE");
        writeformat = recordWriteDescription.retrieveRecordFormat();
        writeFile.setRecordFormat(writeformat[0]);
        writeFile.open(AS400File.READ_WRITE, 100, AS400File.COMMIT_LOCK_LEVEL_NONE);

        File dir = new File(Configurazione.getConfig("richpath")+"\\INARRIVO");
            File[] clientFolders = dir.listFiles();
            for (int j=0; j<clientFolders.length; j++){
                if(clientFolders[j].isDirectory()){
                    File[] vmfiles = clientFolders[j].listFiles();
                    for (int i = 0; i < vmfiles.length; i++) {
                        if (vmfiles[i].getName().indexOf("VM2") > -1) {
                        BufferedReader br = null;
                        try {
                            boolean exit;
                            br = new BufferedReader(new FileReader(vmfiles[i]));
                            // Pulisce campi
                            exit = false;
                            kcons = "";
                            dcons = "";
                            kdric = "";
                            kdsca = "";
                            kdagg = "";
                            kcodi = "";
                            knome = null;
                            rifac = "";
                            preve = "";
                            datana = "";
                            cfpiva = "";
                            numNote = "";
                            codcli = "";
                            note = "";
                            auth = "";
                            tiporic = "";
                            
                            while ((input = br.readLine()) != null) {
                                if ((input.length() > 9) && (input.indexOf("tipo_ric=") == 0)) {
                                    tiporic = input.substring(9).trim();
                                }
                                if ((input.length() > 14) && (input.indexOf("agg_dal_tras1=") == 0)) {
                                    kdagg = input.substring(14).trim();
                                }
                                if ((input.length() > 8) && (input.indexOf("Codcons=") == 0)) {
                                    kcons = input.substring(8).trim();
                                }
                                if ((input.length() > 9) && (input.indexOf("Data_ute=") == 0)) {
                                    kdric = input.substring(9).trim();
                                }
                                if ((input.length() > 14) && (input.indexOf("data_sca_forn=") == 0)) {
                                    kdsca = input.substring(14).trim();
                                }
                                if ((input.length() > 8) && (input.indexOf("NumPrat=") == 0)) {
                                    kcodi = input.substring(8).trim();
                                    // Nel caso il codice sia uno di quelli nuovi ossia lungo 8 togliamo la cifra più a sinistra
                                    if(kcodi.trim().length() == 8){
                                        kcodi = kcodi.substring(1);
                                    }
                                }
                                if ((input.length() > 11) && (input.indexOf("Nominativo=") == 0)) {
                                    knome = input.substring(11).trim().split(",");
                                }
                                if ((input.length() > 12) && (input.indexOf("rifacimento=") == 0)) {
                                    rifac = input.substring(12).trim();
                                }
                                if ((input.length() > 7) && (input.indexOf("Datana=") == 0)) {
                                    datana = input.substring(7).trim();
                                }
                                if ((input.length() > 7) && (input.indexOf("codfis=") == 0)) {
                                    cfpiva = input.substring(7).trim();
                                }
                                if ((input.length() > 6) && (input.indexOf("cfsoc=") == 0)) {
                                    cfpiva = input.substring(6).trim();
                                }
                                if ((input.length() > 5) && (input.indexOf("piva=") == 0)) {
                                    cfpiva = input.substring(5).trim();
                                }
                                if ((input.length() > 11) && (input.indexOf("Limite_aut=") == 0)) {
                                    numNote = input.substring(11).trim();
                                    while(numNote.length()<4)
                                        numNote = "0"+numNote;
                                }
                                if ((input.length() > 8) && (input.indexOf("Autoriz=") == 0)) {
                                    auth = input.substring(8).trim();
                                }
                                if ((input.length() > 6) && (input.indexOf("Notes=") == 0)) {
                                    Pattern pattern = Pattern.compile(".*\\((.*)\\).*");
                                    Matcher matcher = pattern.matcher(input);
                                    if (matcher.matches()) {
                                        codcli = matcher.group(1);
                                    }
                                }
                            }
                            //Chiude il file
                            br.close();
                            //scrivo richiesta su AS400
                            Record writeRecord = new Record(writeformat[0]);
                            try
                            {
                                writeRecord.setField("S0CHIE", clientFolders[j].getName());
                            }
                            catch(Exception ex)
                            {
                                writeRecord.setField("S0CHIE", "AAAA");
                            }
                            writeRecord.setField("S0FORN", "SEBA");
                            writeRecord.setField("S0SUBF", "");
                            
                            writeRecord.setField("S0CODH", kcons);
                            //Setto il flag da smistare
                            writeRecord.setField("S0FLDR", "DA SMISTARE");
                            //Controllo Rifacimento ed imposto su AS
                            if (rifac.toLowerCase().contains("s")) {
                                writeRecord.setField("S0TIPO", "R");
                                writeRecord.setField("S0CODI", "R"+kcodi);
                            } else {
                                writeRecord.setField("S0TIPO", "V");
                                writeRecord.setField("S0CODI", "V"+kcodi);
                            }
                            //Controllo data di nascita ed imposto CTDNAS
                            if ((datana != null) && (datana.length() > 2)) {
                                writeRecord.setField("S0DNAS", BigDecimal.valueOf(Double.parseDouble(datana)));
                            }
                            
                            if ((kdric != null) && (kdric.length() > 2)) { 
                                writeRecord.setField("S0DRIC", BigDecimal.valueOf(Double.parseDouble(kdric)));
                            }                              

                            // Imposto la data di aggiornamento visura
                            if ((kdagg != null) && (kdagg.length() > 9)) {  
                                String temp = kdagg.substring(6,10) + kdagg.substring(3,5) + kdagg.substring(0,2);
                                writeRecord.setField("S0DAGG", BigDecimal.valueOf(Double.parseDouble(temp.trim()))); 
                            } 


                            try
                            {        
                                writeRecord.setField("S0DSCA", BigDecimal.valueOf(Double.parseDouble(kdsca)));
                            } catch(Exception err) 
                            {
                                System.out.println("Errore nella data scadenza : " + kdsca);
                            }

                            // Controllo tipo richiesta
                            if ((tiporic != null) && (tiporic.equals("A"))) {
                                writeRecord.setField("S0TRIC", "07");
                            } else {
                                writeRecord.setField("S0TRIC", "01");
                            }

                            // Se società non imposto il cognome
                            if (knome[0].trim().length() > 40) 
                            {
                                writeRecord.setField("S0RSO1", knome[0].trim().substring(0, 39));
                            }
                            else
                            {
                                writeRecord.setField("S0RSO1", knome[0].trim());
                            }
    
                            try{
                                writeRecord.setField("S0RSO2", knome[1]); 
                            }
                            catch(Exception ex){
                                writeRecord.setField("S0RSO2", "");
                            }
                            writeRecord.setField("S0FLGE", "");
                            writeRecord.setField("S0CDFI", cfpiva);
                            writeRecord.setField("S0NPRE", numNote);
                            writeRecord.setField("S0CLIE", codcli);
                            writeFile.write(writeRecord);
                            // Creo la cartella di salvataggio generale
                            String saveGen = Configurazione.getConfig("savegen");
                            File saveDir = new File(saveGen);
                            if (!saveDir.exists()) {
                                saveDir.mkdir();
                            }
                            // Copia il file nella cartella di salvataggio generale
                            File temp2 = new File(saveGen + "\\" + vmfiles[i].getName());
                            copy(vmfiles[i], temp2);
                            moveInFolder(vmfiles[i], Configurazione.getConfig("richpath")+"\\DASMISTARE\\");
                            infoLogger.log(Level.INFO, "Salvato file richieste " + vmfiles[i].getName());
                        } catch (IOException ex) {
                            Logger.getLogger(Smistatore.class.getName()).log(Level.SEVERE, null, ex);
                        }finally {
                            try {
                                br.close();
                            } catch (IOException ex) {
                                Logger.getLogger(Smistatore.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        }
                    }
                }
            }

            writeFile.close();
    }


    //Smista file: sposta un file in una cartelle specificata    
    // Argomenti
    //      String filePath : cartella di destinazione
    //      String vmfile: istanza del file da smistare
    //   
    public void moveInFolder(File file, String folder) throws Exception {
        // Crea la cartella
        File newDir = new File(folder);
        if (!newDir.exists()) {
            newDir.mkdir();
        }
        // Sposta il file 
        File temp = new File(folder + "\\" + file.getName());

        //controllo se il file di destinazione già esiste
        if (temp.exists()) {
            infoLogger.log(Level.INFO, "il file" + temp.getName() +
                    " esistente nella cartella " + folder + " verrà sovrascritto");
            temp.delete();
        }
        if (file.renameTo(temp)) {
            infoLogger.log(Level.INFO, "Smistato file richieste " + file.getName());
        } else {
            infoLogger.log(Level.INFO, "Errore nella rinomina del file " + file.getName());
        }

    }

    // Copia due file 
    void copy(File src, File dst) throws IOException {
        
        if(dst.exists())
            dst.delete();
        
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    void closeHandlers(Handler[] handlers) {
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].close();
        }
    }
}
