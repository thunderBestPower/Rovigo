/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package richiestedh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author koalacurioso
 */
public class ContatoreUnix{
    private static int counter = 0;
    private static Calendar date = GregorianCalendar.getInstance();
    private static File file = new File("savedUnixCouter.dat");
    private static Logger exceptionLogger = Logger.getLogger("exceptions");
    private static Logger infoLogger = Logger.getLogger("ftpRoGetRich");

    private static boolean isNewerDate(Calendar newDate) {
        if(date.get(Calendar.YEAR)<newDate.get(Calendar.YEAR) ||
           date.get(Calendar.MONTH)<newDate.get(Calendar.MONTH) ||
           date.get(Calendar.DATE)<newDate.get(Calendar.DATE)){
           return true;
        }
        else{
            return false;
        }
    }
    
    public static int getCounter(){
        if (counter == 0 && file.exists()) {
            infoLogger.log(Level.INFO, "Ripristino dei contatori da");
            infoLogger.log(Level.INFO, "contatore: "+counter);
            infoLogger.log(Level.INFO, "data: "+date.get(Calendar.YEAR)+"\\"+
                    date.get(Calendar.MONTH)+"\\"+date.get(Calendar.DATE));
            restore();
            infoLogger.log(Level.INFO, "contatore: "+counter);
            infoLogger.log(Level.INFO, "data: "+date.get(Calendar.YEAR)+"\\"+
                    date.get(Calendar.MONTH)+"\\"+date.get(Calendar.DATE));
        }
        Calendar newDate = GregorianCalendar.getInstance();
        if(isNewerDate(newDate))
            reset();
        counter++;
        infoLogger.log(Level.INFO, "Contatore Incrementato a "+counter);
        backup();
        return counter-1;
    }

    private static void reset() {
        counter=0;
        date = GregorianCalendar.getInstance();
        infoLogger.log(Level.INFO, "Resettati i contatori");
    }
    
    private static void restore(){     
        ObjectInputStream objIn = null;
        try {
            objIn = new ObjectInputStream(new FileInputStream(file));
            Object obj = objIn.readObject();
            if (obj instanceof Integer) {
                counter = ((Integer) obj).intValue();
            }
            obj = objIn.readObject();
            if (obj instanceof Calendar) {
                date = (Calendar) obj;
            }
        } catch (IOException ex) {
            Logger.getLogger(ContatoreUnix.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ContatoreUnix.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objIn.close();
                file.delete();
            } catch (IOException ex) {
                Logger.getLogger(ContatoreUnix.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void backup(){
        
        file.delete();
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(new FileOutputStream(file));
            objOut.writeObject(new Integer(counter));
            objOut.writeObject(date);
        } catch (IOException ex) {
            Logger.getLogger(ContatoreUnix.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objOut.close();
            } catch (IOException ex) {
                Logger.getLogger(ContatoreUnix.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
