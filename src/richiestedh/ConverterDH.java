/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package richiestedh;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 05-PC02
 */
public class ConverterDH {
    private String richiesteVM2;
    private String richiesteIn;
    private String richiesteOut;
    private String richiesteErr;
    
    Logger infoLogger, exceptionLogger;
    
    
    private void setPath(String path){
        richiesteIn = path;
        richiesteOut = path+"\\Rich";
        richiesteErr = path+"\\Err";
        richiesteVM2 =  richiesteOut+"\\VM2";
        
        createPath(richiesteIn);
        createPath(richiesteOut);
        createPath(richiesteErr);
        createPath(richiesteVM2);
    }
    
    private void createPath(String path){
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
            infoLogger.log(Level.INFO, "Creazione cartelle per: "+path);
        }
    }
    /**
     * @param args the command line arguments
     */
    public void convert(String path) {
        exceptionLogger = Logger.getLogger("exceptions");
        infoLogger = Logger.getLogger("ftpRoGetRich");
        setPath(path);
        
        try{
            //Apro lo stream in output sul file di conversione
            File folderOut = new File(richiesteOut);
            BufferedWriter out = new BufferedWriter(new FileWriter(richiesteOut+"\\richieste.txt", true));
            
            infoLogger.log(Level.INFO, "Inizio conversione in formato Unix per: "+richiesteOut);
            //Legge i file nella cartella di input
            File folderIn = new File(richiesteIn);
            String[] filesIn = folderIn.list();    
            String richiestaConvertita;
            for(int i=0; i<filesIn.length; i++){
                infoLogger.log(Level.INFO, "ciclo di elaborazione");
                //elaboro i file che hanno estensione .vm2
                if(filesIn[i].endsWith(".VM2")){
                    //creo l'istanza del file
                    File file = new File(richiesteIn +"\\"+ filesIn[i]);
                    //converto la richiesta in tracciato
                    richiestaConvertita = new ParserRichiesta(file).parse().stampaRichiesta(); 
                    //se la richiesta è stata convertita correttamente
                    if(richiestaConvertita!=null){
                        //scrivo sul buffer in uscita la stringa convertita
                        out.write(richiestaConvertita+"\n");
                        infoLogger.log(Level.INFO, "Convertito il file: "+file);
                        //copio il vm2 convertito nella cartella appropriata
                        copy(file, new File(richiesteVM2+"\\"+ filesIn[i]));
                    }
                    else{
                        infoLogger.log(Level.WARNING, "Errore nella conversione del file: "+file);
                        file.renameTo(new File(richiesteErr +"\\"+filesIn[i]));                        
                    }
                }
            }
            out.close();
            infoLogger.log(Level.INFO, "Conversione del traccito Unix per: "+richiesteOut);
        }catch(Exception e){
            exceptionLogger.log(Level.WARNING, e.getMessage());
        }
    }
   
        // Copia due file 
    static void copy(File src, File dst) throws IOException {

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
}
