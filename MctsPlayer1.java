/******************************************************************************
Monte Carlo Tree Search using UCT 
******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MctsPlayer1 {
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
    public MctsPlayer1(Hand myHand, Deck discardPile) {
	// known information
	root = new Node();
	root.p2 = true;
	root.move = null;
	root.discard = discardPile.peekBottomCard();
	root.discardPile = discardPile;
	root.h1 = myHand;
	
	// create the chance node from the root
	Node chanceNode = new Node();
	chanceNode.chance = true;
	chanceNode.h1 = myHand;

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
	
	// update as if this choice has been played


	// Then, expand the node that you found (should always have 22 options).
	// Choose a random one out of the nodes you just created.
	Node expanded = expand(choice);
	if (expanded.move.equals("deck")) {
	    deck.removeTopCard();
	}
	if (expanded.move.equals("discardPile")) {
	    discardPile.removeBottomCard();
	}
	discardPile.addSpecificCard(expanded.discard);
	/*for (Node blah : choice.children) {
	    System.out.print(blah.printType());
	      }
	      System.out.println(expanded);*/
	// From the chosen node, simulate a (random/heuristic) game.
	int result = simulate(expanded);

	// backpropagate the result.
	backpropagate(result, expanded);
	//System.out.println(choice);
	//System.out.println(expanded);
    }

    public String makeMove() {
	double best = 0;
	Node chanceNode = root.children.get(0);
	Node bestNode = chanceNode.children.get(0);
	for (Node c : chanceNode.children) {
	    if ((double)c.wins/((double)c.visits) > best) {
		best = (double)c.wins/((double)c.visits);
		bestNode = c;
	    }
	}
	return bestNode.move;
    }

    public Card makeDiscard(Hand actualHand) {
	double best = 0;
	Node chanceNode = root.children.get(0);
	Node bestNode = chanceNode.children.get(0);
	for (Node c : chanceNode.children) {
	    if ((double)c.wins/((double)c.visits) > best && actualHand.contains(c.discard)) {
		best = (double)c.wins/((double)c.visits);
		bestNode = c;
	    }
	}
	return bestNode.discard;
    }

    public void determine(Node chanceNode) {
	//System.out.println("determine");
	this.deck = new Deck(chanceNode.possibleDeck);
	if (deck.size() < 10) return;
	deck.shuffle();

	this.myHand = new Hand(chanceNode.h1);
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
	Hand h = new Hand(chanceNode.h1);
	Card c = new Card(topOfDeck.rank, topOfDeck.suit);
	h.draw(c);
	for (int i = 0; i < h.allCards.size(); i++) {
	    Node newNode = new Node();
	    newNode.p1 = true;
	    newNode.move = s;
	    newNode.discard = h.allCards.get(i);
	    newNode.h1 = new Hand(h);
	    newNode.h1.discard(newNode.discard);
	    newNode.h2 = oppHand;
	    if (!chanceNode.childrenContains(newNode)) {
		newNode.parent = chanceNode;
		chanceNode.children.add(newNode);
	    }
	}

	// 11 children for discardPile option
	String s1 = "discardPile";
	Card dp = discardPile.peekBottomCard();
	Hand ha = new Hand(chanceNode.h1);
	Card c1 = new Card(dp.rank, dp.suit);
	ha.draw(c1);
	for (int i = 0; i < ha.allCards.size(); i++) {
	    Node newNode = new Node();
	    newNode.p1 = true;
	    newNode.move = s1;
	    newNode.discard = ha.allCards.get(i);
	    newNode.h1 = new Hand(ha);
	    newNode.h1.discard(newNode.discard);
	    newNode.h2 = oppHand;
	    if (!chanceNode.childrenContains(newNode)) {
		newNode.parent = chanceNode;
		chanceNode.children.add(newNode);
	    }
	}
    }

    public Node choose(Node chanceNode) {
	//System.out.println("choose");
	if (simulations < 10) {
	    int best = maxDeadWood;
	    Node bestNode = chanceNode.children.get(0);
	    for (Node c : chanceNode.children) {
		if (c.h1.deadWood() < best) {
		    best = c.h1.deadWood();
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
		bestNode = current.children.get(0);
		double best = Double.MAX_VALUE;
		for (Node c : current.children) {
		    if (c.uct(simulations) < best) {
			best = c.uct(simulations);
			bestNode = c;
		    }
		}
		current = bestNode;
		//System.outprintln(current.children);
		
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
    }

    public Node expand(Node choice) {
	//System.out.println("expand");
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
		if (!choice.childrenContains(newNode)) {
		    newNode.parent = choice;
		    choice.children.add(newNode);
		}
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
		if (!choice.childrenContains(newNode)) {
		    newNode.parent = choice;
		    choice.children.add(newNode);
		}
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
		if (!choice.childrenContains(newNode)) {
		    newNode.parent = choice;
		    choice.children.add(newNode);
		}
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
		if (!choice.childrenContains(newNode)) {
		    newNode.parent = choice;
		    choice.children.add(newNode);
		}
	    }
	    int r = rand.nextInt(choice.children.size()); 
	    return choice.children.get(r);
	}
    }

    public String endGame(Hand h1, Hand h2) {
	String s;
	if (h1.deadWood() <= 10) {
	    s = "player1";
	}
	else if (h2.deadWood() <= 10) {
	    s = "player2";
	}
	else s = "Draw";
	return s;

    }

    public int simulate(Node expanded) {
	// returns 1 for when I win, 0 for draw or if I lose
	//System.out.println("simulate");
	Deck deckCopy = new Deck(deck);
	Deck dpCopy = new Deck(discardPile);
	Player_Good player1 = new Player_Good(expanded.h1);
	Player_Good player2 = new Player_Good(expanded.h2);

	if (expanded.p2) {
	    player1 = new Player_Good(expanded.h2);
	    player2 = new Player_Good(expanded.h1);
	}

	String s1; // player 1's last move                                                               
        String s2; // player 2's last move                                                               
        String winner;
        String knocker; // who was the one to knock                                                      

        Card discard;

        int count = 0;
        while (true) {
	    count++;

	    if (deckCopy.cards.size()<= 0){
		winner = "Draw";
		break;
	    }

	    if (dpCopy.cards.size() > 0){
		player2.setTopOfDiscard(dpCopy.peekBottomCard());
	    }
            s2 = player2.makeMove();

            if (s2.equals("deck") || dpCopy.cards.size() <= 0) {
		player2.draw(deckCopy.removeTopCard());
            }
            if (s2.equals("discardPile")) {
		player2.draw(dpCopy.removeBottomCard());
            }

            // make Player 2's discard 
            discard = player2.discard();
            dpCopy.addSpecificCard(discard);

            // check to see if the game should end                                                   
            if (deckCopy.size() <= 2) {
		winner = endGame(player1.hand, player2.hand);
		knocker = "Deck";
		break;
            }
            if (player2.knock()) {
		winner = endGame(player1.hand, player2.hand);
		knocker = "player2";
		break;
            }

            player1.setTopOfDiscard(dpCopy.peekBottomCard());
            s1 = player1.makeMove();

            if (s1.equals("deck")) {
		player1.draw(deckCopy.removeTopCard());
            }
            if (s1.equals("discardPile")) {
		player1.draw(dpCopy.removeBottomCard());
            }

            // make Player 2's discard 
            discard = player1.discard();
            dpCopy.addSpecificCard(discard);

            // check to see if the game should end                                                   
            if (deckCopy.size() <= 2) {
		winner = endGame(player1.hand, player2.hand);
		knocker = "Deck";
		break;
            }
            if (player2.knock()) {
		winner = endGame(player1.hand, player2.hand);
		knocker = "player2";
		break;
            }
            if (count > 1000) {
		winner = "Draw";
		break;

	    }
	}

	if (expanded.p1) {
	    if (winner.equals("player1")) return 1;
	    return 0;
	}
	else {
	    if (winner.equals("player2")) return 1;
	    return 0;
	}
    }

    public void backpropagate(int result, Node expanded) {
	Node current = expanded;
	while (!current.chance) {
	    current.visits++;
	    if (result == 1) current.wins++;
	    current = current.parent;
	}
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
	/*int arg = args[0];
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
		  for (int i = 0; i < arg; i++) {
	test.search();
    }*/
	/*System.out.println("my hand: " + game.h2);
	    System.out.println("determined deck: " + test.deck);
	      System.out.println("determined opp hand: " + test.oppHand);
	        System.out.println("actual deck: " + game.deck);
		  System.out.println("actual opp: " + game.h1);
		    System.out.println(test.root.printType());
		      for (Node c : test.root.children) {
		        System.out.println(c.printType());
			  for (Node c1 : c.children) {
			    System.out.print(c1.printType());
			      }
			      }*/

	int winCount = 0;
	int drawCount = 0;
	for (int z = 0; z < 100; z++) {
	    
	    String s1; // player 1's last move 
	    String s2; // player 2's last move
	    String winner;
	    String knocker; // who was the one to knock

	    Card discard;
	    Game game = new Game();

	    // add one card to the discard pile
	    game.discardPile.addSpecificCard(game.deck.removeTopCard());
	    int count = 0; // number of rounds

	    Player_Good player2 = new Player_Good(game.h2);

	    /*System.out.println("Round: " + count);
	      System.out.println("Discard: " + game.discardPile.toString());
	      System.out.println("Deck: " + game.deck.toString());
	      System.out.println("Player1 hand: ");
	      System.out.println(player1);
	      System.out.println("Player1 deadWood: " + player1.hand.deadWood());
	      System.out.println("Player2 hand: ");
	      System.out.println(game.h2);
	      System.out.println("Player2 deadWood: " + game.h2.deadWood());
	    */
	    // make Player 1's move
	    /*player1.setTopOfDiscard(game.discardPile.peekBottomCard());
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

		      game.h1 = player1.hand;*/

	    // essentially acts as player 2

	    /* test stuff 
	          Mcts mc = new Mcts(game.h2, s1, game.discardPile);
		     // make Player 2's move
		        long startTime = System.currentTimeMillis();
			   long elapsedTime = 0L;
			      //for (int i = 0; i <10; i++) {
			         mc.search();
				 elapsedTime = System.currentTimeMillis() - startTime;
				 //}
				 s2 = mc.makeMove(); // test stuff */
	    
	    

	    // take turns between player 1 and 2 until one of then knocks
	    while (true) {
		MctsPlayer1 mc = new MctsPlayer1(game.h1, game.discardPile);

		// make Player 1's move
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;
		int times = 0;
		while (elapsedTime < 1*100) {
		//while (times < 10){
		    mc.search();
		    times++;
		    elapsedTime = System.currentTimeMillis() - startTime;
		}
		s1 = mc.makeMove();
		if (s1.equals("deck")) {
		    game.h1.draw(game.deck.removeTopCard());
		}
		if (s1.equals("discardPile")) {
		    game.h1.draw(game.discardPile.removeBottomCard());
		}

		discard = mc.makeDiscard(game.h1);
		game.h1.discard(discard);
		game.discardPile.addSpecificCard(discard);
		//System.out.println("Player2 Discard: " + discard);
		//System.out.print("\n");
		count++;

		// check to see if the game should end
		if (game.deck.size() <= 2) { 
		    winner = game.endGame();
		    knocker = "Deck";
		    if (winner.equals("player1")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}
		if (game.h1.deadWood() <= 10) {
		    winner = game.endGame();
		    knocker = "player1";
		    if (winner.equals("player1")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}

		// make Player 2's move
		player2.setTopOfDiscard(game.discardPile.peekBottomCard());
		s2 = player2.makeMove();

		if (s2.equals("deck")) {
		    player2.draw(game.deck.removeTopCard());
		}
		if (s2.equals("discardPile")) {
		    player2.draw(game.discardPile.removeBottomCard());
		}
		//System.out.println("Player1 move: " + s1);

		// make Player 1's discard
		discard = player2.discard();
		game.discardPile.addSpecificCard(discard);
		//System.out.println("Player1 Discard: " + discard);

		game.h2 = player2.hand;

		// check to see if the game should end
		if (game.deck.size() <= 2) { 
		    winner = game.endGame();
		    knocker = "Deck";
		    if (winner.equals("player1")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}
		if (player2.knock()) {
		    winner = game.endGame();
		    knocker = "player2";
		    if (winner.equals("player1")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}

		/*    System.out.println("Round: " + count);
		          System.out.println("Discard: " + game.discardPile.toString());
			      System.out.println("Deck: " + game.deck.toString());
			          System.out.println("Player1 hand: ");
				      System.out.println(player1);
				          System.out.println("Player1 deadWood: " + player1.hand.deadWood());
					      System.out.println("Player2 hand: ");
					          System.out.println(game.h2);
						      System.out.println("Player2 deadWood: " + game.h2.deadWood());
		*/
		//	System.out.println(times);
	    } // end of while loop
	
	    System.out.println("wins: " + winCount);
	    System.out.println("draws: " + drawCount);
	
	    }
	// System.out.println("wins: " + winCount);
	//  System.out.println("draws: " + drawCount);
	
	

    }

}
