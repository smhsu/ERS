package ers.game;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;

/**
 * Card.java<br>
 * Container that holds a card's rank and suit, and supports
 * fetching of images.  Rank and suit are encoded as bytes to
 * allow convenient interfacing with network protocols.
 * 
 * This class has no accessors; just use the fields 'rank' and
 * 'suit' directly.
 * 
 * @author Silas Hsu // hsu.silas@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class Card
{
	// Ranks.  Make sure these stay in consecutive order!!!
	public static final byte
		JACK = 11,
		QUEEN = 12,
		KING = 13,
		ACE = 14,
		LOW_RANK = 2,
		HIGH_RANK = 14;
	
	// Suits.  Make sure these stay in consecutive order!!!
	public static final byte
		SPADES = 0,
		HEARTS = 1,
		DIAMONDS = 2,
		CLUBS = 3,
		LOW_SUIT = 0,
		HIGH_SUIT = 3;
	
	private static final String IMG_LOC = "ERS/PNG-cards/";
	private static final boolean USE_FACE_CARDS = true;
	
	public final byte rank;
	public final byte suit;
	
	/**
	 * Constructs a new card.  Ranks range from 2 to 14 (ace).
	 * Suits range from 0 (spades) to 3 (clubs).  Advice: use this
	 * class' public static fields!
	 * 
	 * @param rank - the card's rank
	 * @param suit - the card's suit
	 * @throws IllegalArgumentException when given an out-of-range rank or suit
	 */
	public Card(byte rank, byte suit)
	{
		if (rank < LOW_RANK || rank > HIGH_RANK)
			throw new IllegalArgumentException("Invalid rank");
		if (suit < LOW_SUIT || suit > HIGH_SUIT)
			throw new IllegalArgumentException("Invalid suit");
		
		this.rank = rank;
		this.suit = suit;
	}
	
	/**
	 * Constructs a new card.  Ranks range from 2 to 14 (ace).
	 * Suits range from 0 (spades) to 3 (clubs).  Advice: use this
	 * class' public static fields!
	 * 
	 * @param rank - the card's rank
	 * @param suit - the card's suit
	 * @throws IllegalArgumentException when given an out-of-range rank or suit
	 */
	public Card(int rank, int suit)
	{
		this( (byte)rank, (byte)suit );
	}

	/**
	 * Fetches an ImageIcon that represents this card.
	 * Returns null if the image file was not found.
	 * 
	 * @return an ImageIcon that represents this card, or null if it cannot be found.
	 */
	public ImageIcon getImage()
	{
		String fileName = IMG_LOC + rankToStr() + "_of_" + suitToStr();
		if (USE_FACE_CARDS && rank >= JACK && rank <= KING)
			fileName += "2.png";
		else
			fileName += ".png";
		
		if (new File(fileName).exists())
			return new ImageIcon(fileName);
		
		return null;
	}
	
	/**
	 * Fetches an ImageIcon that represents this card, and scales it by
	 * the input proportion.  Returns null if the image file was not found.
	 * 
	 * @param scaleAmt - the number by which to multiply the image dimensions
	 * @return a scaled ImageIcon that represents this card, or null if it cannot be found.
	 * 
	 * @throws IllegalArgumentException if the scale amount is negative, or if it is small
	 * enough to create a 0-width or 0-height image.
	 */
	public ImageIcon getImage(double scaleAmt)
	{
		ImageIcon icon = this.getImage();
		if (icon == null)
			return null;
		
		if (scaleAmt <= 0)
			throw new IllegalArgumentException("Cannot have scale amount <= 0");
		
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		Image scaled = icon.getImage().getScaledInstance((int)(width*scaleAmt), (int)(height*scaleAmt), Image.SCALE_DEFAULT);
		return new ImageIcon(scaled);
	}
	
	/**
	 * Fetches an ImageIcon that represents this card, and scales it to fit within a
	 * bounding box of desired dimensions.  Preserves the image's aspect ratio.
	 * Returns null if the image file was not found.
	 * 
	 * @param width - the maximum width of the returned ImageIcon (in pixels)
	 * @param height - the maximum height of the returned ImageIcon (in pixels)
	 * @return a scaled ImageIcon that represents this card, or null if it cannot be found.
	 * 
	 * @throws IllegalArgumentException if width or height are negative, or if they
	 * are small enough to create a 0-width or 0-height image.
	 */
	public ImageIcon getImage(int width, int height)
	{
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("Cannot have width or height <= 0");
		
		ImageIcon icon = this.getImage();
		if (icon == null)
			return null;
		
		double wRatio = (double)width/icon.getIconWidth();
		double hRatio = (double)height/icon.getIconHeight();
		Image scaled;
		if (wRatio < hRatio)
			scaled = icon.getImage().getScaledInstance(width, (int) (icon.getIconHeight()*wRatio), Image.SCALE_DEFAULT);
		else
			scaled = icon.getImage().getScaledInstance((int) (icon.getIconWidth()*hRatio), height, Image.SCALE_DEFAULT);
		
		return new ImageIcon(scaled);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rank;
		result = prime * result + suit;
		return result;
	}

	/**
	 * Indicates whether another Card and has the same rank and suit
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;

		return (this.rank == other.rank && this.suit == other.suit);
	}
	
	@Override
	public String toString() {
		return "Card " + rankToStr() + " of " + suitToStr();
	}
	
	/**
	 * @return this card's rank represented as a String
	 */
	private String rankToStr()
	{
		if (rank < JACK)
			return Byte.toString(rank);
		
		switch (rank)
		{
		case JACK:
			return "jack";
		case QUEEN:
			return "queen";
		case KING:
			return "king";
		case ACE:
			return "ace";
		default:
			assert(false);
			return "???";	
		}
	}
	
	/**
	 * @return this card's suit represented as a String
	 */
	private String suitToStr()
	{
		switch (suit)
		{
		case SPADES:
			return "spades";
		case HEARTS:
			return "hearts";
		case DIAMONDS:
			return "diamonds";
		case CLUBS:
			return "clubs";
		default:
			assert(false);
			return "???";
		}
	}

}
