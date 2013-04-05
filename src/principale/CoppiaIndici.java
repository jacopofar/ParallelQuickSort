package principale;

/**Contiene due indici interi, simile a Point()*/
public final class CoppiaIndici {
	public int inizio;
	public int fine;
	public CoppiaIndici(int i,int f){
		this.inizio=i;
		this.fine=f;
	}
	
	public int hashCode(){
		return inizio*37+fine;
	}
	public boolean equals(Object o){
		if(!(o instanceof CoppiaIndici)) return false;
		if(((CoppiaIndici)o).inizio!=inizio) return false;
		if(((CoppiaIndici)o).fine!=fine) return false;
		return true;
		
	}
}
