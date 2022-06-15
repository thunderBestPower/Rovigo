/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package richiestedh;

import java.io.*;
import java.util.regex.*;
/**
 *
 * @author 05-PC02
 */
public class ParserRichiesta {
    String txtVm2;
    
    public ParserRichiesta(File file) throws Exception{
        BufferedReader in = new BufferedReader(new FileReader(file));
        txtVm2 = new String();
        
        for(String temp=""; temp!=null; temp = in.readLine())
            txtVm2 = txtVm2 + temp +"\n";
        
        in.close();
        //System.out.println(txtVm2);
    }
    
    public Richiesta parse() throws Exception{
        Richiesta rich = new Richiesta();
        rich.setDataRichiesta(getElementByTagName("Data_ute"));
        rich.setCodiceCons(getCodConservatoria());
        rich.setPratica(getElementByTagName("NumPrat"));
        if(getElementByTagName("Sesso").equals("")){            
            rich.setCognome(getElementByTagName("Nominativo"));
        }else{
            rich.setCognome(getElementByTagName("Nominativo").split(",")[0]);
            rich.setNome(getElementByTagName("Nominativo").split(",")[1]);
        }
        rich.setDataNascita(getElementByTagName("Datana"));
        rich.setSesso(getElementByTagName("Sesso"));
        rich.setCodiceFiscale(getElementByTagName("Cf"));
        rich.setComune(getElementByTagName("luogona"));
        rich.setUrgente(getElementByTagName("Urgenza"));
        rich.setLegale(getElementByTagName("Legale"));
        rich.setCatasti(getElementByTagName("Catasto_cli"));
        rich.setConfini(getElementByTagName("Coerenze"));
        rich.setNNoteMax(getElementByTagName("Limite_aut"));
        return rich;
    }
    
    public String getElementByTagName(String tag) throws Exception{
        String patternString = tag+"=(.*)\n";
        
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(txtVm2);
        if(matcher.find()){
            for (int i=0; i<=matcher.groupCount(); i++) {
            //System.out.println(matcher.group(i));
            }
            return matcher.group(1);
        }
        
        throw new Exception("Nessun elemento trovato con il tag " + tag); 
    }
    
    public String getCodConservatoria() throws Exception{
        
        String codcons = getElementByTagName("Codcons");
        //Recupero il pezzo di file contenete le conservatorie
        String patternString = "Conservatorie(.*)FineConservatorie";
        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(txtVm2);
        if(matcher.find()){
            String conservatorieStr = matcher.group(1);

            patternString = codcons+"(\\p{Alpha}\\d\\d\\d)";
            pattern = Pattern.compile(patternString);
            matcher = pattern.matcher(conservatorieStr);

            if(matcher.find())
                return matcher.group(1);
        }
        throw new Exception(""); 
    }
    
    
}
