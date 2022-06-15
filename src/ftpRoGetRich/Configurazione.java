package ftpRoGetRich;  

import java.util.*;  
import java.io.*;

public class Configurazione {
 
    private static Hashtable conf;
    private static Hashtable cons;
    private static ArrayList unix;

    public static void loadData() {

        conf = new Hashtable();
        cons = new Hashtable();
        unix = new ArrayList();

        // Legge il file di configurazione
        caricaHashtable(conf, "config.ini");
        caricaHashtable(cons, "conservatorie.ini");
        caricaArrayList(unix, "fornitoriUnix.ini");
    }

    public static String getConfig(String key){
        return (String) conf.get(key);
    }

    public static String getCons(String key){
        return (String) cons.get(key);
    }

    public static ArrayList getUnix(){
        return unix;
    }

    public static String[] getClienti(){
        return getConfig("ftpdirs").split(",");
    }
    
    private static void caricaHashtable(Hashtable hash, String filePath)
    {
        try
        {
            StringTokenizer st;
            String stringa1 = new String("");
            String stringa2 = new String("");
            String input = new String("");

            BufferedReader br = new BufferedReader(new FileReader(filePath));

            while((input = br.readLine()) != null)
            {
                if((input != null) && (input.length()>1))
                {
                    st = new StringTokenizer(input,"=");
                    stringa1 = st.nextToken();
                    try {
                        stringa2 = st.nextToken();
                        hash.put(stringa1,stringa2);
                    } catch (Exception e) {
                        
                    }
                    
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void caricaArrayList(ArrayList array, String filePath) {
        try
        {
            StringTokenizer st;
            String input = new String("");

            BufferedReader br = new BufferedReader(new FileReader(filePath));

            while ((input = br.readLine()) != null) {
                if ((input != null) && (input.length() > 1)) {
                    array.add(input);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

}
