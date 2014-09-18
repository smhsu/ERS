package ers.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import ers.game.ERSGame;


/**
 * PlayerHandler.java<br>
 * Reads messages sent by client and adds Runnables to the blocking queue
 * of the game.  Also contains methods for sending messages to the client.
 * 
 * @author Brittany Scheid // b.scheid@wustl.edu<br>
 * Silas Hsu // hsu.silas@wustl.edu<br>
 * Lawrance Fung // fungjl@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class PlayerHandler extends Thread {
	private Socket s;
	private ERSGame game;

	private DataInputStream dis;
	private DataOutputStream dos;
	private PlayerHandler ph; // Should always == this
	
	private boolean joined;
	
	private static final boolean DEBUG = true;

	/**
	 * Constructs a PlayerHandler out of a connected socket
	 * @param s - a Socket representing a connection to a client
	 */
	public PlayerHandler(Socket s) {
		ph=this;
		this.s = s;
	}

	/**
	 * Continuously reads commands from the client.
	 */
	public void run() {

		try
		{
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());
			game = ERSGame.instance();


			while (true)
			{
				//Terminates thread if interrupted
				if(Thread.interrupted()){
					s.close();
					return;
				}
				
				//Check for findmsgStart
				if (!Protocol.findMsgStart(dis))
					continue; 

				//get type of message to be sent
				byte type= dis.readByte(); 


				if (type == Protocol.C_JOIN_REQUEST)
				{
					String name= dis.readUTF(); 
					if (joined)
						continue;
					
					byte id = game.addPlayer(this, name);
					if (id < 0) // Could not add the player
					{
						sendCommand(id);
						s.close();
						return;
					}
					
					joined = true;
					
				}
				
				if (type == Protocol.C_PLAY_CARD) {
					Runnable r= new Runnable(){
						public void run(){
							game.takeTurn(ph);
						}
					};

					game.enqueue(r);
				}

				else if(type== Protocol.C_SLAP){
					final long currTime = System.currentTimeMillis();
					
					Runnable r= new Runnable(){
						public void run(){
							game.slap(ph, currTime);
						}
					};

					game.enqueue(r);
				}

			}
		}
		catch (IOException e)
		{
			if (DEBUG) System.out.println(s.getInetAddress() + ":" + s.getPort() + " dropped");
			Runnable r= new Runnable(){
				public void run(){
					game.dropPlayer(ph);
				}
			};
			
			game.enqueue(r);
		}

	}

	/**
	 * Disconnects the player from server.
	 */
	public void kick() {
		this.interrupt();
	}

	/**
	 * Sends a command to the client, complete with message start byte.
	 * @param command - a byte representing the command type
	 * 
	 * @throws IOException if an error occurs while sending the command
	 */
	public synchronized void sendCommand(byte command) throws IOException
	{
		dos.writeByte(Protocol.MSG_START);
		dos.writeByte(command);
	}

	/**
	 * Sends a byte to the client <b>without</b> the message start byte.
	 * @param b - byte to send
	 * @throws IOException if an error occurs while sending the byte
	 */
	public synchronized void sendByte(byte b) throws IOException
	{
		dos.writeByte(b);	
	}

	/**
	 * Sends a UTF-8 string to the client <b>without</b> the message start byte.
	 * @param str - the string to send
	 * @throws IOException if an error occurs while sending the string
	 */
	public synchronized void sendString(String str) throws IOException
	{
		dos.writeUTF(str);
	}

}
