import java.io.*;
import java.net.*;

// Runnable olio josta muodostetaan summapalvelijoiden s‰ikeet
public class Summaaja implements Runnable {
	private int port;
	private ServerSocket serverSocket;
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;
	private int x;
	private int indeksi;

	// Konstruktori
	public Summaaja(int portti, int indeksi){
		this.port = portti;
		this.indeksi = indeksi;
	}

	// S‰ikeen suorituksessa suorittuva metodi
	public void run(){

		// Yritet‰‰n luoda serversoketti ja soketti
		try{
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(5000);
			socket = serverSocket.accept();
		}catch(Exception e){
			System.out.println("Soketteja ei pystytty luomaan.");
			return;
		}

		// Yritet‰‰n luoda sis‰‰ntulovirta ja oliosis‰‰ntulovirta
		try{
			in = socket.getInputStream();
			objectIn = new ObjectInputStream(in);
		}catch(IOException e){
			System.out.println("Datavirtoja ei pystytty luomaan.");
			return;
		}

		while(true){
			
			// Yritet‰‰n lukea lukua oliosis‰‰ntulovirrasta
			try{
				x = objectIn.readInt();
			}catch(IOException e){
				break;
			}
			
			// Jos x on 0 suljetaan virrat ja yhteys sek‰ poistutaan while loopista ja lopetetaan s‰ikeen suoritus automaattisesti.
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
			
			// Lis‰t‰‰n lokero-olioon sis‰‰ntulovirrasta saatu luku.
			Hajoyhteys.lokerot.lisaa(indeksi, x);
		}
	}
}
