package principale;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public final class Avvio {
	/**
	 * mappa di sola lettura contenente il file intero
	 * */
	public static final HashMap<Integer,String> contenuto=new HashMap<Integer,String>(2000);
	/**
	 * mappa di sola lettura contenente gli indici delle chiavi
	 * */
	public static final HashMap<Integer,String> chiavi=new HashMap<Integer,String>(2000);
	public static int[] indici=null;
	
	/**
	 * @param args gli argomenti passati dalla linea di comando
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long avvio=System.currentTimeMillis();
		if(args.length!=4){
			System.err.println("Errore, devi inserire quattro argomenti:" +
					"\n*il percorso del file TSV" +
					"\n*L'indice della colonna su cui ordinare" +
					"\n*Il numero di thread massimi (da 1 a 12)" +
			"\n*Il file per l'output ordinato");
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
		String outputFile=args[0];
		//carichiamo il file in memoria, memorizzando il campo su cui avverrà l'ordinamento sotto forma di valore e di chiave
		FileInputStream fs=null;
		try {
			fs = new FileInputStream(inputFile);
		} catch (FileNotFoundException e) {
			System.err.println("Errore di lettura del file "+inputFile);
			e.printStackTrace();
			System.exit(4);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fs)));
		String strl;
		int ind=0;
		while((strl=br.readLine())!=null){
			String[] vals = strl.split("\t",-1);
			contenuto.put(ind, strl);
			chiavi.put(ind, vals[indice]);
			ind++;
		}
		indici=new int[ind];
		for(int i=0;i<ind;i++){
			indici[i]=1;
		}
		System.out.println("File caricato, ho impiegato "+(System.currentTimeMillis()-avvio)+"ms");
		avvio=System.currentTimeMillis();
	}

}
