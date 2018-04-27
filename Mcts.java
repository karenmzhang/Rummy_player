/******************************************************************************
Monte Carlo Tree Search using UCT 
******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Mcts {
    private final static double c = Math.sqrt(2); // for UCT calculation

    public Random rand = ThreadLocalRandom.current();
    private Node root;

    private int simulations; // total number of simulations executed
    private Deck deck;
    private Hand myHand;
    private Hand oppHand;
    private Deck discardPile;

    // this will set up the root and the "chance node" from which all future
    // exploration will branch
    public Mcts(Hand myHand, String player1move, Deck discardPile, Card discard) {
	// known information
	root = new Node();
	root.p1 = true;
	root.move = player1move;
	root.discard = discard;
	root.discardPile = discardPile;
	root.h2 = myHand;
	
	// create the chance node from the root
	Node chanceNode = new Node();
	chanceNode.chance = true;
	chanceNode.h2 = myHand;

	// make a list of possible cards still in the deck
	Deck p = new Deck(); 
	for (Card c : myHand.allCards)
	    p.removeSpecificCard(c);
	for (Card c : discardPile.cards)
	    p.removeSpecificCard(c);
	p.removeSpecificCard(discard);

	chanceNode.possibleDeck = p;
	chanceNode.discardPile = discardPile;
	chanceNode.parent = root;

	// add chance node to the root as a child
	root.children.add(chanceNode);

	this.deck = null;
	this.myHand = null;
	this.oppHand = null;
	this.discardPile = null;
    }

    public String toString() {
	String s = "";
	s += root;
	return s;
    }

    public void search() {
	// does one step of the search
	// first, determine a game state from the chance node
	// each step will feature a different determinzation. This 
	// may change what is possible, but hopefully it will not be 
	// too inefficient. 
	Node chanceNode = root.children.get(0);
	determine(chanceNode);

	// then, move down the tree until you find a leaf. Choose where to
	// go using UCT (choose method)

	// Then, expand the node that you found (should always have 22 options).
	// Choose a random one out of the nodes you just created.

	// From the chosen node, simulate a (random/heuristic) game.

	// backpropagate the result.
    }

    public void determine(Node chanceNode) {
	this.deck = new Deck(chanceNode.possibleDeck);
	if (deck.size() < 10) return;
	deck.shuffle();

	this.myHand = new Hand(chanceNode.h2);
	this.discardPile = new Deck(chanceNode.discardPile);

	// new hand for the opponent
	this.oppHand = new Hand();
	for (int i = 0; i < 10; i++) {
	    oppHand.draw(deck.removeTopCard());
	}

	// add 22 children based on possible moves

	// 11 children for deck option
	String s = "deck";
	Card topOfDeck = deck.peekTopCard();
	for (int i = 0; i < 11; i++) {
	    Hand h = new Hand(chanceNode.h2);
	    Card c = new Card(topOfDeck.rank, topOfDeck.suit);
	    h.draw(c);
	    Card discard = h.allCards.get(i);
	}
	for (Node n : chanceNode.children) {

	}
    }

    public void choose() {
	Node current = root;

    }

    public Node expand(Node e) {
	return null;
    }

    public void simulate(Node s) {

    }

    public void backpropagate(Node b) {

    }

    // inspired by https://github.com/haroldsultan/MCTS
    private class Node {
	
	// which kind of node is this?
	private boolean p1; // player 1's move
	private boolean chance; // chance node
	private boolean p2; // player 2's move

	// keeping track of simulation information 
	private int visits; // how many times has this node been visited
	private int wins; // how many times have I won from this node?

	// keeping track of gameplay
	private String move; // discardPile or deck
	private Card discard; // which card gets discarded by this player
	private Hand h1; // the cards player 1 has
	private Hand h2; // the cards player 2 has

	// keeping track of game state
	private Deck discardPile;
	private Deck deck; 

	// for chance nodes, what are possible outcomes?
	private Deck possibleDeck; // all possible cards that could still be in deck

	// node structure
	private Node parent;
	private ArrayList<Node> children;

	public Node() {
	    move = null;
	    discard = null;
	    h1 = null;
	    h2 = null;
	    discardPile = null;
	    deck = null;
	    possibleDeck = null;
	    parent = null;
	    children = new ArrayList<Node>();
	}

	public boolean p2equals(Node that) {
	    if (!this.p2 || !that.p2) return false;
	    if (!this.move.equals(that.move)) return false;
	    if (!this.discard.equals(that.discard)) return false;
	    return true;
	}

	public boolean p1equals(Node that) {
	    if (!this.p1 || !that.p1) return false;
	    if (!this.move.equals(that.move)) return false;
	    if (!this.discard.equals(that.discard)) return false;
	    return true;
	}

	public String toString() {
	    String s = "";
	    if (p1) {
		s += "p1" + "\n";
		s += "hand: " + h1+ "\n";
		s += "move: " + move + "\n";
		s += "discard: " + discard + "\n";
	    }
	    if (chance) {
		s += "chance" + "\n";
		s += "possible deck: " + possibleDeck + "\n";
	    }
	    if (p2) {
		s += "p2" + "\n";
		s += "hand: " + h2 + "\n"; 
		s += "visits: " + visits + "\n"; 
		s += "wins: " + wins + "\n"; 
	    }
	    return s;
	}
    }

    public static void main(String[] args) {
	Game game = new Game();

	// add one card to the discard pile
	game.discardPile.addSpecificCard(game.deck.removeTopCard());
	int count = 0; // number of rounds

	Player_Random player1 = new Player_Random(game.h1);
	String s1 = player1.makeMove();
	if (s1.equals("deck")) {
	    player1.draw(game.deck.removeTopCard());
	}
	if (s1.equals("discardPile")) {
	    player1.draw(game.discardPile.removeBottomCard());
	}
	Card discard = player1.discard();
	Mcts test = new Mcts(game.h2, s1, game.discardPile, discard);
	System.out.println(test);
	System.out.println(test.root.children.get(0));
	test.search();
	System.out.println("my hand: " + game.h2);
	System.out.println("determined deck: " + test.deck);
	System.out.println("determined opp hand: " + test.oppHand);
	System.out.println("actual deck: " + game.deck);
	System.out.println("actual opp: " + game.h1);
    }

}
