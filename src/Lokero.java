
public class Lokero {
	int[] lista;
	int lukumaara;

	// konstruktori
	public Lokero(int pituus){
		this.lista = new int[pituus];
	}
	
	// palauttaa kertyneiden lukujen kokonaismaaran
	public int getMaara(){
		return this.lukumaara;
	}

	// summaa listan kohtaan "kohta" "maaran"
	public void lisaa(int kohta, int maara){
		this.lista[kohta] += maara;
		this.lukumaara++;
	}
	
	// palauttaa listan "lokeron" maarittaman kohdan
	public int getLokero(int kohta){
		return this.lista[kohta];
	}
	
	//palauttaa koko listan
	public int[] getLista(){
		return this.lista;
	}
}
