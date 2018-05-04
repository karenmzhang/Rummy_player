/******************************************************************************
Monte Carlo Tree Search using UCT 
******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MctsPlayer2Test {

    public static void main(String[] args) {
	
	int winCount = 0;
	int drawCount = 0;
	for (int z = 0; z < 10000; z++) {
	    
	    String s1; // player 1's last move 
	    String s2; // player 2's last move
	    String winner;
	    String knocker; // who was the one to knock

	    Card discard;
	    Game game = new Game();

	    // add one card to the discard pile
	    game.discardPile.addSpecificCard(game.deck.removeTopCard());
	    int count = 0; // number of rounds

	    Player_Good player1 = new Player_Good(game.h1);

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
		Mcts mc = new Mcts(game.h2, s1, game.discardPile);

		// make Player 2's move
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;
		while (elapsedTime < 0.001*100) {
		    mc.search();
		    elapsedTime = System.currentTimeMillis() - startTime;
		}
		s2 = mc.makeMove();
		if (s2.equals("deck")) {
		    game.h2.draw(game.deck.removeTopCard());
		}
		if (s2.equals("discardPile")) {
		    game.h2.draw(game.discardPile.removeBottomCard());
		}

		discard = mc.makeDiscard(game.h2);
		game.h2.discard(discard);
		game.discardPile.addSpecificCard(discard);
		//System.out.println("Player2 Discard: " + discard);
		//System.out.print("\n");
		count++;

		// check to see if the game should end
		if (game.deck.size() <= 2) { 
		    winner = game.endGame();
		    knocker = "Deck";
		    if (winner.equals("player2")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}
		if (game.h2.deadWood() <= 10) {
		    winner = game.endGame();
		    knocker = "player2";
		    if (winner.equals("player2")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}

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
		    
	    } // end of while loop
	    System.out.println("wins: " + winCount);
	    System.out.println("draws: " + drawCount);
	}
	
	

    }

}
