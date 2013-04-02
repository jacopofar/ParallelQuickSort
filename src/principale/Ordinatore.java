package principale;

import java.text.Collator;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Ordinatore extends Thread {
	
	/**
	 * Indica il locale da usare, di default il lituano
	 * */
	static final Locale MIO_LOCALE=Locale.forLanguageTag("lt_LT");
	private final Collator collator=Collator.getInstance(MIO_LOCALE);
	/**
	 * Indica se questo thread sta facendo qualcosa
	 * */
	public volatile boolean starving=true;
	/**
	 * Indica se questo thread deve terminarsi
	 * */
	public volatile boolean terminabile=false;
	private int[] ind;
	private String[] chiavi;
	public void run(){
		while(true){
			if(terminabile){
				System.out.println("richiesta di terminazione, chiudo un ordinatore");
				return;
			}
			starving=true;
			CoppiaIndici azione=null;
			try {
				azione = Gestore.pendenti.poll(20,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				//non ho trovato nulla, riprovo all'infinito ma rimango starving
				continue;
			}
			//azione è null? se sì, ripeto il ciclo aspettando
			if(azione==null) continue;
			//sono qua, quindi ho preso un'azione e non era null, non sono più starving
			starving=false;
			//azione di quicksort vera e propria
			//System.out.println("azione: "+azione.inizio+"-"+azione.fine);
			//se sono due o uno, li sposto e basta senza accodarli
			if(azione.fine-azione.inizio<2){
				if(azione.fine==azione.inizio) continue;
				if(decrescenti(chiavi[azione.inizio],chiavi[azione.fine])){
					//vanno scambiati
					scambia(azione.inizio,azione.fine);
				}
				continue;
			}
			//sono più di 2, cerco il pivot
			//per farlo scelgo 3 elementi casuali e faccio la mediana tra essi
			Random rng = new Random();
			int a=rng.nextInt(azione.fine-azione.inizio)+azione.inizio;
			int b=rng.nextInt(azione.fine-azione.inizio)+azione.inizio;
			int c=rng.nextInt(azione.fine-azione.inizio)+azione.inizio;
			String pivot;
			int ipivot;
			if(decrescenti(chiavi[a],chiavi[b])){
				if(decrescenti(chiavi[b],chiavi[c])) {
					pivot=chiavi[b];
					ipivot=b;
				}
				else
					if (decrescenti(chiavi[a],chiavi[c])){
						pivot=chiavi[c];
						ipivot=c;
					}
					else{
						pivot=chiavi[a];
						ipivot=a;
					}

			}
			else{
				if(decrescenti(chiavi[a],chiavi[c])){
					pivot=chiavi[a];
					ipivot=a;
				}
				else
					if (decrescenti(chiavi[b],chiavi[c])){
						ipivot=c;
						pivot=chiavi[c];
					}
					else{
						pivot=chiavi[b];
						ipivot=b;
					}

			}
			//ora pivot contiene la stringa pivot e ipivot il suo indice
			//System.out.println(" pivot:"+pivot);
			scambia(azione.fine,ipivot);
			int store=azione.inizio;
			for(int i=azione.inizio;i<azione.fine;i++){
				if(decrescenti(pivot,chiavi[i])){
					scambia(i,store);
					store++;
				}
			}
			scambia(store,azione.fine);
			//ora ho eseguito un passo del quicksort, inserisco le due nuove partizioni nella coda
			//System.out.println("aggiungo in coda "+azione.inizio+" "+store);
			Gestore.pendenti.add(new CoppiaIndici(azione.inizio,store));
			Gestore.pendenti.add(new CoppiaIndici(store+1,azione.fine));

			//for(int k=azione.inizio;k<azione.fine;k++) System.out.print(chiavi.get(ind[k])+", ");
			
		}
	}
	public void start(){
		this.ind=Gestore.indici;
		this.chiavi=Gestore.chiavi;
		super.start();
		collator.setStrength(Collator.TERTIARY);
	}
	/**
	 * Restituisce true se i valori agli indici a e b sono in ordine decrescente
	 * altrimenti true (se sono crescenti o uguali)
	 * */
	private boolean decrescenti(String a,String b){
		return collator.compare(a, b)>0;
		//return a.compareTo(b)>0;
	}
	/**
	 * Scambia due elementi, scambiando sia le chiavi che gli indici
	 * */
	private void scambia(int a,int b){
		int tmp=ind[a];
		ind[a]=ind[b];
		ind[b]=tmp;

		String tmps=chiavi[a];
		chiavi[a]=chiavi[b];
		chiavi[b]=tmps;
	}
}
