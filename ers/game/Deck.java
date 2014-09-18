package ers.game;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Deck.java<br>
 * Represents ordered piles of cards by using a list.
 * 
 * @author Silas Hsu // hsu.silas@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class Deck {

	private LinkedList<Card> cards; // The "top" is the start of the list
	
	/**
	 * Creates an empty deck.
	 */
	public Deck()
	{
		cards = new LinkedList<Card>();
	}
	
	/**
	 * Adds the standard 52 cards to this deck.
	 */
	public void addStandard52()
	{
		for (byte suit = Card.LOW_SUIT; suit <= Card.HIGH_SUIT; suit++)
		{
			for (byte rank = Card.LOW_RANK; rank <= Card.HIGH_RANK; rank++)
			{
				cards.add(new Card(rank, suit));
			}
		}
	}
	
	/**
	 * Shuffles this deck - puts the cards in a random order.
	 */
	public void shuffle() { Collections.shuffle(cards); }
	
	/**
	 * @return the number of cards in this deck.
	 */
	public int size() { return cards.size(); }
	
	/**
	 * Adds a card to the top of this deck.
	 * @param c - the card to add
	 */
	public void addTop(Card c) { cards.addFirst(c); }

	/**
	 * Adds a card to the bottom of this deck.
	 * @param c - the card to add
	 */
	public void addBot(Card c) { cards.addLast(c); }
	
	/**
	 * Adds the contents of another deck to the bottom of this deck.
	 * The cards will be added in reverse order; that is bottom card to top.
	 * Afterwards, all cards in the argument deck will be removed.
	 * 
	 * @param d - the deck from which to add cards and then clear
	 */
	public void addDeck(Deck d)
	{
		Iterator<Card> iter = d.cards.descendingIterator();
		while (iter.hasNext())
		{
			cards.addLast(iter.next());
		}
		d.removeAll();
	}
	
	/**
	 * @return the card on the top of this deck, or null if the deck is empty
	 */
	public Card peekTop()
	{
		if (cards.size() == 0)
			return null;
		return cards.getFirst();
	}
	
	/**
	 * @return the card on the bottom of this deck, or null if the deck is empty
	 */
	public Card peekBot()
	{
		if (cards.size() == 0)
			return null;
		return cards.getLast();
	}
	
	/**
	 * Removes and returns the card on the top of this deck.
	 * @return the card on the top of this deck, or null if the deck is empty
	 */
	public Card removeTop()
	{
		if (cards.size() == 0)
			return null;
		return cards.removeFirst();
	}
	
	/**
	 * Removes and returns the card on the bottom of this deck.
	 * @return the card on the bottom of this deck, or null if the deck is empty
	 */
	public Card removeBot()
	{
		if (cards.size() == 0)
			return null;
		return cards.removeLast();
	}
	
	/**
	 * Returns if there is a double or sandwich on the top of this deck.  A double is two
	 * cards of the same rank in succession.  A sandwich is two cards of the same rank
	 * with a card of any rank sandwiched between.
	 * 
	 * @return true if there is a double or sandwich on the top of this deck
	 */
	public boolean slapOk()
	{
		if ( (cards.size() >= 2) && (cards.get(0).rank == cards.get(1).rank) ) // Double
			return true;
		
		if ( (cards.size() >= 3) && (cards.get(0).rank == cards.get(2).rank) ) // Sandwich
			return true;
		
		return false;
	}
	
	/**
	 * Removes all cards from this deck.
	 */
	public void removeAll() { cards.clear(); }
}
