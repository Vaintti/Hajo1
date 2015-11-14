import java.net.*;
import java.util.*;
import java.io.*;

public class Hajoyhteys {
	static Socket tcpSocket;
	static ServerSocket tcpServerSocket;

	public static void main(String[] args) throws Exception {

		int udpPort = 3126;

		String tcpPort = "10000";
		byte[] tcpPortBytes = tcpPort.getBytes();

		InetAddress os = InetAddress.getByName("localhost");

		int connectionFails = 5;

		tcpServerSocket = new ServerSocket(Integer.parseInt(tcpPort));
		while (connectionFails > 0) {
			try {
				sendTCPPort(tcpPortBytes, os, udpPort);
				System.out.println("Sending TCP port in UDP packet...");
				tcpServerSocket.setSoTimeout(5000);
				tcpSocket = tcpServerSocket.accept();
				System.out.println("TCP-connection set up.");
				break;
			} catch (SocketTimeoutException e) {

			}
			connectionFails--;
			System.out.println("Connection failed.");
		}
		if (connectionFails == 0) {
			System.out.println("Connection failed too many times. Terminating process.");
			Thread.sleep(5000);
			System.exit(0);
		}
		System.out.println("Starting communication with client.");
		InputStream in = tcpSocket.getInputStream();
		OutputStream out = tcpSocket.getOutputStream();
		ObjectInputStream objectIn = new ObjectInputStream(in);
		ObjectOutputStream objectOut = new ObjectOutputStream(out);
		tcpSocket.setSoTimeout(5000);
		int t = objectIn.readInt();
		System.out.println(t);
		for(int tt = 0; tt  < t; t++){
			objectOut.writeInt(Integer.parseInt(tcpPort) + 1 + tt);
		}

	}

	public static void sendTCPPort(byte[] tcpPortBytes, InetAddress os, int udpPort) {
		try {
			DatagramSocket udpSocket = new DatagramSocket();
			DatagramPacket udpPacket = new DatagramPacket(tcpPortBytes, tcpPortBytes.length, os, udpPort);
			udpSocket.send(udpPacket);
			udpSocket.close();
		} catch (Exception e) {

		}
	}

}
