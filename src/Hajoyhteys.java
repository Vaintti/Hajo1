import java.net.*;
import java.util.*;
import java.io.*;

public class Hajoyhteys {
	static Socket tcpSocket;
	static ServerSocket tcpServerSocket;

	public static void main(String[] args) throws Exception {
		// Udp portti jonne yhteydenottoviesti l‰hetet‰‰n 
		int udpPort = 3126;
		// Tcp-portti johon palvelimen pyydet‰‰n ottamaan yhteytt‰
		String tcpPort = "10000";
		// Tcp-portti bittein‰
		byte[] tcpPortBytes = tcpPort.getBytes();
		// Osoite johon otetaan yhteytt‰
		InetAddress os = InetAddress.getByName("localhost");
		// maksimim‰‰r‰ ep‰onnistuneita yhteydenottoja
		int connectionFails = 5;
		// M‰‰ritet‰‰n tcp serversocket
		tcpServerSocket = new ServerSocket(Integer.parseInt(tcpPort));
		while (connectionFails > 0) {
			try {
				// L‰hetet‰‰n tcp portti udp pakettina
				sendTCPPort(tcpPortBytes, os, udpPort);
				System.out.println("Sending TCP port in UDP packet...");
				// Asetetaan yhteyden aikakatkaisun aikarajaksi 5 sekuntia
				tcpServerSocket.setSoTimeout(5000);
				// Hyv‰ksyt‰‰n tcp yhteys ja sidotaan se tcp sockettiin
				tcpSocket = tcpServerSocket.accept();
				System.out.println("TCP-connection set up.");
				break;
			} catch (SocketTimeoutException e) {

			}
			// Yhteydenoton ep‰onnistumislaskuria v‰hennet‰‰n
			connectionFails--;
			System.out.println("Connection failed.");
		}
		// Kun yhteydenotto ep‰onnistuu liian monta kertaa suljetaan ohjelma
		if (connectionFails == 0) {
			System.out.println("Connection failed too many times. Terminating process.");
			Thread.sleep(5000);
			System.exit(0);
		}
		// Luodaan socketit ja avataan oliovirrat
		System.out.println("Starting communication with client.");
		InputStream in = tcpSocket.getInputStream();
		OutputStream out = tcpSocket.getOutputStream();
		ObjectInputStream objectIn = new ObjectInputStream(in);
		ObjectOutputStream objectOut = new ObjectOutputStream(out);
		// Asetetaan tcp socketin aikakatkaisun aikaraja
		tcpSocket.setSoTimeout(5000);
		// Yritet‰‰n lukea objektivirrasta summauspalvelimien m‰‰r‰
		int t = 0;
		try{
			t = objectIn.readInt();
		}catch(SocketException e){
			objectOut.writeInt(-1);
			System.exit(0);
		}
		// L‰hetet‰‰n summauspalvelimien portteja
		System.out.println("L‰hetet‰‰n " + t + "porttia");
		for(int tt = 0; tt  < t; tt++){
			objectOut.writeInt(Integer.parseInt(tcpPort) + 1 + tt);
		}

	}

	// L‰hett‰‰ tcp portin udp pakettina
	public static void sendTCPPort(byte[] tcpPortBytes, InetAddress os, int udpPort) {
		try {
			// Luodaan udp socketti ja paketti.
			DatagramSocket udpSocket = new DatagramSocket();
			DatagramPacket udpPacket = new DatagramPacket(tcpPortBytes, tcpPortBytes.length, os, udpPort);
			// L‰hetet‰‰n paketti ja suljetaan socketti.
			udpSocket.send(udpPacket);
			udpSocket.close();
		} catch (Exception e) {
			
		}
	}

}
