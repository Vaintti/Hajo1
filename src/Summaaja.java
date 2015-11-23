import java.io.*;
import java.net.*;

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
			objectIn = new ObjectInputStream(in);
		}catch(Exception e){
			System.out.println("Datavirtoja ei pystytty avaamaan.");
			return;
		}
		while(true){
			try{
				socket.setSoTimeout(10000);
				try{
					x = objectIn.readInt();
					System.out.println(x);
					if(x == 0){
						break;
					}
				}catch(EOFException eof){
					break;
				}catch(IOException io){
					break;
				}
			}catch(SocketException s){
				s.printStackTrace();
				return;
			}
			System.out.println("Lis‰t‰‰n indeksiin " + indeksi + " luku " + x);
			Hajoyhteys.lokerot.lisaa(indeksi, x);
		}
	}
}
