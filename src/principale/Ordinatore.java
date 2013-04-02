package principale;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Ordinatore extends Thread {
	/**
	 * Indica se questo thread sta facendo qualcosa
	 * */
	public volatile boolean starving=true;
	/**
	 * Indica se questo thread deve terminarsi
	 * */
	public volatile boolean terminabile=false;
	private int[] ind;
	public void run(){
		while(true){
			if(terminabile) return;
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
			System.out.println("azione: "+azione.inizio+"-"+azione.fine);
			//se sono due o uno, li sposto e basta senza accodarli
			if(azione.fine-azione.inizio<3){
				if(azione.fine==azione.inizio) continue;
				if(decrescenti(azione.inizio,azione.fine)){
					//vanno scambiati
					int tmp=ind[azione.inizio];
					ind[azione.inizio]=ind[azione.fine];
					ind[azione.fine]=tmp;
				}
				continue;
			}
			//sono più di 2, cerco il pivot
			//per farlo scelgo 3 elementi casuali e faccio la mediana tra essi
			Random rng = new Random();
			int a=ind[rng.nextInt(azione.fine-azione.inizio+1)+azione.inizio];
			int b=ind[rng.nextInt(azione.fine-azione.inizio+1)+azione.inizio];
			int c=ind[rng.nextInt(azione.fine-azione.inizio+1)+azione.inizio];
			int pivotInd;
			if(decrescenti(a,b)){
				if(decrescenti(b,c)) pivotInd=b;
				else
					if (decrescenti(a,c))
						pivotInd=c;
					else
						pivotInd=a;

			}
			else{
				if(decrescenti(a,c)) pivotInd=a;
				else
					if (decrescenti(b,c))
						pivotInd=c;
					else
						pivotInd=b;

			}
			//ora pivot contiene l'indice della stringa pivot
			//la estraggo per usarla velocemente
			String pivot=Gestore.chiavi.get(pivotInd);
			//avrò due indici, corrispondenti all'estremo destro e sinistro
			//li sposto finché non si incontrano, e man mano che accade inverto le coppie
			int dx=azione.fine,sx=azione.inizio;
			for(int i=sx;i<dx;i++){
				if(Gestore.chiavi.get(a).compareTo(pivot)<=0){
					int tmp=ind[i];
					ind[i]=ind[sx];
					ind[sx]=tmp;
					sx++;
				}
			}
			int tmp=ind[dx];
			ind[dx]=ind[sx];
			ind[sx]=tmp;
			//ora ho eseguito un passo del quicksort, inserisco le due nuove partizioni nella coda
			Gestore.pendenti.add(new CoppiaIndici(azione.inizio,sx));
			Gestore.pendenti.add(new CoppiaIndici(dx,azione.fine));
		}
	}
	public void start(){
		this.ind=Gestore.indici;
	}
	/**
	 * Restituisce true se i valori agli indici a e b sono in ordine decrescente
	 * altrimenti true (se sono crescenti o uguali)
	 * */
	private boolean decrescenti(int a,int b){
		return Gestore.chiavi.get(a).compareTo(Gestore.chiavi.get(b))>0;
	}
}
