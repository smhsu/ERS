package ers.game;

/**
 * Player.java<br>
 * Representation of an ERS player.  Players have a name, deck, and id.
 * 
 * @author Tyler Darwin // tdarwin@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class Player {
	
	private String name;
	Deck deck;
	public final byte id;
	
	boolean inGame;
	
	/**
	 * @param name - name of the player
	 * @param id - id of the player
	 */
	public Player(String name, byte id) {
		this.name = name;
		deck = new Deck();
		this.id = id;
		inGame = true;
	}
	
	/**
	 * @return true if this Player still has cards to play
	 */
	public boolean hasCards()
	{
		return deck.size() != 0;
	}
	
	/**
	 * @return the name of the Player
	 */
	public String getName() { return name; }
	
	/**
	 * @return the id of this player
	 */
	public int getID() { return id; }

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		return (this.id == other.id);
	}

	/**
	 * @return the name of the Player
	 */
	@Override
	public String toString() {
		return getName();
	}


}
