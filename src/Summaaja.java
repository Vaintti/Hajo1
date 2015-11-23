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
			return;
		}
		try{
			in = socket.getInputStream();
			out = socket.getOutputStream();
			objectIn = new ObjectInputStream(in);
			objectOut = new ObjectOutputStream(out);
		}catch(Exception e){
			System.out.println("Datavirtoja ei pystytty avaamaan.");
			return;
		}
		while(true){
			try{
				socket.setSoTimeout(10000);
				x = objectIn.readInt();
			}catch(Exception e){
				System.out.println("Syötettä ei pystytty lukemaan.");
				break;
			}
			if(x == 0){
				break;
			}
			System.out.println("Lisätään indeksiin " + indeksi + " luku " + x);
			Hajoyhteys.lokerot.lisaa(indeksi, x);
		}
		try{
			socket.close();
			serverSocket.close();
		}catch(Exception e){
		}
	}
}
