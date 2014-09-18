package ers.server;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Protocol.java<br>
 * Contains constants used in ERS's network protocol.
 * Also contains utility function findMsgStart().
 * 
 * @author Silas Hsu // hsu.silas@wustl.edu<br>
 * CSE 132 Lab 5
 */
public abstract class Protocol {
	
	public static final byte MSG_START = 29;
	
	// Client commands
	public static final byte
		C_JOIN_REQUEST = 0,
		C_PLAY_CARD = 10,
		C_SLAP = 11;
	
	// Server commands
	public static final byte
		S_PLAYER_JOIN = 1,
		S_PLAYER_DROP = 3,
		S_GAME_START = 10,
		S_CARD_ADDED = 20,
		S_SLAP = 21,
		S_TURN = 22,
		S_PILE_CLAIM = 23,
		S_GAME_END = 60;
	
	// Bytes to follow S_SLAP
	public static final byte
		SLAP_BAD = -1,
		SLAP_GOOD = 0,
		SLAP_SLOW = 1;
		
	// Server errors
	public static final byte
		S_NAME_TAKEN = -3,
		S_GAME_FULL = -2,
		S_ERR = -1;
	
	/**
	 * Blocks until the input stream has bytes to read.  Then, consumes bytes
	 * from the input stream until the magic number indicating the start of a
	 * message is found.  Returns true if the magic number was found, and false
	 * if reached the end of the stream before finding it.
	 * 
	 * @param in - the input stream to read from
	 * @return true if the start of a message is found, false otherwise
	 * @throws IOException when encountering an IOException while reading
	 */
	public static boolean findMsgStart(DataInputStream in) throws IOException
	{
		do
		{
			byte num = in.readByte();
			if (num == MSG_START) return true;
			
		} while (in.available() >= 1);
		return false;
	}

}
