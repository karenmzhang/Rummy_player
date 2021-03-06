/******************************************************************************
Monte Carlo Tree Search using UCT 
******************************************************************************/

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MctsTimeTest {

    public static void main(String[] args) {
	
	int winCount = 0;
	int drawCount = 0;
	    
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

	    Mcts mc = new Mcts(game.h2, s1, game.discardPile);

	    long startTime = System.currentTimeMillis();
	    long elapsedTime = 0L;
	    int times = 0;
	    while (elapsedTime < 100*1000) {
		mc.search();
		elapsedTime = System.currentTimeMillis() - startTime;
		times++;
	    }
	    System.out.println(times);
	    
	    

	}

    }
