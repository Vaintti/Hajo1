// S‰ilytt‰‰ summat ja saatujen lukujenlukum‰‰r‰n
public class Lokero {
	private int[] lista;
	private int lukumaara;

	// konstruktori
	public Lokero(int pituus) {
		this.lista = new int[pituus];
	}

	// palauttaa kertyneiden lukujen kokonaismaaran
	public int getMaara() {
		System.out.println("Lukuja on vastaanotettu " + this.lukumaara);
		return this.lukumaara;
	}

	// summaa listan kohtaan "kohta" "maaran"
	public synchronized void lisaa(int kohta, int maara) {
		this.lista[kohta] += maara;
		this.lukumaara++;
	}

	// palauttaa listan "lokeron" maarittaman kohdan
	public int getLokero(int kohta) {
		return this.lista[kohta];
	}

	// palauttaa koko listan
	public int[] getLista() {
		return this.lista;
	}

	// Etsit‰‰n ja palautetaan listan suurin summa
	public int getSuurin() {
		int x = 0;
		for (int i = 1; i < this.lista.length; i++) {
			if (this.lista[i] > this.lista[x]) {
				x = i;
			}
		}
		System.out.println("Suurin summa on summa numero " + (x+1));
		return x+1;
	}

	// Etsit‰‰n ja palautetaan listan summien summa
	public int getSummienSumma() {
		int y = 0;
		for (int i = 0; i < this.lista.length; i++) {
			y += this.lista[i];
		}
		System.out.println("Summa on " + y);
		return y;
	}
}
