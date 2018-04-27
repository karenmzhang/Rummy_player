/******************************************************************************
Monte Carlo Tree Search using UCT 
******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Mcts {
    private final static double UCT_CONSTANT = Math.sqrt(2); // for UCT calculation
    private final static int maxDeadWood = 110;

    public Random rand = ThreadLocalRandom.current();
    private Node root;

    private int simulations; // total number of simulations executed
    private Deck deck;
    private Hand myHand;
    private Hand oppHand;
    private Deck discardPile;

    // this will set up the root and the "chance node" from which all future
    // exploration will branch
    public Mcts(Hand myHand, String player1move, Deck discardPile) {
	// known information
	root = new Node();
	root.p1 = true;
	root.move = player1move;
	root.discard = discardPile.peekBottomCard();
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
	simulations++;
	Node chanceNode = root.children.get(0);

	// does one step of the search
	// first, determine a game state from the chance node
	// each step will feature a different determinzation. This 
	// may change what is possible, but hopefully it will not be 
	// too inefficient.
	determine(chanceNode);

	// then, move down the tree until you find a leaf. Choose where to
	// go using UCT (choose method)
	Node choice = choose(chanceNode);
	System.out.println(choice);
	// update as if this choice has been played


	// Then, expand the node that you found (should always have 22 options).
	// Choose a random one out of the nodes you just created.
	Node expanded = expand(choice);
	for (Node blah : choice.children) {
	    System.out.print(blah.printType());
	}
	System.out.println(expanded);
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
	Hand h = new Hand(chanceNode.h2);
	Card c = new Card(topOfDeck.rank, topOfDeck.suit);
	h.draw(c);
	for (int i = 0; i < h.allCards.size(); i++) {
	    Node newNode = new Node();
	    newNode.p2 = true;
	    newNode.move = s;
	    newNode.discard = h.allCards.get(i);
	    newNode.h2 = new Hand(h);
	    newNode.h2.discard(newNode.discard);
	    newNode.h1 = oppHand;
	    if (!chanceNode.childrenContains(newNode)) {
		newNode.parent = chanceNode;
		chanceNode.children.add(newNode);
	    }
	}

	// 11 children for discardPile option
	String s1 = "discardPile";
	Card dp = discardPile.peekBottomCard();
	Hand h1 = new Hand(chanceNode.h2);
	Card c1 = new Card(dp.rank, dp.suit);
	h1.draw(c1);
	for (int i = 0; i < h.allCards.size(); i++) {
	    Node newNode = new Node();
	    newNode.p2 = true;
	    newNode.move = s1;
	    newNode.discard = h1.allCards.get(i);
	    newNode.h2 = new Hand(h);
	    newNode.h2.discard(newNode.discard);
	    newNode.h1 = oppHand;
	    if (!chanceNode.childrenContains(newNode)) {
		newNode.parent = chanceNode;
		chanceNode.children.add(newNode);
	    }
	}
    }

    public Node choose(Node chanceNode) {
	if (simulations < 10) {
	    int best = maxDeadWood;
	    Node bestNode = chanceNode.children.get(0);
	    for (Node c : chanceNode.children) {
		if (c.h2.deadWood() < best) {
		    best = c.h2.deadWood();
		    bestNode = c;
		}
	    }
	    if (bestNode.move.equals("deck")) {
		deck.removeTopCard();
	    }
	    if (bestNode.move.equals("discardPile")) {
		discardPile.removeBottomCard();
	    }
	    discardPile.addSpecificCard(bestNode.discard);
	    return bestNode;
	}
	else {
	    //System.out.println("hi");
	    Node current = chanceNode;
	    Node bestNode = chanceNode.children.get(0);
	    // while not at a leaf
	    while (current.children.size() > 0) {
		double best = Double.MAX_VALUE;
		for (Node c : chanceNode.children) {
		    if (c.uct(simulations) < best) {
			best = c.uct(simulations);
			bestNode = c;
		    }
		}
		current = bestNode;
		if (bestNode.move.equals("deck")) {
		    deck.removeTopCard();
		}
		if (bestNode.move.equals("discardPile")) {
		    discardPile.removeBottomCard();
		}
		discardPile.addSpecificCard(bestNode.discard);
	    }
	    return bestNode;
	}
    }

    public Node expand(Node choice) {
	if (choice.p2) {
	    String s = "deck";
	    Card topOfDeck = deck.peekTopCard();
	    Hand h = new Hand(choice.h1);
	    Card c = new Card(topOfDeck.rank, topOfDeck.suit);
	    h.draw(c);
	    for (int i = 0; i < h.allCards.size(); i++) {
		Node newNode = new Node();
		newNode.p1 = true;
		newNode.move = s;
		newNode.discard = h.allCards.get(i);
		newNode.h1 = new Hand(h);
		newNode.h1.discard(newNode.discard);
		newNode.h2 = choice.h2;
		newNode.parent = choice;
		choice.children.add(newNode);
	    }

	    String sa = "discardPile";
	    Card dp = discardPile.peekBottomCard();
	    Hand ha = new Hand(choice.h1);
	    Card ca = new Card(dp.rank, dp.suit);
	    ha.draw(ca);
	    for (int i = 0; i < ha.allCards.size(); i++) {
		Node newNode = new Node();
		newNode.p1 = true;
		newNode.move = sa;
		newNode.discard = ha.allCards.get(i);
		newNode.h1 = new Hand(ha);
		newNode.h1.discard(newNode.discard);
		newNode.h2 = choice.h2;
		newNode.parent = choice;
		choice.children.add(newNode);
	    }
	    int r = rand.nextInt(choice.children.size()); 
	    return choice.children.get(r);
	}
	// choice is p1
	else {
	    String s = "deck";
	    Card topOfDeck = deck.peekTopCard();
	    Hand h = new Hand(choice.h2);
	    Card c = new Card(topOfDeck.rank, topOfDeck.suit);
	    h.draw(c);
	    for (int i = 0; i < h.allCards.size(); i++) {
		Node newNode = new Node();
		newNode.p2 = true;
		newNode.move = s;
		newNode.discard = h.allCards.get(i);
		newNode.h2 = new Hand(h);
		newNode.h2.discard(newNode.discard);
		newNode.h1 = choice.h1;
		newNode.parent = choice;
		choice.children.add(newNode);
	    }

	    String sa = "discardPile";
	    Card dp = discardPile.peekBottomCard();
	    Hand ha = new Hand(choice.h2);
	    Card ca = new Card(dp.rank, dp.suit);
	    ha.draw(ca);
	    for (int i = 0; i < ha.allCards.size(); i++) {
		Node newNode = new Node();
		newNode.p2 = true;
		newNode.move = sa;
		newNode.discard = ha.allCards.get(i);
		newNode.h2 = new Hand(ha);
		newNode.h2.discard(newNode.discard);
		newNode.h1 = choice.h1;
		newNode.parent = choice;
		choice.children.add(newNode);
	    }
	    int r = rand.nextInt(choice.children.size()); 
	    return choice.children.get(r);
	}
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
	    visits = 1;
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

	public boolean childrenContains(Node child) {
	    for (Node c : children) {
		if (c.p2equals(child) || c.p1equals(child))
		    return true;
	    }
	    return false;
	}

	// calculates score of the node using UCT 
	public double uct(double simulations) {
	    return ((double) wins)/((double) visits) + UCT_CONSTANT*Math.sqrt(Math.log(simulations)/((double) visits));
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

	public String printType() {
	    if (p1) return "p1";
	    if (chance) return "chance";
	    if (p2) return "p2";
	    return "error";
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
	game.discardPile.addSpecificCard(discard);
	Mcts test = new Mcts(game.h2, s1, game.discardPile);
	//System.out.println(test);
	//System.out.println(test.root.children.get(0));
	test.search();
	/*System.out.println("my hand: " + game.h2);
	  System.out.println("determined deck: " + test.deck);
	  System.out.println("determined opp hand: " + test.oppHand);
	  System.out.println("actual deck: " + game.deck);
	  System.out.println("actual opp: " + game.h1);*/
	System.out.println(test.root.printType());
	for (Node c : test.root.children) {
	    System.out.println(c.printType());
	    for (Node c1 : c.children) {
		System.out.print(c1.printType());
	    }
	}

    }

}
