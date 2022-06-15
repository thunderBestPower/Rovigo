package richiestedh;

 /* @author 05-PC02
 *
 * Per modificare il modello associato al commento di questo tipo generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
public class Richiesta{
	
    private String abi;             // ABI      :  5
    private String cab;             // CAB      :  5
    static private int nRichiesta;         //N° RICH   :  5
    private String dataRichiesta;   //Data Rich :  8 ( AAAAMMGG )
    private String codiceCons;      //Codice Cons       :  5 ( ultimo carattere Blank )
    private String codiceFornitore; //Codice Fornitore	:  5
    private String pratica;         //Pratica   : 10 ( Mettere codice del file VM2
    private String tipoRichiesta;   // Tipo Richiesta :  2
    private String cognome;         //Cognome   : 40
    private String nome;            //Nome      : 40
    private String dataNascita;     //Data nascita:  8 ( AAAAMMGG )
    private String sesso;           //Sesso :  1 ( M / F / G )
    private String codiceFiscale;   //Codice Fiscale : 16
    private String comune;          //Comune	: 30
    private String provincia;       //Provincia :  2
    private String urgente;         //Urgente	:  1 ( S / N )
    private String legale;          //Legale    :  1 ( S / N )
    private String catasti;         //Catasto	:  1 ( S / N )
    private String confini;         //Confini	:  1 ( S / N )	
    private String importoMassimo;  //Importo massimo   : 12 ( 10 + 2 dec )
    private String nNoteMax;        //N° note max       :  3
	
    public Richiesta(){
        abi = "00000";
        cab = "00000";
        codiceFornitore = "99999";
        tipoRichiesta = "01";
        nRichiesta = ContatoreUnix.getCounter();
    }

    public String getAbi(){
        int max = 5;
        return getNumerico(abi, max);
    }
    
    public String getCab(){
        int max = 5;
        return getNumerico(cab, max);
    }
    
    public String getNRichiesta(){
        int max = 5;
        return getNumerico(Integer.toString(nRichiesta), max);
    }
    
    public String getDataRichiesta(){
        int max = 8;
        return getNumerico(dataRichiesta, max);
    }
    
    public String getCodiceCons(){
        int max = 5;
        return getAlfanumerico(this.codiceCons, max);
    }
    
    public String getCodiceFornitore(){
        int max = 5;
        return this.getNumerico(this.codiceFornitore, max);
    }
    
    public String getPratica(){
        int max = 10;
        return getAlfanumerico(this.pratica, max);
    }

    public String getTipoRichiesta() {
        int max = 2;
        return getAlfanumerico(tipoRichiesta, max);
    }

    public String getCognome() {
        int max = 40;
        return getAlfanumerico(cognome, max);
    }

    public String getNome() {
        int max = 40;
        return getAlfanumerico(nome, max);
    }

    public String getDataNascita() {
        int max = 8;
        return getNumerico(dataNascita, max);
    }

    public String getSesso() {
        int max = 1;
        return getAlfanumerico(sesso, max);
    }

    public String getCodiceFiscale() {
        int max = 16;
        return getAlfanumerico(codiceFiscale, max);
    }

    public String getComune() {
        int max = 30;
        return getAlfanumerico(comune, max);
    }

    public String getProvincia() {
        int max = 2;
        return getAlfanumerico(provincia, max);
    }

    public String getUrgente() {
        int max = 1;
        return getAlfanumerico(urgente, max);
    }

    public String getLegale() {
        int max = 1;
        return getAlfanumerico(legale, max);
    }

    public String getCatasti() {
        int max = 1;
        return getAlfanumerico(catasti,max);
    }

    public String getConfini() {
        int max = 1;
        return getAlfanumerico(confini, max);
    }

    public String getImportoMassimo() {
        int max = 12;
        return getNumerico(importoMassimo, max);
    }

    public String getNNoteMax() {
        int max = 3;
        return getNumerico(nNoteMax, max);
    }
    
    public String stampaRichiesta() throws Exception{
        String temp; 
        temp = getAbi() +
               getCab() + 
               getNRichiesta() + 
               getDataRichiesta() + 
               getCodiceCons() +
               getCodiceFornitore() + 
               blank(10) +  //settorista
               getPratica() + 
               blank(15) +  //note
               getTipoRichiesta() + 
               getCognome() + 
               getNome() + 
               getDataNascita() +
               getSesso() + 
               getCodiceFiscale() + 
               getComune() + 
               blank(2) +   //provincia
               blank(20) +  //paternità
               blank(1) +   //stato
               zero(8) +    //data evasione
               zero(10) +   //codice visura fornitore
               blank(8) +   //user id
               zero(8) +    //numero iscr tribunale
               blank(2) +   //prov iscr tribunale
               zero(8) +    //n. reg ditte
               blank(2) +   //prov. reg ditte
               zero(8) +    //data monitoraggio
               zero(8) +    //data aggiornamento
               getUrgente() +
               getLegale() +
               getCatasti() +
               getConfini() + 
               "N" +        //M.60
               blank(30) +  //Referente
               blank(15) +  //Tel. referente
               blank(15) +  //Fax referente
               zero(12)  +  //importo massimo
               getNNoteMax() +
               blank(75) +  //note varie
               "N" +        //autorizzata
               zero(8)  +   //data autorizzazione
               zero(8) +    //data sospensione
               blank(20) +  //autorizzato da
               zero(7) +    //numero avviso
               zero(8) +    //data avviso
               zero(8) +    //data formalità
               zero(7)  +   //numero formalità
               blank(5) +   //filler
               zero(8)  +   //data canc
               blank(5) +   //N.Canc
               blank(33);   //filler
               
        return temp;
    }
    

    
    
    /*
     * CONTROLLI LUNGHEZZA E RIEMPIMENTO CARATTERI
     * 
     * 
     */
    
    
    public String getAlfanumerico(String str, int max){
        if(str==null)
            str = "";
        if(str.length()>max)
            return trimToMax(str, max);
        for(int i=str.length(); i<max; i++){
            str = str + ' ';
        }
        return str;
    }
    
    public String getNumerico(String str, int max){
        if(str==null)
            str = "";
        if(str.length()>max)
            return trimToMax(str, max);
        for(int i=str.length(); i<max; i++){
            str = '0' + str;
        }
        return str;
    }
    
    public String trimToMax(String str, int max){
        return str.substring(0,max);
    }

    public String blank(int length){
        return getAlfanumerico("", length);
    }
    
    public String zero(int length){
        return getNumerico("", length);
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public void setCab(String cab) {
        this.cab = cab;
    }

    public void setNRichiesta(int nRichiesta) {
        Richiesta.nRichiesta = nRichiesta;
    }

    public void setDataRichiesta(String dataRichiesta) {
        this.dataRichiesta = dataRichiesta;
    }

    public void setCodiceCons(String codiceCons) {
        this.codiceCons = codiceCons;
    }

    public void setCodiceFornitore(String codiceFornitore) {
        this.codiceFornitore = codiceFornitore;
    }

    public void setPratica(String pratica) {
        this.pratica = pratica;
    }

    public void setTipoRichiesta(String tipoRichiesta) {
        this.tipoRichiesta = tipoRichiesta;
    }

    public void setCognome(String cognome) {
        if(cognome.length()>40){
            this.cognome = cognome.substring(0,40);
            setNome(cognome.substring(40,80));
        }else{
            this.cognome = cognome;
        }
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDataNascita(String dataNascita) {
        this.dataNascita = dataNascita;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public void setUrgente(String urgente) {
        this.urgente = urgente;
    }

    public void setLegale(String legale) {
        this.legale = legale;
    }

    public void setCatasti(String catasti) {
        this.catasti = catasti;
    }

    public void setConfini(String confini) {
        this.confini = confini;
    }

    public void setImportoMassimo(String importoMassimo) {
        this.importoMassimo = importoMassimo;
    }

    public void setNNoteMax(String nNoteMax) {
        this.nNoteMax = nNoteMax;
    }


}
