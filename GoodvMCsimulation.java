/******************************************************************************
Monte Carlo simulations of Rummy playouts - no tree structure
******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GoodvMCsimulation {
    

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
	    Player_Good player1 = new Player_Good(game.h1);
	    int count = 0; // number of rounds

	    MCsimulation mcs = new MCsimulation(game.h2, game.discardPile);

	    while (true) {
		// make Player 1's move                                                                  
		player1.setTopOfDiscard(game.discardPile.peekBottomCard());
		s1 = player1.makeMove();

		if (s1.equals("deck")) {
		    player1.draw(game.deck.removeTopCard());
		}
		if (s1.equals("discardPile")) {
		    player1.draw(game.discardPile.removeBottomCard());
		}

		// make Player 1's discard                                                               
		discard = player1.discard();
		game.discardPile.addSpecificCard(discard);

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
		    



		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;
		while (elapsedTime < 0.5*100) {
		    mcs.simulate();
		    elapsedTime = System.currentTimeMillis() - startTime;
		}

		if (mcs.dpScore > mcs.dScore) {
		    mcs.myHand.draw(game.discardPile.removeBottomCard());
		    s2 = "discardPile";
		}
		else {
		    mcs.myHand.draw(game.deck.removeTopCard());
		    s2 = "deck";
		    
		}
		//System.out.println("Player2 move: " + s2);

		mcs.updateKnownDeck();
		mcs.makeDiscardChoices();
		// make Player 2's discard
		startTime = System.currentTimeMillis();
		elapsedTime = 0L;
		while (elapsedTime < 0.5*100) {
		    mcs.discard();
		    elapsedTime = System.currentTimeMillis() - startTime;
		}

		discard = mcs.chooseDiscard();
		mcs.myHand.discard(discard);
		game.discardPile.addSpecificCard(discard);

		game.h2 = mcs.myHand;

		// check to see if the game should end
		if (game.deck.size() <= 2) { 
		    winner = game.endGame();
		    knocker = "Deck";
		    if (winner.equals("player2")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}
		if (mcs.knock()) {
		    winner = game.endGame();
		    knocker = "player2";
		    if (winner.equals("player2")) winCount++;
		    if (winner.equals("Draw")) drawCount++;
		    break;
		}
	    }



	    System.out.println("wins: " + winCount);
	    System.out.println("draws: " + drawCount);
	}
    }
}
