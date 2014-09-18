package ers.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Server.java<br>
 * Main entry point.  Accepts new clients and creates PlayerHandlers for each of them.
 * 
 * @author Brittany Scheid // b.scheid@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class Server implements Runnable {

	private static int PORT = 10501;
	private ServerSocket ss;
	
	private static final boolean DEBUG = true;

	/**
	 * Creates a new server, i.e. binds a new ServerSocket.
	 */
	public Server() {
		
		try {
			ss= new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace(); // Default
		}
	}

	/**
	 * Continuously accepts clients that attempt to connect to the server.
	 * Creates PlayerHandlers for each client.
	 */
	public void run() {
		while (true) {
			try {
				Socket s= ss.accept();
				if (DEBUG) System.out.println(s.getInetAddress() + ":" + s.getPort() + " connected");
				PlayerHandler ph= new PlayerHandler(s); 
				ph.start();
			} catch (IOException e) {
				e.printStackTrace(); // Default
			}
			
			
		}
	}
	
	/**
	 * Starts the server.
	 * @param args - unused
	 */
	public static void main(String[] args) {
		Server m = new Server();
		m.run();
	}

}

