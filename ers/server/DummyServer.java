package ers.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ers.game.Card;

/**
 * DummyServer.java<br>
 * Sends commands to a single client in predefined order for debugging purposes.
 * Not up to date.
 * 
 * @author Tyler Darwin // tdarwin@wustl.edu
 * CSE 132 Lab 5
 */
public class DummyServer implements Runnable {
	
	/**
	 * Does nothing.
	 */
	public DummyServer()
	{
	}
	
	/**
	 * Starts the server.  Once a client has connected, sends a predefined series of commands.
	 */
	public void run()
	{
		try
		{
			ServerSocket ss = new ServerSocket(21342);
			Socket clientConn = ss.accept();
			
			DataInputStream dis = new DataInputStream(clientConn.getInputStream());
			DataOutputStream dos = new DataOutputStream(clientConn.getOutputStream());	
			
			// HANDSHAKE
			while(true){
			if (Protocol.findMsgStart(dis)){
				System.out.println("Reading!");
				Byte b = dis.readByte(); // Command, presumable new player
				System.out.println(b);
				String s= dis.readUTF(); // Name of the new player
				System.out.println(s);
				dos.writeByte(Protocol.MSG_START);
				dos.writeByte(1);
				if(s.equals("bo")){
					break;
				}
			}
			}
			System.out.println("finished loop");
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_GAME_START);
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_CARD_ADDED);
//			dos.writeByte(Card.JACK);
//			dos.writeByte(Card.CLUBS);
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_CARD_ADDED);
//			dos.writeByte(Card.JACK);
//			dos.writeByte(Card.DIAMONDS);
//			Thread.sleep(4000);
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_SLAP);
//			dos.writeByte(1);
//			dos.writeByte(0);
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_CARD_ADDED);
//			dos.writeByte(Card.JACK);
//			dos.writeByte(Card.DIAMONDS);
//			dos.writeByte(Protocol.S_TURN);
//			dos.writeByte(1);
//			Thread.sleep(4000);
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_CARD_ADDED);
//			dos.writeByte(Card.JACK);
//			dos.writeByte(Card.SPADES);
//			Thread.sleep(4000);
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_SLAP);
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_SLAP);
//			dos.writeByte(1);
//			dos.writeByte(-1);
//			Thread.sleep(4000);
			dos.writeByte(Protocol.MSG_START);
			dos.writeByte(Protocol.S_SLAP);
			dos.writeByte(1);
			dos.writeByte(-1);
//			dos.writeByte(Protocol.S_PILE_CLAIM);
//			dos.writeByte(Protocol.MSG_START);
//			dos.writeByte(Protocol.S_PLAYER_JOIN);
//			dos.writeByte(2);
//			dos.writeUTF("Jimmy");
//			Thread.sleep(4000);
//			dos.writeByte(Protocol.S_GAME_END);
			clientConn.close();
			ss.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace(); // Default
		} 
	}
	
	/**
	 * Main entry point
	 * @param args - unused
	 */
	public static void main(String[] args)
	{
		DummyServer s = new DummyServer();
		s.run();
	}

}
