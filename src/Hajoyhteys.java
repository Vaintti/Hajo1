import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.Thread;

public class Hajoyhteys {
	static Socket tcpSocket;
	static ServerSocket tcpServerSocket;
	
	public static Lokero lokerot;

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
		objectOut.flush();
		// Asetetaan tcp socketin aikakatkaisun aikaraja
		tcpSocket.setSoTimeout(5000);
		// Yritet‰‰n lukea objektivirrasta summauspalvelimien m‰‰r‰
		int t = 0;
		try{
			t = objectIn.readInt();
		}catch(SocketException e){
			objectOut.writeInt(-1);
			objectOut.flush();
			System.exit(0);
		}
		// L‰hetet‰‰n summauspalvelimien portteja
		System.out.println("L‰hetet‰‰n " + t + " porttia");
		for(int tt = 0; tt  < t; tt++){
			System.out.println("Kirjoitetaan oliovirtaan porttia " + (Integer.parseInt(tcpPort) + 1 + tt));
			objectOut.writeInt(Integer.parseInt(tcpPort) + 1 + tt);
			objectOut.flush();
			System.out.println("Kirjoitettu oliovirtaan");
		}
		System.out.println("Luodaan summapalveluita");
		// M‰‰ritell‰‰n lokero-olio loppuun kun tiedet‰‰n sen pituus
		lokerot = new Lokero(t);
		for(int x = 0; x < t; x++){
			Summaaja summaaja = new Summaaja(Integer.parseInt(tcpPort) + 1 + x, x);
			(new Thread(summaaja)).start();
			System.out.println("Summapalvelu " + x + " k‰ynnistetty");
		}
		int kysely;
		while(true){
			System.out.println("Odotetaan kyselyj‰");
			try{
				tcpSocket.setSoTimeout(60000);
				kysely = objectIn.readInt();
				System.out.println("Kysely " + kysely + " vastaanotettu");
				if(kysely == 0){
					System.out.println("Lopetuskysely vastaanotettu. Lopetetaan");
					System.exit(0);
				}
				else if(kysely == 1){
					System.out.println("Summakysely vastaanotettu. Palautetaan kaikkien lukujen summa");
					objectOut.writeInt(getSummienSumma(lokerot.getLista()));
					objectOut.flush();
				}
				else if(kysely == 2){
					System.out.println("Suuruuskysely vastaanotettu. Palautetaan suurin summa");
					objectOut.writeInt(getSuurin(lokerot.getLista()));
					objectOut.flush();
				}
				else if(kysely == 3){
					System.out.println("M‰‰r‰kysely vastaanotettu. Palautetaan vastaanotettujen lukujen lukum‰‰r‰.");
					objectOut.writeInt(lokerot.getMaara());
					objectOut.flush();
				}
				else{
					System.out.println("Virheellinen kysely vastaanotettu. Kirjoitetaan virtaan -1");
					objectOut.writeInt(-1);
					objectOut.flush();
				}
			}catch(Exception e){
				System.out.println("Kyselyvirhe. Lopetetaan");
				System.exit(0);
			}
		}
	}
	//Etsit‰‰n ja palautetaan listan suurin summa
	static int getSuurin(int[] lista){
		int x = 0;
		for(int i = 1; i < lista.length; i++){
			if(lista[i] > lista[x]){
				x = i;
			}
		}
		return x+1;
	}
	//Etsit‰‰n ja palautetaan listan summien summa
	static int getSummienSumma(int[] lista){
		int y = lista[0];
		for(int i = 1; i < lista.length; i++){
			y = y + lista[i];
		}
		return y;
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
