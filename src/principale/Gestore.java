package principale;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Classe che gestisce l'ordinamento con quicksort parallelo di un file di testo.
 * */
public final class Gestore {
	/**
	 * mappa di sola lettura contenente il file intero
	 * */
	public static String[] contenuto;
	/**
	 * Indica il Locale da usare, in questo caso il Lituano
	 * */
	static Locale MIO_LOCALE=new Locale("en");
	/**
	 * mappa di sola lettura contenente gli indici delle chiavi
	 * */
	public static String[] chiavi;
	public static int[] indici=null;
	public static final LinkedBlockingQueue<CoppiaIndici> pendenti=new LinkedBlockingQueue<CoppiaIndici>();
	private static Ordinatore[] ordinatori=new Ordinatore[12];
	/**
	 * @param args gli argomenti passati dalla linea di comando
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long avvio=System.currentTimeMillis();
		if(args.length!=4){
			System.err.println("Errore, devi inserire quattro o cinque argomenti:" +
					"\n* il percorso del file TSV" +
					"\n* L'indice della colonna su cui ordinare" +
					"\n* Il numero di thread massimi (da 1 a 12)" +
			"\n* Il file per l'output ordinato" +
			"\n* [opzionale] Il Locale, ad esempio 'lt-LT' se non specificato utilizza 'en-US'");
			System.exit(1);
		}
		String inputFile=args[0];
		int indice=-1;
		int maxThread=-1;
		try{
			indice=Integer.parseInt(args[1]);
			if(indice<0){
				System.err.println("Errore: l'indice deve maggiore o uguale a 0");
				System.exit(2);
			}
			maxThread=Integer.parseInt(args[2]);
			if(maxThread<1 || maxThread>12){
				System.err.println("Errore: Il numero di thread deve essere compreso tra 1 e 12");
				System.exit(2);
			}
		}
		catch(NumberFormatException e){
			System.err.println("Errore, mi aspettavo un numero, invece c'era una stringa.");
			e.printStackTrace();
			System.exit(3);
		}
		String outputFile=args[3];
		if(args.length==5)
			MIO_LOCALE=new Locale(args[4]);
		System.out.println("NOTA: per confrontare le stringhe utilizzo il Locale '"+MIO_LOCALE.toLanguageTag()+"' ['"+MIO_LOCALE.getDisplayName()+"']");
		
		
		//carichiamo il file in memoria, memorizzando il campo su cui avverrà l'ordinamento sotto forma di valore e di chiave
		FileInputStream fs=null;
		try {
			fs = new FileInputStream(inputFile);
		} catch (FileNotFoundException e) {
			System.err.println("Errore di lettura del file "+inputFile);
			e.printStackTrace();
			System.exit(4);
		}
		System.out.println("Inizio a leggere il file "+inputFile);
		
		DataInputStream din = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(din));
		
		//passo il file due volte, la prima solo per contare le righe e la seconda per memorizzarle
		//ho fatto delle prove e ho visto che è più veloce fare due passate e istanziare direttamente un arraypiuttosto che usare una struttura dati
		//intermedia allocata dinamicamente
		int dimensioni=0;
		while(br.readLine()!=null)dimensioni++;
		chiavi=new String[dimensioni];
		contenuto=new String[dimensioni];
		System.out.println("ci sono "+dimensioni+" righe");
		
		din.close();
		fs.close();
		fs = new FileInputStream(inputFile);
		din = new DataInputStream(fs);
		br = new BufferedReader(new InputStreamReader(din));
		
		String strl;
		int ind=0;
		while((strl=br.readLine())!=null){
			String[] vals = strl.split("\t",-1);
			contenuto[ind]= strl;
			chiavi[ind]=vals[indice];
			ind++;
		}

		din.close();
		fs.close();
		indici=new int[ind];
		for(int i=0;i<ind;i++){
			indici[i]=i;
		}
		System.out.println("File caricato, ho impiegato "+(System.currentTimeMillis()-avvio)+"ms. Passo all'ordinamento");
		
		
		avvio=System.currentTimeMillis();
		//ora creo la coda per le azioni di ordinamento da compiere, nella forma "indice inizio,indice fine"
		try {
			pendenti.put(new CoppiaIndici(0,ind-1));
		} catch (InterruptedException e) {
			// non dovrebbe mai accadere, la dimensione della coda è Integer.MAXINT
			e.printStackTrace();
		}
		/*i thread:
		 * Faranno la pop della coda prendendo un'azione
		 * Eseguiranno l'ordinamento specificato in quell'azione agendo SOLO sull'array degli indici
		 *     (essendo un quicksort, non ci sono problemi di concorrenza perché sicuramente lavorano su parti diverse dell'array)
		 * Se necessario, faranno la push per le azioni dei due array così creati
		 * Quando la coda è vuota e i thread sono in starving, il lavoro è finito e posso scrivere il file.
		 * Non uso join e wait, perché per efficienza lascio aperta l'istanza del thread e la riutilizzo, senza chiuderla.
		 *  */
		//parto da 1, il thread 0 è il main
		for(int i=0;i<maxThread;i++){
			ordinatori[i]=new Ordinatore();
			ordinatori[i].start();
		}
		//ora controllo periodicamente che il programma stia facendo qualcosa
		//quando tutti i thread sono in starving e la lista è vuota, ho finito
		while(true){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				//non importa
			}
			System.out.println("Ci sono "+pendenti.size()+" ordinamenti pendenti");
			if(pendenti.size()>0) continue;
			for(int i=0;i<maxThread;i++)
				if (ordinatori[i].starving==false)
					continue;
			//sono arrivato qua, quindi la coda è vuota e i thread sono in starvation, esco dal ciclo e salvo il file
			break;
		}
		System.out.println("\nOrdinamento effettuato, impiegati "+(System.currentTimeMillis()-avvio)+"ms, salvo...");
		avvio=System.currentTimeMillis();
		
		FileWriter fso = new FileWriter(outputFile);
        BufferedWriter out = new BufferedWriter(fso,1024*1024*5);
		
		for(int i=0;i<ind;i++){
			 out.write(contenuto[indici[i]]+"\n");
		}
		out.close();
		System.out.println("Scrittura del file "+outputFile+" conclusa, ha richiesto "+(System.currentTimeMillis()-avvio)+"ms");
		System.exit(0);
	}
}