/******************************************************************************                                  
Coordinate a game between two players                                                                            
******************************************************************************/
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameRandomvGood {

    public static void main(String[] args) {
	/********************************************************************/
	// Player_Good vs Player_Good
                
	int p1Win = 0;
	int p2Win = 0;
	int draw = 0;
	int[] pointDifference = new int[1000];
	// records player1 deadwood = player2 deadwood (negative means player2 did better)

	for (int z = 0; z < 1000; z++) {

	    Game game = new Game();

	    Player_Random player1 = new Player_Random(game.h1);
	    Player_Good player2 = new Player_Good(game.h2);

	    String s1; // player 1's last move                                                               
	    String s2; // player 2's last move                                                               
	    String winner;
	    String knocker; // who was the one to knock                                                      

	    Card discard;

	    // add one card to the discard pile                                                              
	    game.discardPile.addSpecificCard(game.deck.removeTopCard());
	    int count = 0; // number of rounds                                                               

	    // take turns between player 1 and 2 until one of then knocks                                    
	    while (true) {

		// make Player 1's move                                                                  
		//player1.setTopOfDiscard(game.discardPile.peekBottomCard());
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

		// make Player 2's move                                                                  
		player2.setTopOfDiscard(game.discardPile.peekBottomCard());
		s2 = player2.makeMove();

		if (s2.equals("deck")) {
		    player2.draw(game.deck.removeTopCard());
		}
		if (s2.equals("discardPile")) {
		    player2.draw(game.discardPile.removeBottomCard());
		}

		// make Player 2's discard 
		discard = player2.discard();
		game.discardPile.addSpecificCard(discard);
		count++;

		game.h2 = player2.hand;

		// check to see if the game should end                                                   
		if (game.deck.size() <= 2) {
		    winner = game.endGame();
		    knocker = "Deck";
		    break;
		}
		if (player2.knock()) {
		    winner = game.endGame();
		    knocker = "player2";
		    break;
		}

	    }
	    if (winner.equals("Draw"))
		draw++;
	    if (winner.equals("player1"))
		p1Win++;
	    if (winner.equals("player2"))
		p2Win++;
	    pointDifference[z] = game.h1.deadWood() - game.h2.deadWood();
	}

	System.out.println(p1Win);
	System.out.println(draw);
	System.out.println(p2Win);

	int pointCount = 0;
	System.out.println("Points: [");
	for (int y = 0; y < 1000; y++) {
	    //System.out.print(pointDifference[y]);
	    //System.out.print(", ");
	    pointCount += pointDifference[y];
	}
	System.out.println(pointCount);
	System.out.println("]");



    }
}
