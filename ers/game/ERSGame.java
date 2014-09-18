package ers.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import ers.server.PlayerHandler;
import ers.server.Protocol;
import ers.utils.BlockingQueue;
import ers.game.Player;

/**
 * ERSGame.java<br>
 * Implementation of Egyptian Rat-Screw model specialized for
 * server-side use.  Uses singleton paradigm.
 * 
 * @author Silas Hsu // hsu.silas@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class ERSGame {
	
	private static ERSGame theGame = null;
	
	public static final int
		MIN_PLAYERS = 2,
		MAX_PLAYERS = 3;
	private static final int SLAP_GRACE = 1000; // Milliseconds allowed for slow slaps
	private static final int PILE_CLAIM_DELAY = 1000; // Milliseconds before sending a pile claim command
	private Thread awardPileThread;
	
	static byte id = 0; // Next player id to give out
	
	private BlockingQueue<Runnable> bq;
	
	private HashMap<PlayerHandler, Player> clients;
	private ArrayList<Player> turnList; // List whose order should stay constant
	private int turnListPos;
	private Player turn; // Whoever's turn it is
	private int playersWithCards; // Number of players that still have cards
	
	private Deck pile; // Community pile
	private boolean slapOk;
	private long lastGoodSlapTime; // The last system time at which slapping was ok
	private Player challenger; // A player that has played a face card or ace
	private int challengeCnt; // How many chances left to play a face card or ace because of a challenge

	private boolean started; // If not started, server ignores all play card and slap commands
	private boolean paused; // If paused, server ignores turn commands
	
	/**
	 * Makes a new game model with zero players
	 */
	private ERSGame()
	{
		pile = new Deck();
		pile.addStandard52();
		pile.shuffle();
		
		clients = new HashMap<PlayerHandler, Player>();
		turnList = new ArrayList<Player>();
		turnListPos = -1;
		turn = null;
		playersWithCards = 0;
		
		pile = new Deck();
		pile.addStandard52();
		pile.shuffle();
		
		slapOk = false;
		lastGoodSlapTime = 0;
		challenger = null;
		challengeCnt = 0;
		
		started = false;
		paused = false;
		
		bq = new BlockingQueue<Runnable>(10);
		new Thread()
		{
			public void run()
			{
				while (true)
				{
					bq.dequeue().run();
				}
			}
		}.start();
	}
	
	/**
	 * Accessor for the single, shared instance of ERSGame.
	 * @return the one ERS game in the program.
	 */
	public static ERSGame instance()
	{
		if (theGame == null)
			theGame = new ERSGame();
		
		return theGame;
	}
	
	/**
	 * Enqueues a runnable to be executed as soon as possible.  A separate thread
	 * will execute it.
	 * 
	 * @param r - a Runnable containing code to execute.
	 */
	public void enqueue(Runnable r) { bq.enqueue(r); }
	
	/**
	 * Adds a player to the model, and returns the player's assigned ID number.
	 * On an error, returns a negative number corresponding to an error in the
	 * ERS protocol.
	 * 
	 * If a player can be added, sends a player list to the client in the form
	 * of PLAYER_JOIN messages.
	 * 
	 * @param ph - the PlayerHandler from which the request originated
	 * @param name - the name of the new player
	 * @return the added player's ID number, or a negative number on an error.
	 */
	public byte addPlayer(PlayerHandler ph, String name)
	{
		if (isFull() || started)
			return Protocol.S_GAME_FULL;
		
		for (Player p : turnList)
		{
			if (p.getName().equals(name))
				return Protocol.S_NAME_TAKEN;
		}
		
		Player newPl = new Player(name, id);
		try
		{
			for (Player p : turnList)
			{
				ph.sendCommand(Protocol.S_PLAYER_JOIN);
				ph.sendByte(p.id);
				ph.sendString(p.getName());
			}
		} catch (IOException e) { return -2; }
		
		clients.put(ph, newPl);
		turnList.add(newPl);
		indicateNewPlayer(name, id);
		return id++; // Returns the current id, then increments it
	}
	
	/**
	 * Drops a player.  His cards will be added to the community pile.
	 * If he is the challenger, the current challenged player still
	 * has an obligation to put down cards, but the cards will be awarded
	 * to nobody if he fails to get a face card or ace.
	 * 
	 * @param ph - the client from which the request originated.
	 */
	public void dropPlayer(PlayerHandler ph)
	{
		Player dropped = clients.get(ph);
		dropped.inGame = false;
		indicateDrop(dropped);
		decrPlayerCount();
		
		if (turn == dropped)
		{
			turn = nextPlayer();
			indicateTurn(turn.id);
		}
		if (challenger == dropped)
		{
			challenger = null;
		}
		
		pile.addDeck(dropped.deck);
	}
	
	/**
	 * Indicates to all clients that a player has dropped.
	 * @param dropper - the player that is dropping
	 */
	private void indicateDrop(Player dropper)
	{
		for (PlayerHandler ph : clients.keySet())
		{
			try
			{
				ph.sendCommand(Protocol.S_PLAYER_DROP);
				ph.sendByte(dropper.id);
				ph.sendByte((byte) dropper.deck.size());
			} catch (IOException e) {} // Do nothing; continue	
		}
	}
	
	/**
	 * Indicate to all clients that a new player has joined.  If the number
	 * of players in game has reached maximum, starts the game.
	 * 
	 * @param name - the name of the new player
	 * @param id - the assigned id of the new player
	 */
	private void indicateNewPlayer(String name, byte id)
	{
		for (PlayerHandler ph : clients.keySet())
		{
			try
			{
				ph.sendCommand(Protocol.S_PLAYER_JOIN);
				ph.sendByte(id);
				ph.sendString(name);
			} catch (IOException e) {} // Do nothing; continue	
		}
		
		if (turnList.size() == MAX_PLAYERS)
		{
			startGame();
		}
	}
	
	/**
	 * Gets whether or not the game can accept more players.
	 * @return true if the game is full
	 */
	public boolean isFull() { return clients.size() >= MAX_PLAYERS; }
	
	/**
	 * Deals cards to everybody in the game as equally as possible,
	 * initializes the turn list, and starts the game.
	 * @throws IllegalStateException if the game already started or if
	 * there are an inappropriate number of players
	 */
	private void startGame()
	{
		if (started)
			throw new IllegalStateException("Game already started");
		if (clients.size() < MIN_PLAYERS)
			throw new IllegalStateException("Cannot start game with less than " + MIN_PLAYERS + " players");
		
		Iterator<Player> playerIter = turnList.iterator();
		Player p;
		while (pile.size() > 0) // Deal cards to players from the pile
		{
			if (!playerIter.hasNext())
				playerIter = turnList.iterator(); // Reset to beginning
			
			p = playerIter.next(); // Warning: breaks if there are 0 players.  Make sure MIN_PLAYERS > 0
			p.deck.addTop((pile.removeTop()));
		}
		
		playersWithCards = turnList.size();
		turnListPos = 0;
		turn = turnList.get(0);
		started = true;
		indicateStart();
		indicateTurn(turn.id);
	}
	
	/**
	 * Indicate to all clients that the game has started.
	 */
	private void indicateStart()
	{
		for (PlayerHandler ph : clients.keySet())
		{
			try
			{
				ph.sendCommand(Protocol.S_GAME_START);
				ph.sendByte((byte) clients.size());
				for (Player p : turnList)
				{
					ph.sendByte(p.id);
					ph.sendByte((byte) p.deck.size());
				}
			} catch (IOException e) {} // Do nothing; continue	
		}
	}
	
	/**
	 * Indicates to all clients who's turn it is.
	 * @param id - the id of the player who's turn it is
	 */
	private void indicateTurn(byte id)
	{
		for (PlayerHandler ph : clients.keySet())
		{
			try
			{
				ph.sendCommand(Protocol.S_TURN);
				ph.sendByte(id);
			} catch (IOException e) {} // Do nothing; continue	
		}
	}

	/**
	 * Tells the ERSGame that the client represented by the input PlayerHandler wishes
	 * to take a turn.  Does nothing if it is not the client's turn, or if the game has
	 * not started or has ended.
	 * 
	 * If the right client is taking a turn, adds a card from the associated Player to
	 * the community pile, and appropriately determines the next turn.  All clients will
	 * be notified of these events.
	 * 
	 * @param ph - a PlayerHandler representing a client who wishes to take a turn
	 */
	public void takeTurn(PlayerHandler ph)
	{
		if (!started || paused)
			return;
		
		Player who = clients.get(ph);
		if (who != turn) // Check if ph represents the right client
			return;
		
		if (slapOk)
			lastGoodSlapTime = System.currentTimeMillis();
		
		Card c = turn.deck.removeTop();
		indicateCard(c);	
		pile.addTop(c);
		slapOk = pile.slapOk();
		
		int newChal = getChallenge(c);
		if (newChal > 0) // A new challenger appears!
		{		
			if ((challenger != null) && (!challenger.hasCards()))
				decrPlayerCount();
			
			this.challengeCnt = newChal;
			challenger = turn;
			turn = nextPlayer();
		}
		else if (this.challengeCnt > 0) // Old challenge still active
		{		
			if (!turn.hasCards())
			{
				decrPlayerCount();
				turn = nextPlayer(); // Move the challenge to next player
			}
			turn = decrChallenge(); // See javadoc for details
		}
		else
		{
			if (!turn.hasCards())
				decrPlayerCount();
			
			turn = nextPlayer();
		}
		
		if (started) // decrPlayerCount() may end the game, so check if that happened
			indicateTurn(turn.id);
	}
	
	/**
	 * Indicates to all clients that a new card has been played
	 * @param c - the card that was player
	 */
	private void indicateCard(Card c)
	{
		for (PlayerHandler ph : clients.keySet())
		{
			try
			{
				ph.sendCommand(Protocol.S_CARD_ADDED);
				ph.sendByte(c.rank);
				ph.sendByte(c.suit);
			} catch (IOException e) {} // Do nothing; continue	
		}
	}

	/**
	 * Gets the next player that can play cards.  <b>Warning</b>: infinite loop
	 * if no players have cards.
	 * 
	 * @return the next player that can play cards
	 * @throws IndexOutOfBoundsException if there are 0 players
	 */
	private Player nextPlayer()
	{
		Player p;
		do
		{
			turnListPos++;
			if (turnListPos >= turnList.size())
				turnListPos = 0;
			p = turnList.get(turnListPos); // IndexOutOfBoundsException if there are 0 players
			
		} while (!p.hasCards() || !p.inGame); // Infinite loop if no one has cards
		
		return p;
	}
	
	/**
	 * If a face card or ace has appeared, gets the number of chances to play another
	 * face card or ace: four chances after an Ace, three after a King, two after a
	 * Queen, and one after a Jack.  Otherwise, returns 0.
	 * 
	 * @param c - a card that may initiate a challenge, as indicated above
	 * @return the number of chances to play another face card or ace, or 0 if the
	 * input card was not a face card or ace.
	 */
	private int getChallenge(Card c)
	{
		switch (c.rank)
		{
		case Card.JACK:
			return 1;
		case Card.QUEEN:
			return 2;
		case Card.KING:
			return 3;
		case Card.ACE:
			return 4;
		default:
			return 0;
		}
	}
	
	/**
	 * Marks one card of a challenge as played.  If that was the last one, awards
	 * the pile to the challenger and returns him.  Otherwise, returns the current
	 * player's turn
	 * @return the next player's turn
	 */
	private Player decrChallenge()
	{
		if (challengeCnt <= 0)
			throw new IllegalStateException("Cannot decrement challenge below 0");
		
		challengeCnt--;
		if (challengeCnt == 0) // challenger won the challenge!
		{
			if (challenger == null) // If the challenger drops, he may be null
				return nextPlayer();
			
			awardPile(challenger);
			turnListPos = turnList.indexOf(challenger);
			return challenger;
			
		}
		return turn;
	}
	
	/**
	 * After a specified delay, awards the specified player the pile
	 * and informs all clients of that fact.  Another thread performs
	 * this action, so the thread that executes this method will not
	 * stall.
	 * 
	 * @param p - the player that won the pile
	 */
	private void awardPile(final Player p)
	{
		if (pile.size() == 0)
			return;
		
		paused = true;
		
		awardPileThread = new Thread() {
			public void run()
			{	
				try
				{
					Thread.sleep(PILE_CLAIM_DELAY);
				} catch (InterruptedException interr) { return; }

				synchronized(theGame) // Start awarding the pile
				{
					if (pile.size() == 0) { // Some other thread may have already sent the pile claim
						paused = false;
						return;
					}
					
					byte pileSize = (byte) pile.size();
					p.deck.addDeck(pile);
					for (PlayerHandler ph : clients.keySet())
					{
						try
						{
							ph.sendCommand(Protocol.S_PILE_CLAIM);
							ph.sendByte(p.id);
							ph.sendByte(pileSize);
						} catch (IOException e) {} // Do nothing; continue	
					}

					paused = false;
				} // End synchronized
			} // End run()
		};
		
		awardPileThread.start();
			
	}
	
	/**
	 * Tells the ERSGame that the client represented by the input PlayerHandler wishes
	 * to slap.  Does nothing if the game has not started or has ended.
	 * 
	 * @param ph - a PlayerHandler representing the client that wishes to slap
	 * @param time - the approximate system time at which the server received the slap
	 * command
	 */
	public void slap(PlayerHandler ph, long time)
	{
		if (!started)
			return;
		
		Player slapper = clients.get(ph);
		if (slapOk)
		{
			if (!slapper.hasCards())
			{
				playersWithCards++;
			}
			
			indicateSlap(slapper, Protocol.SLAP_GOOD);
			turn = slapper;
			turnListPos = turnList.indexOf(turn);
			indicateTurn(slapper.id);
			
			slapOk = false;
			lastGoodSlapTime = time;
			challengeCnt = 0;
			
			if (awardPileThread != null)
				awardPileThread.interrupt(); // Stop if anybody else, namely decrChallenge(), has started the process
			awardPile(slapper); // Does nothing if awardPileThread has already started to award the pile
			
			return;
		}
		
		// Slap not ok, but soon enough
		if ( time <= (lastGoodSlapTime + SLAP_GRACE) )
		{
			indicateSlap(slapper, Protocol.SLAP_SLOW);
		}
		else // A bad slap: remove cards
		{
			indicateSlap(slapper, Protocol.SLAP_BAD);
			/*
			if (slapper.deck.size() >= 2)
			{
				pile.addBot( slapper.deck.removeBot() );
				pile.addBot( slapper.deck.removeBot() );
				if (!slapper.hasCards())
				{
					decrPlayerCount();
					if (slapper == turn)
					{
						turn = nextPlayer();
						indicateTurn(turn.id);
					}
				}
			}
			else if (slapper.deck.size() == 1)
			{
				pile.addBot( slapper.deck.removeBot() );
				decrPlayerCount();
			}
			*/
		}
		
	}
	
	/**
	 * Indicates to all clients that a player has slapped the pile
	 * 
	 * @param p - the player that slapped
	 * @param success - a byte indicating the successfulness of the slap
	 */
	private void indicateSlap(Player p, byte success)
	{
		for (PlayerHandler ph : clients.keySet())
		{
			try
			{
				ph.sendCommand(Protocol.S_SLAP);
				ph.sendByte(p.id);
				ph.sendByte(success);
			} catch (IOException e) {} // Do nothing; continue	
		}
	}
	
	/**
	 * Decrements the number of players in the game.  If only one player remains after
	 * that, ends the game.
	 */
	private void decrPlayerCount()
	{
		playersWithCards--;
		if (playersWithCards <= 1)
		{
			turn = nextPlayer();
			indicateEnd(turn);
			started = false;
			theGame = null; // Destroys this game.  Hopefully.
		}
	}
	
	/**
	 * Notifies all clients there is a winner and the game has ended.
	 * @param winner - the player that won the game
	 */
	private void indicateEnd(Player winner)
	{
		for (PlayerHandler ph : clients.keySet())
		{
			try
			{
				ph.sendCommand(Protocol.S_GAME_END);
				ph.sendByte(winner.id);
			} catch (IOException e) {} // Do nothing; continue	
		}
	}
	
}
