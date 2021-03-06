import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.Thread;

public class Hajoyhteys {
	static Socket tcpSocket;
	static ServerSocket tcpServerSocket;
	public static Lokero lokerot;

	public static void main(String[] args) throws Exception {
		// Asetetaan pääluokan prioriteetti korkeimmaksi ettei kutsujen väleissä tapahdu muutoksia
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		// Udp portti jonne yhteydenottoviesti lähetetään 
		int udpPort = 3126;

		// Tcp-portti johon palvelimen pyydetään ottamaan yhteyttä
		String tcpPort = "10000";

		// Tcp-portti bitteinä
		byte[] tcpPortBytes = tcpPort.getBytes();

		// Osoite johon otetaan yhteyttä
		InetAddress os = InetAddress.getByName("localhost");

		// maksimimäärä epäonnistuneita yhteydenottoja
		int connectionFails = 5;

		// Luodaan arraylist säikeisiin viittausta varten.
		ArrayList<Thread> threads = new ArrayList<Thread>();

		// Määritetään tcp serversocket
		tcpServerSocket = new ServerSocket(Integer.parseInt(tcpPort));
		while (connectionFails > 0) {
			try {
				// Lähetetään tcp portti udp pakettina
				sendTCPPort(tcpPortBytes, os, udpPort);
				System.out.println("Lähetetään TCP portti UDP paketissa...");

				// Asetetaan yhteyden aikakatkaisun aikarajaksi 5 sekuntia
				tcpServerSocket.setSoTimeout(5000);

				// Hyväksytään tcp yhteys ja sidotaan se tcp sockettiin
				tcpSocket = tcpServerSocket.accept();
				System.out.println("TCP-yhteys muodostettu.\n");
				break;
			} catch (SocketTimeoutException e) {
				System.out.println("Soketin aikakatkaisu.");
			}

			// Yhteydenoton epäonnistumislaskuria vähennetään
			connectionFails--;
			System.out.println("Connection failed.");
		}

		// Kun yhteydenotto epäonnistuu liian monta kertaa suljetaan ohjelma
		if (connectionFails == 0) {
			System.out.println("Yhteydenotto epäonnistui liian monta kertaa. Suljetaan prosessi viiden sekuntin kuluttua.");
			Thread.sleep(5000);
			System.exit(0);
		}

		// Avataan datavirrat
		System.out.println("Avataan pääluokan datavirrat.");
		InputStream in = tcpSocket.getInputStream();
		OutputStream out = tcpSocket.getOutputStream();
		ObjectInputStream objectIn = new ObjectInputStream(in);
		ObjectOutputStream objectOut = new ObjectOutputStream(out);
		System.out.println("Pääluokan datavirrat avattu.\n");

		// Asetetaan tcp soketin aikakatkaisun aikaraja
		tcpSocket.setSoTimeout(5000);

		// Yritetään lukea objektivirrasta summauspalvelimien määrä
		int t = 0;
		try{
			t = objectIn.readInt();
		}catch(SocketException e){
			objectOut.writeInt(-1);
			objectOut.flush();
			System.exit(0);
		}

		// Lähetetään summauspalvelimien portteja
		System.out.println("Lähetetään " + t + ":n summauspalvelimen portit.");
		for(int tt = 0; tt  < t; tt++){
			System.out.println("Kirjoitetaan oliovirtaan portti " + (Integer.parseInt(tcpPort) + 1 + tt));
			objectOut.writeInt(Integer.parseInt(tcpPort) + 1 + tt);
			objectOut.flush();
			System.out.println("Portti " + (Integer.parseInt(tcpPort) + 1 + tt) + " kirjoitettu oliovirtaan\n");
		}

		// Määritellään lokero-olio loppuun kun tiedetään sen pituus
		System.out.println("Määritellään lokerot summille.");
		lokerot = new Lokero(t);

		// Avataan summauspalvelut lähetettyihin portteihin
		System.out.println("\nAvataan summauspalvelut portteihin.");
		for(int x = 0; x < t; x++){
			Summaaja summaaja = new Summaaja(Integer.parseInt(tcpPort) + 1 + x, x);
			threads.add((new Thread(summaaja)));
			threads.get(threads.size()-1).start();
			System.out.println("Summapalvelu " + x + " käynnistetty");
		}

		// Otetaan vastaan WorkDistributorin lähettämiä kyselyitä ja vastataan niihin
		int kysely;
		while(true){

			// Asetetaan minuutin aikakatkaisu
			try{
				tcpSocket.setSoTimeout(60000);

				kysely = objectIn.readInt();

				System.out.println("\nKysely " + kysely + ": ");

				// Lopetuskysely
				if(kysely == 0){
					System.out.println("Lopetuskysely vastaanotettu. Lopetetaan summauspalvelijat ja sovellus");
					for(Thread thread : threads){
						thread.interrupt();
					}
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

				// Vastaanotettujen lukujen lukumäärän kysely
				else if(kysely == 3){
					System.out.println("Määräkysely vastaanotettu. Palautetaan vastaanotettujen lukujen lukumäärä.");
					objectOut.writeInt(lokerot.getMaara());
					objectOut.flush();
				}

				// Virheellinen kysely
				else{
					System.out.println("Virheellinen kysely vastaanotettu. Kirjoitetaan virtaan -1");
					objectOut.writeInt(-1);
					objectOut.flush();
				}
			}catch(SocketTimeoutException e){
				System.out.println("Soketin aikakatkaisu. Lopetetaan summauspalvelijat ja sovellus");
				System.exit(0);
			}
		}
	}

	// Lähettää tcp portin udp pakettina
	public static void sendTCPPort(byte[] tcpPortBytes, InetAddress os, int udpPort) {
		try {
			// Luodaan udp socketti ja paketti.
			DatagramSocket udpSocket = new DatagramSocket();
			DatagramPacket udpPacket = new DatagramPacket(tcpPortBytes, tcpPortBytes.length, os, udpPort);
			// Lähetetään paketti ja suljetaan socketti.
			udpSocket.send(udpPacket);
			udpSocket.close();
		} catch (Exception e) {

		}
	}

}
