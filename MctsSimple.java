/******************************************************************************
An implementation of MCTS for Rummy, where the AI does not try to use general
Rummy strategies but only relies on randomness in the MCTS traversal.
	Things to try in the future: 
	- keeping track of what information we know about the opponent to prune 
	some chance/opponent branches
	- using generally good strategies to also prune some decision branches
******************************************************************************/


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MctsSimple {
	public Random rand = ThreadLocalRandom.current();
	private Node root;
	private Hand myHand; // necessary?
	private boolean player1; // am I the first player?

	public MctsSimple(Hand myHand, boolean player1, Deck discardPile) {
		root = new Node();
		this.myHand = myHand;
		this.player1 = player1;

		// make a game that mirrors the information we know
		// the root is basically a dummy 
		Deck deck = new Deck(); // creates a full deck
		// edits deck to not include discard pile cards or my hands cards
		for (Card c : myHand.allCards) {
			deck.removeSpecificCard(c);
		}

		for (Card c : discardPile.cards) {
			deck.removeSpecificCard(c);
		}

		Hand opponentHand = new Hand(); // empty hand

		/*if (player1) {
			Game game = new Game(deck, myHand, opponentHand);
		}*/
		// for now, assume I am player 2
		// else
		Game game = new Game(deck, opponentHand, myHand);

		game.discardPile = discardPile;
		root.state = game;
	}

	public String toString() {
		String s = "";
		s += root;
		//s += myHand;
		return s;
	}

	// inspired by https://github.com/haroldsultan/MCTS
	private class Node {
		private int visits; // how many times has this node been visited
		private double reward; // estimate of the strength of this node
		private Game state; // current game state
		private ArrayList<Node> children;
		private Node parent;
		private boolean chance; // is this a chance node?
		private String move; // deck or discardPile
		// also indicators for whose turn it is, 

		public Node() {
			visits = 1;
			reward = 0;
			state = null;
			children = new ArrayList<Node>();
			parent = null;
			chance = false;
			move = "";
		}

		public String toString() {
			String s = "";
			s += "Visits: " + visits + "\n";
			s += "Reward: " + reward + "\n";
			s += "Deck state: " + state.deck + "\n";
			s += "Discard state: " + state.discardPile + "\n";
			s += "hand1 state: " + state.h1 + "\n";
			s += "hand2 state: " + state.h2 + "\n";
			s += "Children number: " + children.size() + "\n";
			return s;
		}		
	}

	// produce a random move 
	public String makeMove() {
		//Random rand = ThreadLocalRandom.current();
		Double r = rand.nextDouble();
		if (r < 0.5) 
			return "deck";
		else return "discardPile";
	}

	// produce a random index for discarding a card
	public int discard(){
		//Random rand = ThreadLocalRandom.current();
		int r = rand.nextInt(11);
		return r;
	}

	/*public ArrayList<Card> findDesired(Hand hand) {
		ArrayList<Card> desired = new ArrayList<Card>();

		// sort in rank order to find sets
		Collections.sort(hand.allCards); 
		for (int i = 0; i < hand.allCards.size() - 1; i++) {
			// if found a double
			if (hand.allCards.get(i).rank == hand.allCards.get(i+1).rank) {
				// just add all the cards of this rank 
				for (int j = 1; j < 5; j++) {
					Card c = new Card(hand.allCards.get(i).rank, j);
					if (c.isValid()) desired.add(c);
				}
			}
		}

		// sort in suit order to find runs		
		Collections.sort(hand.allCards, Card.bySuitOrder());
		for (int i = 0; i < hand.allCards.size() - 1; i++) {
			// if found two cards of the same suit
			if (hand.allCards.get(i).suit == hand.allCards.get(i+1).suit) {

				// if the two cards are consecutive
				if (hand.allCards.get(i).rank == hand.allCards.get(i+1).rank - 1) {
					Card c1 = new Card(hand.allCards.get(i).rank - 1, hand.allCards.get(i).suit);
					if (c1.isValid()) desired.add(c1);
					Card c2 = new Card(hand.allCards.get(i+1).rank + 1, hand.allCards.get(i).suit);
					if (c2.isValid()) desired.add(c2);
				}
			}
		}

		return desired;
	}*/

	// USE THIS TO CHOOSE A CHILD OF THE ROOT TO EXPLORE
	public Node select() {
		/*Deck deckChild = new Deck();
		for (Card c : myHand.allCards) {
			deckChild.removeSpecificCard(c);
		}

		for (Card c : discardPile.cards) {
			deckChild.removeSpecificCard(c);
		}*/

		Deck deckChild = new Deck(root.state.deck); // deep copy of deck
		deckChild.shuffle();

		// new hand for the opponent
		Hand opponentHandChild = new Hand();
		for (int i = 0; i < 10; i++) {
			opponentHandChild.draw(deckChild.removeTopCard());
		}

		Game gameChild = new Game(deckChild, opponentHandChild, myHand, root.state.discardPile);

		Node nodeChild = new Node();
		nodeChild.reward = myHand.deadWood();
		nodeChild.state = gameChild;
		nodeChild.parent = root;
		root.children.add(nodeChild);

		return nodeChild;
		/*Node current = root;

		Random rand = ThreadLocalRandom.current();
		while (current.children.size() > 0) {
			// randomly choose a child to be the next in our step
			int r = rand.nextInt(0, current.children.size());
			current = current.children.get(r);
		}

		return current;*/
	}

	public boolean listContainsCard(ArrayList<Card> list, Card c) {
		for (Card card : list) {
			if (card.equals(c))
				return true;
		}
		return false;
	}

	/*public String makeMove(Node node) {
		ArrayList<Card> desired = this.findDesired(node.state.h1);

		// if top of discard is something you desire, take it
		Card topOfDiscard = node.state.discardPile.peekBottomCard();
		if (topOfDiscard != null) {
			if (this.listContainsCard(desired, topOfDiscard)) {
				//lastDrawnFromDiscard = true;
				return "discardPile";
			}
		}

		//else {
		//	lastDrawnFromDiscard = false;
		//}

		return "deck";
	}*/

	// expand from a certain node 
	// (will always have 11 choices for something to discard)
	// note: also randomly choose one of 2 options, discard or deck
	// total 22 choices
	public Node expand(Node node) {
		//String move = this.makeMove(node);

		for (int i = 0; i < 11; i++) {
			/*Node child = new Node();

			Deck deck = new Deck();
			for (Card c : myHand.allCards) {
				deck.removeSpecificCard(c);
			}

			for (Card c : node.state.discardPile) {
				deck.removeSpecificCard(c);
			}*/
		}
		return null;
	}

	public Node simulate() {
		return null;
	}

	public Node backpropagate() {
		return null;
	}

	// choose best card to discard
	// (look at all of the 1-level children of root)
	// (then take a tally of the cards that are discarded)
	// use something like UCT to decide which one did the best
	public Card chooseBest() {
		return null;
	}

	public static void main(String[] args) {

		// ASSUMES I AM PLAYER 2
		Game game = new Game();
		String s1;
		Card discard;
		String winner;
		String knocker;

		Player_Good player1 = new Player_Good(game.h1);

		game.discardPile.addSpecificCard(game.deck.removeTopCard());
		int count = 0; // number of rounds

		// player 1's move
		player1.setTopOfDiscard(game.discardPile.peekBottomCard());
		s1 = player1.makeMove();

		if (s1.equals("deck")) {
			player1.draw(game.deck.removeTopCard());
		}
		if (s1.equals("discardPile")) {
			player1.draw(game.discardPile.removeBottomCard());
		}
		discard = player1.discard();
		game.discardPile.addSpecificCard(discard);

		System.out.println(discard);

		MctsSimple mcts = new MctsSimple(game.h2, false, game.discardPile);
		System.out.println(mcts);
		Node selected = mcts.select();
		System.out.println("***********************************************");
		System.out.println(selected);
		//String something = mcts.expand(selected);
		//System.out.println(something);




		// System.out.println("hi");
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;
		int i = 0;

		//while (true) {
			
			/***************** take care of player 1's moves **********************/

		/*	System.out.println("Round: " + count);
			System.out.println("Discard: " + game.discardPile.toString());
			System.out.println("Deck: " + game.deck.toString());

			System.out.println("Player1 hand: ");
			System.out.println(player1);
			System.out.println("Player1 deadWood: " + player1.hand.deadWood());

			// make Player 1's move
			player1.setTopOfDiscard(game.discardPile.peekBottomCard());
			s1 = player1.makeMove();

			if (s1.equals("deck")) {
				player1.draw(game.deck.removeTopCard());
			}
			if (s1.equals("discardPile")) {
				player1.draw(game.discardPile.removeBottomCard());
			}
			//System.out.println("Player1 move: " + s1);

			// make Player 1's discard
			discard = player1.discard();
			game.discardPile.addSpecificCard(discard);
			//System.out.println("Player1 Discard: " + discard);

			game.h1 = player1.hand;

			// check to see if the game should end
			if (game.deck.size() <= 2) { 
				winner = game.endGame();
				knocker = "Deck";
				break;
			}
			if (player1.knock()) {
				winner = game.endGame();
				knocker = "player1";
				break;
			}
			*/
			/******************************************************************/

			// start mcts to figure out 2nd player's moves 
	    	/*MctsSimple mcts = new MctsSimple(game.h2, false, game.discardPile);

	    	// currently allow 30 seconds
	    	while (elapsedTime < 2*60*1000) {
	    		// branching factor to use is 10 currently
	    		Node selected = mcts.select(); // just choose one branch from root
	    		Node expanded = mcts.expand(selected); // try one of the children of the selected one
	    		Node simulated = mcts.simulate(expanded); // simulate to the end of the game
	    		mcts.backpropagate(simulated); // backpropagate
	    	}
	    	System.out.println(mcts);

	    	discard = mcts.chooseBest(); 

		} */
		// System.out.println("bye");
	}
}