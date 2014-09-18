package ers.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import ers.game.Card;
import ers.game.Player;
import ers.server.Protocol;

/**
 * Client.java<br>
 * Reads and sends commands to the server.  Interacts with the view and updates it.
 * 
 * @author Silas Hsu // hsu.silas@wustl.edu<br>
 * Tyler Darwin // tdarwin@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class Client extends Thread {

	private static final boolean DEBUG = true;

	// Complete the Client class
	private int port;
	private String hostName;
	private ClientView view;
	DataOutputStream dos;
	DataInputStream dis;
	private byte currTurnId;
	private byte thisId;
	private int cardsInPile;

	public Client(int portNum, String host, ClientView view) {
		port=portNum;
		hostName=host;
		this.view = view;
		thisId = 0;
		cardsInPile = 0;

	}

	public void sendPlayCard()
	{
		try 
		{
			dos.writeByte(Protocol.MSG_START);
			dos.writeByte(Protocol.C_PLAY_CARD);
			if (DEBUG) System.out.println("DEBUG: sending play card message");
		}
		catch (IOException e) {} // Nothing.  For now.
	}
	
	public void sendSlap()
	{
		try 
		{
			dos.writeByte(Protocol.MSG_START);
			dos.writeByte(Protocol.C_SLAP);
			if (DEBUG) System.out.println("DEBUG: sending slap message");
		}
		catch (IOException e) {} // Nothing.  For now.
	}

	public void run()
	{
		Socket s;
		try{
			String name = view.askForName();
			if (name == null)
			{
				view.enableReconnectButton();
				return;
			}
			
			s=new Socket(hostName, port);
			dos= new DataOutputStream(s.getOutputStream());
			dis= new DataInputStream(s.getInputStream());
			
			dos.writeByte(Protocol.MSG_START);
			dos.writeByte(Protocol.C_JOIN_REQUEST);
			dos.writeUTF(name);			
			
			byte command = -1;
			do
			{
				if (!Protocol.findMsgStart(dis))
					continue;
					
				command = dis.readByte();
				if (command == Protocol.S_GAME_FULL)
				{
					view.showErrDialog("Could not join game: game full or has already started.");
					s.close();
					view.enableReconnectButton();
					return;
				}
				else if (command == Protocol.S_NAME_TAKEN)
				{
					view.showErrDialog("Could not join game: that player name has already been taken.");
					s.close();
					view.enableReconnectButton();
					return;
				}
				else if (command == Protocol.S_PLAYER_JOIN)
				{
					byte p_id = dis.readByte();
					String p_name = dis.readUTF();
					if (p_name.equals(name))
					{
						thisId = p_id;
						if (DEBUG) System.out.println("DEBUG: Successfully joined game");
					}
					view.addPlayer(new Player(p_name, p_id));
				}
				
			} while (command != Protocol.S_GAME_START);
			
			view.setMessageGUI("The game has started!");
			if (DEBUG) System.out.println("DEBUG: Server signals game start!");
			
			// Received game start command!  Initialize cards
			byte players = dis.readByte();
			for (byte i = 0; i < players; i++)
			{
				byte p_id = dis.readByte();
				byte cards = dis.readByte();
				view.setPlayerCards(p_id, cards);
			}
			
			while(true){
				if(Protocol.findMsgStart(dis)){
					
					int type = dis.readByte();
					
					if(type==Protocol.S_CARD_ADDED){
						if (DEBUG) System.out.println("DEBUG: Read a card add command");
						byte suit= dis.readByte(); 
						byte rank= dis.readByte(); 
						view.playCardGUI(new Card(suit,rank));
						view.changePlayerCards(currTurnId,-1);
						
						cardsInPile++;
						view.setPileCounter(cardsInPile);
					}
					else if(type==Protocol.S_SLAP){
						if (DEBUG) System.out.println("DEBUG: Read a slap");
						int p_id= dis.readByte();
						int success= dis.readByte();

						if(success== Protocol.SLAP_BAD)
							view.setMessageGUI(view.getPlayerName(p_id) + " made a bad slap!");
						if(success== Protocol.SLAP_GOOD){
							view.setMessageGUI(view.getPlayerName(p_id) + " made a good slap!");
						}
						if(success==Protocol.SLAP_SLOW)
							view.setMessageGUI(view.getPlayerName(p_id) + " slapped too late!");
					}

					else if(type==Protocol.S_TURN){
						if (DEBUG) System.out.println("DEBUG: Read a turn signal");
						currTurnId = dis.readByte();
						view.indicatePlayerTurn(currTurnId);
						if (thisId == currTurnId)
							view.setEnabledPlayCardButton(true);
						else
							view.setEnabledPlayCardButton(false);
					}
					
					else if (type == Protocol.S_PILE_CLAIM){
						if (DEBUG) System.out.println("DEBUG: Player claimed pile");
						byte p_id=dis.readByte();
						byte count=dis.readByte();
						view.changePlayerCards(p_id, count);
						view.clearCards();
						view.setPileCounter(0);
						cardsInPile = 0;
					}
					
					else if (type == Protocol.S_PLAYER_DROP)
					{
						if (DEBUG) System.out.println("DEBUG: Player dropped");
						byte p_id=dis.readByte();
						cardsInPile += dis.readByte();
						view.removePlayer(p_id);
						view.setPileCounter(cardsInPile);
					}
					
					else if(type==Protocol.S_GAME_END){
						if (DEBUG) System.out.println("DEBUG: Game ended");
						byte p_id = dis.readByte();
						view.setMessageGUI(view.getPlayerName(p_id) + " won!");
						
						s.close();
						return;
					}
				}
			} // End while(true)
		}// End try
		catch(IOException e)
		{
			view.showErrDialog("Connection to server failed.");
			view.enableReconnectButton();
		}
	}

}