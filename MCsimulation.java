/******************************************************************************
Monte Carlo simulations of Rummy playouts - no tree structure
******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MCsimulation {
    public Random rand = ThreadLocalRandom.current();
    private Deck discardPile;
    private Deck knownDeck;
    public Hand myHand;
    public int dpScore; // how many games have been won by playing discardPile
    public int dScore; // how many games have been won by playing deck
    private HashSet<Card> beliefState; // what cards I believe opponent might have
    private int counter;
    private int draw;
    private HashMap<Card, Integer> discardChoices;

    public MCsimulation(Hand myHand, Deck discardPile) {
	this.myHand = myHand;
	this.discardPile = discardPile;
	dpScore = 0;
	dScore = 0;
	counter = 0;
	draw = 0;
	beliefState = new HashSet<Card>();
	discardChoices = new HashMap<Card, Integer>();

	knownDeck = new Deck();

	// edits deck to not include discard pile cards or my hands cards
	for (Card c : myHand.allCards) {
	    knownDeck.removeSpecificCard(c);
	}

	for (Card c : discardPile.cards) {
	    knownDeck.removeSpecificCard(c);
	}

	for (Card c : knownDeck.cards) {
	    beliefState.add(c);
	}
    }

    public String toString() {
	String s = "";
	s += "Hand: ";
	s += myHand;
	s += "Discard: ";
	s += discardPile;
	return s;
    }

    public String stats() {
	String s = "";
	s += dpScore;
	s += "\n";
	s += dScore;
	s += "\n";
	s += counter;
	s += "\n";
	s += draw;
	return s;
    }


    // if b is true, means that the card was something discarded by the opp
    // if b is false, means that the card was something picked up by the opp
    public void updateBeliefs(Card card, boolean b) {
    }

    public String discardStats() {
	String s = "";
	s += discardChoices;
	return s;
    }

    public void updateKnownDeck() {
	for (Card c : myHand.allCards) {
	    knownDeck.removeSpecificCard(c);
	}

	for (Card c : discardPile.cards) {
	    knownDeck.removeSpecificCard(c);
	}
    }

    // see if this player could knock right now
    public boolean knock() {
	if (myHand.deadWood() <= 10) return true;
	else return false;
    }

    public void makeDiscardChoices() {
	discardChoices = new HashMap<Card, Integer>();
	for (Card c : myHand.allCards) {
	    discardChoices.put(c, 0);
	}
    }

    public void discard(){
	
	/*myHand.shuffle(); // consider optimizing to not need a shuffle (choose
	  // a random index instead)
	  Card c = myHand.allCards.get(0);
	  myHand.discard(c);
	  return c;*/
	Deck deckChild = new Deck(knownDeck); // copy of known deck
	if (deckChild.size() < 10) return;
	deckChild.shuffle();

	Hand myHandCopy = new Hand(myHand);
	Deck discardPileCopy = new Deck(discardPile);

	// new hand for the opponent
	Hand opponentHandChild = new Hand();
	for (int i = 0; i < 10; i++) {
	    opponentHandChild.draw(deckChild.removeTopCard());
	}

	Game gameChild = new Game(deckChild, opponentHandChild, myHandCopy, discardPileCopy);

	Player_Random player1 = new Player_Random(opponentHandChild);
	Player_Random player2 = new Player_Random(myHandCopy);

	int count = 0;
	String s1;
	String s2;
	String winner = "";
	String knocker; // who was the one to knock
	Card discard;
	Card discardChoice = new Card(-1,-1);

	String myMove = "";

	// end result should be 
	while (true) {
	    if (count > 1000) {
		winner = "Draw";
		break;
	    }

	    // make Player 2's discard
	    discard = player2.discard();
	    if (count == 0) {
		discardChoice = discard;
	    }
	    gameChild.discardPile.addSpecificCard(discard);
	    count++;

	    gameChild.h2 = player2.hand;

	    count++;

	    // check to see if the game should end
	    if (gameChild.deck.size() <= 2) { 
		winner = gameChild.endGame();
		knocker = "Deck";
		break;
	    }
	    if (player2.knock()) {
		winner = gameChild.endGame();
		knocker = "player2";
		break;
	    }

	    if (winner.equals("player1")) {
		if (myMove.equals("deck")) {
		    dScore--;
		}
		if (myMove.equals("discardPile")) {
		    dpScore--;
		}
	    }
	    if (winner.equals("player2")) {
		if (myMove.equals("deck")) {
		    dScore++;
		}
		if (myMove.equals("discardPile")) {
		    dpScore++;
		}
	    }
	    if (winner.equals("Draw")) draw++;

	    // make Player 1's move
	    //player1.setTopOfDiscard(gameChild.discardPile.peekBottomCard());
	    s1 = player1.makeMove();

	    if (s1.equals("deck")) {
		player1.draw(gameChild.deck.removeTopCard());
	    }
	    if (s1.equals("discardPile")) {
		player1.draw(gameChild.discardPile.removeBottomCard());
	    }

	    // make Player 1's discard
	    discard = player1.discard();
	    gameChild.discardPile.addSpecificCard(discard);

	    gameChild.h1 = player1.hand;

	    // check to see if the game should end
	    if (gameChild.deck.size() <= 2) { 
		winner = gameChild.endGame();
		knocker = "Deck";
		break;
	    }
	    if (player1.knock()) {
		winner = gameChild.endGame();
		knocker = "player1";
		break;
	    }

	    // make Player 2's move
	    //player2.setTopOfDiscard(gameChild.discardPile.peekBottomCard());
	    s2 = player2.makeMove();

	    if (s2.equals("deck")) {
		player2.draw(gameChild.deck.removeTopCard());
	    }
	    if (s2.equals("discardPile")) {
		player2.draw(gameChild.discardPile.removeBottomCard());
	    }
	}

	if (winner.equals("player1")) {
	    for (Card c : discardChoices.keySet()) {
		if (c.rank == discardChoice.rank && c.suit == discardChoice.suit) {
		    int k = discardChoices.get(c);
		    k--;
		    discardChoices.put(c, k);
		}
	    }
	    
	}
	if (winner.equals("player2")) {
	    for (Card c : discardChoices.keySet()) {
		if (c.rank == discardChoice.rank && c.suit == discardChoice.suit) {
		    int k = discardChoices.get(c);
		    k++;
		    discardChoices.put(c, k);
		}
	    }
	}

    }

    public Card chooseDiscard() {
	Card b = new Card(-1,-1);
	int best = Integer.MIN_VALUE;
	for (Card c : discardChoices.keySet()) {
	    if (discardChoices.get(c) > best) {
		b = c;
		best = discardChoices.get(c);
	    }
	}
	return b;
    }

    // USE THIS TO CHOOSE A CHILD OF THE ROOT AND EXPLORE IT
    public void simulate() {
	counter++;
	updateKnownDeck();

	Deck deckChild = new Deck(knownDeck); // copy of known deck
	deckChild.shuffle();

	Hand myHandCopy = new Hand(myHand);
	Deck discardPileCopy = new Deck(discardPile);

	if (deckChild.size() <= 10) return;

	// new hand for the opponent
	Hand opponentHandChild = new Hand();
	for (int i = 0; i < 10; i++) {
	    opponentHandChild.draw(deckChild.removeTopCard());
	}

	Game gameChild = new Game(deckChild, opponentHandChild, myHandCopy, discardPileCopy);

	Player_Random player1 = new Player_Random(opponentHandChild);
	Player_Random player2 = new Player_Random(myHandCopy);

	int count = 0;
	String s1;
	String s2;
	String winner;
	String knocker; // who was the one to knock
	Card discard;

	String myMove = "";

	// end result should be 
	while (true) {
	    if (count > 1000) {
		winner = "Draw";
		break;
	    }

	    // make Player 1's move
	    //player1.setTopOfDiscard(gameChild.discardPile.peekBottomCard());
	    s1 = player1.makeMove();

	    if (s1.equals("deck")) {
		player1.draw(gameChild.deck.removeTopCard());
	    }
	    if (s1.equals("discardPile")) {
		player1.draw(gameChild.discardPile.removeBottomCard());
	    }

	    // make Player 1's discard
	    discard = player1.discard();
	    gameChild.discardPile.addSpecificCard(discard);

	    gameChild.h1 = player1.hand;

	    // check to see if the game should end
	    if (gameChild.deck.size() <= 2) { 
		winner = gameChild.endGame();
		knocker = "Deck";
		break;
	    }
	    if (player1.knock()) {
		winner = gameChild.endGame();
		knocker = "player1";
		break;
	    }

	    // make Player 2's move
	    //player2.setTopOfDiscard(gameChild.discardPile.peekBottomCard());
	    s2 = player2.makeMove();

	    if (count == 0) {
		myMove = s2;
	    }

	    if (s2.equals("deck")) {
		player2.draw(gameChild.deck.removeTopCard());
	    }
	    if (s2.equals("discardPile")) {
		player2.draw(gameChild.discardPile.removeBottomCard());
	    }

	    // make Player 2's discard
	    discard = player2.discard();
	    gameChild.discardPile.addSpecificCard(discard);
	    count++;

	    gameChild.h2 = player2.hand;

	    // check to see if the game should end
	    if (gameChild.deck.size() <= 2) { 
		winner = gameChild.endGame();
		knocker = "Deck";
		break;
	    }
	    if (player2.knock()) {
		winner = gameChild.endGame();
		knocker = "player2";
		break;
	    }
	}

	if (winner.equals("player1")) {
	    if (myMove.equals("deck")) {
		dScore--;
	    }
	    if (myMove.equals("discardPile")) {
		dpScore--;
	    }
	}
	if (winner.equals("player2")) {
	    if (myMove.equals("deck")) {
		dScore++;
	    }
	    if (myMove.equals("discardPile")) {
		dpScore++;
	    }
	}
	if (winner.equals("Draw")) draw++;

    }

    public static void main(String[] args) {
	int winCount = 0;
	int drawCount = 0;
	for (int z = 0; z < 1000; z++) {
	    
	    String s1; // player 1's last move 
	    String s2; // player 2's last move
	    String winner;
	    String knocker; // who was the one to knock

	    Card discard;
	    Game game = new Game();

	    // add one card to the discard pile
	    game.discardPile.addSpecificCard(game.deck.removeTopCard());
	    int count = 0; // number of rounds

	    Player_Random player1 = new Player_Random(game.h1);

	    // essentially acts as player 2
	    MCsimulation mc = new MCsimulation(game.h2, game.discardPile);

	    /*long startTime = System.currentTimeMillis();
	      long elapsedTime = 0L;
	      int i = 0;
	      System.out.println(mc);
	      mc.updateKnownDeck();
	      mc.makeDiscardChoices();
	      System.out.println(mc.discardStats());
	      while (elapsedTime < 10*1000) {
	      mc.discard();
	      elapsedTime = System.currentTimeMillis() - startTime;
	      }
	      System.out.println(mc.discardStats());
	      System.out.println("Discard: " + mc.chooseDiscard());*/
	    

	    // take turns between player 1 and 2 until one of then knocks
	    while (true) {
		if (count > 1000) {
		    winner = "Draw";
		    break;
		}

		/*System.out.println("Round: " + count);
		  System.out.println("Discard: " + game.discardPile.toString());
		  System.out.println("Deck: " + game.deck.toString());

		  System.out.println("Player1 hand: ");
		  System.out.println(player1);
		  System.out.println("Player1 deadWood: " + player1.hand.deadWood());
		  System.out.println("Player2 hand: ");
		  System.out.println(mc.myHand);
		  System.out.println("Player2 deadWood: " + mc.myHand.deadWood());*/

		// make Player 1's move
		//player1.setTopOfDiscard(game.discardPile.peekBottomCard());
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
		    if (winner.equals("player2")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}
		if (player1.knock()) {
		    winner = game.endGame();
		    knocker = "player1";
		    if (winner.equals("player2")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}

		// make Player 2's move
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;
		while (elapsedTime < 0.5*100) {
		    mc.simulate();
		    elapsedTime = System.currentTimeMillis() - startTime;
		}

		if (mc.dpScore > mc.dScore) {
		    mc.myHand.draw(game.discardPile.removeBottomCard());
		    s2 = "discardPile";
		}
		else {
		    mc.myHand.draw(game.deck.removeTopCard());
		    s2 = "deck";
		    
		}
		//System.out.println("Player2 move: " + s2);

		mc.updateKnownDeck();
		mc.makeDiscardChoices();
		// make Player 2's discard
		startTime = System.currentTimeMillis();
		elapsedTime = 0L;
		while (elapsedTime < 0.5*100) {
		    mc.discard();
		    elapsedTime = System.currentTimeMillis() - startTime;
		}

		discard = mc.chooseDiscard();
		mc.myHand.discard(discard);
		game.discardPile.addSpecificCard(discard);
		//System.out.println("Player2 Discard: " + discard);
		//System.out.print("\n");
		count++;

		game.h2 = mc.myHand;

		// check to see if the game should end
		if (game.deck.size() <= 2) { 
		    winner = game.endGame();
		    knocker = "Deck";
		    if (winner.equals("player2")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}
		if (mc.knock()) {
		    winner = game.endGame();
		    knocker = "player2";
		    if (winner.equals("player2")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}

	    }
	    //System.out.println("Player1 deadWood: " + player1.hand.deadWood());
	    //System.out.println("Player2 deadWood: " + player2.hand.deadWood());

	    /*System.out.println("Player1 final hand: " + game.h1);
	      System.out.println("Player2 final hand: " + game.h2);

	      System.out.println("Player1 check: " + game.h1.deadWood());
	      System.out.println("Player2 check: " + game.h2.deadWood());

	      System.out.println("Knocker: " + knocker);
	      System.out.println("Winner: " + winner);*/


	    //Assumes I am player 2
	    /*
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
	    System.out.println("wins: " + winCount);
	    System.out.println("draws: " + drawCount);
	}
    }
}
