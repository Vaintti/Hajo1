import java.io.*;
import java.net.*;

// Runnable olio josta muodostetaan summapalvelijoiden säikeet
public class Summaaja implements Runnable {
	private int port;
	private ServerSocket serverSocket;
	private Socket socket;
	private InputStream in;
	private ObjectInputStream objectIn;
	private int x;
	private int indeksi;
	private boolean running;

	// Konstruktori
	public Summaaja(int portti, int indeksi){
		this.port = portti;
		this.indeksi = indeksi;
	}
	
	public void sammuta(){
		running = false;
	}

	// Säikeen suorituksessa suorittuva metodi
	public void run(){
		running = true;

		// Yritetään luoda serversoketti ja soketti
		try{
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(5000);
			socket = serverSocket.accept();
		}catch(Exception e){
			System.out.println("Soketteja ei pystytty luomaan.");
			return;
		}

		// Yritetään luoda sisääntulovirta ja oliosisääntulovirta
		try{
			in = socket.getInputStream();
			objectIn = new ObjectInputStream(in);
		}catch(IOException e){
			System.out.println("Datavirtoja ei pystytty luomaan.");
			return;
		}

		while(running){
			
			// Yritetään lukea lukua oliosisääntulovirrasta
			try{
				x = objectIn.readInt();
			}catch(IOException e){
				break;
			}
			
			// Jos x on 0 suljetaan virrat ja yhteys sekä poistutaan while loopista ja lopetetaan säikeen suoritus automaattisesti.
			if(x == 0){
				try{
					objectIn.close();
					in.close();
					socket.close();
					serverSocket.close();
				}catch(IOException e){
					
				}
				return;
			}
			
			// Lisätään lokero-olioon sisääntulovirrasta saatu luku.
			Hajoyhteys.lokerot.lisaa(indeksi, x);
		}
	}
}
