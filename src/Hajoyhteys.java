import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.Thread;

public class Hajoyhteys {
	static Socket tcpSocket;
	static ServerSocket tcpServerSocket;
	public static Lokero lokerot;

	public static void main(String[] args) throws Exception {
		// Asetetaan p‰‰luokan prioriteetti korkeimmaksi ettei kutsujen v‰leiss‰ tapahdu muutoksia
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
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
				System.out.println("L‰hetet‰‰n TCP portti UDP paketissa...");

				// Asetetaan yhteyden aikakatkaisun aikarajaksi 5 sekuntia
				tcpServerSocket.setSoTimeout(5000);

				// Hyv‰ksyt‰‰n tcp yhteys ja sidotaan se tcp sockettiin
				tcpSocket = tcpServerSocket.accept();
				System.out.println("TCP-yhteys muodostettu.\n");
				break;
			} catch (SocketTimeoutException e) {
				System.out.println("Soketin aikakatkaisu.");
			}

			// Yhteydenoton ep‰onnistumislaskuria v‰hennet‰‰n
			connectionFails--;
			System.out.println("Connection failed.");
		}

		// Kun yhteydenotto ep‰onnistuu liian monta kertaa suljetaan ohjelma
		if (connectionFails == 0) {
			System.out.println("Yhteydenotto ep‰onnistui liian monta kertaa. Suljetaan prosessi viiden sekuntin kuluttua.");
			Thread.sleep(5000);
			System.exit(0);
		}

		// Avataan datavirrat
		System.out.println("Avataan p‰‰luokan datavirrat.");
		InputStream in = tcpSocket.getInputStream();
		OutputStream out = tcpSocket.getOutputStream();
		ObjectInputStream objectIn = new ObjectInputStream(in);
		ObjectOutputStream objectOut = new ObjectOutputStream(out);
		System.out.println("P‰‰luokan datavirrat avattu.\n");

		// Asetetaan tcp soketin aikakatkaisun aikaraja
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
		System.out.println("L‰hetet‰‰n " + t + ":n summauspalvelimen portit.");
		for(int tt = 0; tt  < t; tt++){
			System.out.println("Kirjoitetaan oliovirtaan portti " + (Integer.parseInt(tcpPort) + 1 + tt));
			objectOut.writeInt(Integer.parseInt(tcpPort) + 1 + tt);
			objectOut.flush();
			System.out.println("Portti " + (Integer.parseInt(tcpPort) + 1 + tt) + " kirjoitettu oliovirtaan\n");
		}

		// M‰‰ritell‰‰n lokero-olio loppuun kun tiedet‰‰n sen pituus
		System.out.println("M‰‰ritell‰‰n lokerot summille.");
		lokerot = new Lokero(t);

		// Avataan summauspalvelut l‰hetettyihin portteihin
		System.out.println("\nAvataan summauspalvelut portteihin.");
		for(int x = 0; x < t; x++){
			Summaaja summaaja = new Summaaja(Integer.parseInt(tcpPort) + 1 + x, x);
			(new Thread(summaaja)).start();
			System.out.println("Summapalvelu " + x + " k‰ynnistetty");
		}

		// Otetaan vastaan WorkDistributorin l‰hett‰mi‰ kyselyit‰ ja vastataan niihin
		int kysely;
		while(true){
			
			// Asetetaan minuutin aikakatkaisu
			tcpSocket.setSoTimeout(60000);
			kysely = objectIn.readInt();
			System.out.println("\nKysely " + kysely + ": ");

			// Lopetuskysely
			if(kysely == 0){
				System.out.println("Lopetuskysely vastaanotettu. Lopetetaan");
				System.exit(0);
			}

			// Lukujen summan kysely
			else if(kysely == 1){
				System.out.println("Summakysely vastaanotettu. Palautetaan kaikkien lukujen summa");
				objectOut.writeInt(lokerot.getSummienSumma());
				objectOut.flush();
			}

			// Suurimman summan kysely
			else if(kysely == 2){
				System.out.println("Suuruuskysely vastaanotettu. Palautetaan suurin summa");
				objectOut.writeInt(lokerot.getSuurin());
				objectOut.flush();
			}

			// Vastaanotettujen lukujen lukum‰‰r‰n kysely
			else if(kysely == 3){
				System.out.println("M‰‰r‰kysely vastaanotettu. Palautetaan vastaanotettujen lukujen lukum‰‰r‰.");
				objectOut.writeInt(lokerot.getMaara());
				objectOut.flush();
			}

			// Virheellinen kysely
			else{
				System.out.println("Virheellinen kysely vastaanotettu. Kirjoitetaan virtaan -1");
				objectOut.writeInt(-1);
				objectOut.flush();
			}
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
