package ers.client;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import ers.game.Card;
import ers.game.Player;
import javax.swing.SwingConstants;
import java.awt.Font;

/**
 * ClientView.java<br>
 * Main entry point - runs the GUI and starts the Client object which 
 * connects to the server.  The GUI informs players of cards played,
 * cards of each player, who's turn it is, etc.  It also contains buttons
 * for sending messages to the server.
 * 
 * @author Brittany Scheid // b.scheid@wustl.edu<br>
 * Silas Hsu // hsu.silas@wustl.edu<br>
 * CSE 132 Lab 5
 */
public class ClientView extends JFrame {

	private static final long serialVersionUID = 1L; // Default
	
	private JPanel contentPane;
	private JLayeredPane layeredPane; 
	private JLabel[] cardLabels= new JLabel[NUM_CARDS-1];
	private JButton topCard; 
	private JLabel lblMessageTextField;
	private JButton btnReconn;
	private JButton deckButton;
	private DefaultTableModel dtm= new DefaultTableModel();
	private HashMap<Integer, Player> idsToPlayers;
	private JTable table;
	static final int NUM_CARDS=5;
	static final double CARD_SCALE = 0.3;
	int LAYER_PANE_WIDTH= 340;
	
	private Client serverLink;
	private static int PORT = 10501;
	private static String ADDR = "localhost";
	private final ClientView cv = this;
	private JLabel pileCounter;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientView frame = new ClientView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 645, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		dtm.addColumn("Players", new Vector<String>());
		dtm.addColumn("Cards", new Vector<String>());
		table= new JTable(dtm);
	
		//Add table to scroll pane
		JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(10, 11, 150, 400);
		contentPane.add(scrollPane);

		//Create Layered pane
		layeredPane = new JLayeredPane();
		layeredPane.setBorder(BorderFactory.createTitledBorder("Played Cards"));
		layeredPane.setBounds(175, 78, LAYER_PANE_WIDTH, 280);

		//add card labels:
		for (int i = 0; i < NUM_CARDS; i++) {
			if(i== NUM_CARDS-1){
				topCard= createCardButton(new Card(2, Card.CLUBS), new Point(10+i*LAYER_PANE_WIDTH/8, 30));
				layeredPane.add(topCard, new Integer(i));
			}
			else{
				cardLabels[i]= createCardLabel(new Card(2, Card.CLUBS), new Point(10+i*LAYER_PANE_WIDTH/8, 30));
				layeredPane.add(cardLabels[i], new Integer(i)); 

			}
		}
		clearCards();

		contentPane.add(layeredPane);

		//Create play card button 
		deckButton = new JButton("Play card");
		deckButton.setBounds(241, 385, 199, 106);
		deckButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				serverLink.sendPlayCard();
			}
		});
		deckButton.setEnabled(false);
		contentPane.add(deckButton);		

		//Create Message Field
		lblMessageTextField = new JLabel("Click the button to play cards; click the face-up card to slap");
		lblMessageTextField.setBounds(165, 11, 400, 60);
		lblMessageTextField.setBorder(BorderFactory.createTitledBorder("Messages"));
		contentPane.add(lblMessageTextField);
		
		// Create reconnect button
		btnReconn = new JButton("Reconnect");
		btnReconn.setBounds(35, 466, 97, 25);
		btnReconn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				serverLink = new Client(PORT, ADDR, cv);
				serverLink.start();
				btnReconn.setEnabled(false);
			}
		});
		btnReconn.setEnabled(false);
		contentPane.add(btnReconn);
		
		JLabel pileCounterTitle = new JLabel("Cards in pile");
		pileCounterTitle.setHorizontalAlignment(SwingConstants.CENTER);
		pileCounterTitle.setBounds(527, 180, 88, 25);
		contentPane.add(pileCounterTitle);
		
		pileCounter = new JLabel("0");
		pileCounter.setFont(new Font("Tahoma", Font.PLAIN, 24));
		pileCounter.setHorizontalAlignment(SwingConstants.CENTER);
		pileCounter.setBounds(527, 207, 88, 32);
		contentPane.add(pileCounter);
		
		serverLink = new Client(PORT, ADDR, this);
		serverLink.start();
		
		idsToPlayers = new HashMap<Integer, Player>();
	}

	/**Creates a label with a card image and places it at the 
	 * points specified by origin
	 * @param c - the card from which to fetch the image
	 * @param origin - point of top left corner of label
	 * @return Jlabel containing an icon
	 */
	private JLabel createCardLabel(Card c, Point origin) {  
		JLabel label = new JLabel(); 
		ImageIcon cardpic= c.getImage(CARD_SCALE);
		label.setIcon(cardpic);
		label.setVerticalAlignment(JLabel.TOP);        
		label.setHorizontalAlignment(JLabel.CENTER);        
		label.setOpaque(true);               
		label.setBounds(origin.x, origin.y, cardpic.getIconWidth(), cardpic.getIconHeight());        
		return label;
		
	}

	/**
	 * Creates a button with a card image and places it at the 
	 * points specified by origin    
	 * @param origin point of top left corner of label
	 * @return Jlabel containing an icon
	 */
	private JButton createCardButton(Card card, Point origin) {  
		JButton button = new JButton(); 
		ImageIcon cardpic= card.getImage(.3);
		button.setIcon(cardpic);                  
		button.setBounds(origin.x, origin.y, cardpic.getIconWidth(), cardpic.getIconHeight()); 
		//when top card is clicked, server is notified of slap
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				serverLink.sendSlap();
				//play slapping sound?
			}
		});
		return button; 
	}

	/**
	 * Removes bottom card and shifts down other
	 * visible cards, adds newCard to the top
	 * @param newCard - the card from which to fetch the image
	 */
	public void playCardGUI (Card newCard){

		//shift all cards down by moving icons
		for (int i=0; i<NUM_CARDS-2; i++){
			if (cardLabels[i+1] != null)
				cardLabels[i].setIcon(cardLabels[i+1].getIcon());
		}
		if (topCard.getIcon() != null)
			cardLabels[NUM_CARDS-2].setIcon(topCard.getIcon());
		
		topCard.setIcon(newCard.getImage(CARD_SCALE));
	}
	
	/**
	 * Clears all card images.
	 */
	public void clearCards(){
		for (int i=0; i<=NUM_CARDS-2; i++){
			cardLabels[i].setIcon(null);
		}
		
		topCard.setIcon(null);
	}
	
	/**
	 * Adds a player with name to the jtable.
	 * He starts with a card count of 0.
	 * If the id has already been added, replaces the name.
	 * Otherwise, adds the name, even if the same name is
	 * in the table.
	 * @param p - the player to add
	 */
	public void addPlayer(Player p)
	{
		idsToPlayers.put((int) p.id, p);
		dtm.addRow(new Object[]{p, 0});
	}
	
	/**
	 * Adds a player with name to the jtable.
	 * He starts with a card count of 0.
	 * If the id has already been added, replaces the name.
	 * Otherwise, adds the name, even if the same name is
	 * in the table.
	 * @param p - the player's name
	 * @param id - the player's id
	 */
	public void addPlayer(String name, int id)
	{
		Player p = new Player(name, (byte) id);
		idsToPlayers.put(id, p);
		dtm.addRow(new Object[]{p, 0});
	}
	
	/**
	 * Removes a player with specified id from the GUI's player table.  Does
	 * nothing if the player is not in the table.
	 * 
	 * @param id - the id of the player to remove
	 */
	public void removePlayer(int id)
	{
		int row = findPlayer(id);
		if (row >= 0)
		{
			idsToPlayers.remove(id);
			dtm.removeRow(row);
		}
	}
	
	/**
	 * Gets the name of the player with specified ID.  If the player is not found,
	 * returns null.
	 * 
	 * @param id - the id of the player
	 */
	public String getPlayerName(int id)
	{
		Player p = idsToPlayers.get(id);
		if (p == null)
			return null;
		
		return p.getName();
	}
	
	/**
	 * Finds a player with specified id in the GUI's table.  If not found,
	 * returns -1.
	 * 
	 * @param id - the id of the player
	 * @return the row number of the player in the table, or -1 if the player
	 * was not found
	 */
	private int findPlayer(int id)
	{
		Player p = idsToPlayers.get(id);
		if (p == null)
			return -1;
		
		for (int i = 0; i < dtm.getDataVector().size(); i++)
		{
			if (dtm.getValueAt(i, 0).equals(p)) // Row i, col 0
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Sets the number of cards for a player of certain id.  Does nothing if
	 * the player is not in the table.
	 * 
	 * @param id - the id of the player
	 * @param numCards - the new number of cards
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" }) // Unfortunately, there is no type-safe way.
	public void setPlayerCards(int id, int numCards)
	{
		int row = findPlayer(id);
		if (row < 0)
			return;

		Vector rowVec = (Vector) dtm.getDataVector().get(row);
		rowVec.set(1, numCards); // Column 1
		dtm.fireTableRowsUpdated(row, row);
	}
	
	/**
	 * Increases or decreases the number of cards that a player has.  Does nothing if
	 * the player is not in the table.
	 * 
	 * @param id - the id of the player
	 * @param howMany - how many cards to add or subtract
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" }) // Unfortunately, there is no type-safe way.
	public void changePlayerCards(int id, int howMany)
	{
		int row = findPlayer(id);
		if (row < 0)
			return;
		
		Vector rowVec = (Vector) dtm.getDataVector().get(row);
		int old = (int) rowVec.get(1);
		rowVec.set(1, old + howMany); // Column 1
		dtm.fireTableRowsUpdated(row, row);
	}
	
	/**
	 * Enables the "reconnect" button of the GUI.
	 */
	public void enableReconnectButton() { btnReconn.setEnabled(true); }
	
	/**
	 * Enables or disables the button that allows the user to play a card.
	 * @param enable - true for enabled, false for disabled
	 */
	public void setEnabledPlayCardButton(boolean enable) { deckButton.setEnabled(enable); }
	
	/**
	 * Sets the counter for how many cards are in the pile
	 * @param cnt - the number to display
	 */
	public void setPileCounter(int cnt) { pileCounter.setText(Integer.toString(cnt)); }
	
	/**
	 * Sets the message displayed to users
	 * @param message message to display
	 */
	public void setMessageGUI(String message){
		lblMessageTextField.setText(message);
	}
	
	/**
	 * Highlights the player whos turn it is to play cards in the JTable
	 * @param id- id of the current player's turn
	 */
	public void indicatePlayerTurn(int id){
		int row = findPlayer(id);
		if (row < 0)
			return;
		
		table.setRowSelectionInterval(row, row);
	}
	
	/**
	 * Asks the user to provide a player name
	 * @return the name that the user provided
	 */
	public String askForName(){
		
		String s = (String)JOptionPane.showInputDialog(
                this,
                "Please enter your name:",
                "Enter Name",
                JOptionPane.PLAIN_MESSAGE);
		return s;
	}
	
	/**
	 * Shows an error message to the user.
	 * @param message - the message's contents
	 */
	public void showErrDialog(String message)
	{
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
