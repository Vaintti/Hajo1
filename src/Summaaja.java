import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;

public class Summaaja implements Runnable {
	int port;
	ServerSocket serverSocket;
	Socket socket;
	InputStream in;
	OutputStream out;
	ObjectInputStream objectIn;
	ObjectOutputStream objectOut;
	int x;
	int indeksi;
	
	public Summaaja(int portti, int indeksi){
		this.port = portti;
		this.indeksi = indeksi;
	}
	public void run(){
		try{
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(5000);
			socket = serverSocket.accept();
		}catch(Exception e){
			System.out.println("Soketteja ei pystytty sitomaan.");
		}
		try{
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			ObjectInputStream objectIn = new ObjectInputStream(in);
			ObjectOutputStream objectOut = new ObjectOutputStream(out);
		}catch(Exception e){
			System.out.println("Datavirtoja ei pystytty avaamaan.");
		}
		while(true){
			try{
				socket.setSoTimeout(1000);
				x = objectIn.readInt();
			}catch(Exception e){
				System.out.println("Syötettä ei pystytty lukemaan.");
			}
			if(x == 0){
				//lopetetaan
			}
			Hajoyhteys.lokerot.lisaa(indeksi, x);
		}
	}
}
