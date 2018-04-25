/******************************************************************************
Coordinate a game between two players
******************************************************************************/
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Game {
	public Deck deck;
	public Deck discardPile; //
	public Hand h1; // player 1's hand
	public Hand h2; // player 2's hand

	public Game() {
		deck = new Deck();
		deck.shuffle();
		discardPile = new Deck(0);
		Hand h1 = new Hand();
		Hand h2 = new Hand();
		for (int i = 0; i < 10; i++) {
			h1.draw(deck.removeTopCard());
			h2.draw(deck.removeTopCard());
		}
		this.h1 = h1;
		this.h2 = h2;
	}

	public Game(Deck deck, Hand h1, Hand h2) {
		this.deck = deck;
		this.h1 = h1;
		this.h2 = h2;
		discardPile = new Deck(0);
	}

	public Game(Deck deck, Hand h1, Hand h2, Deck discardPile) {
		this.deck = deck;
		this.h1 = h1;
		this.h2 = h2;
		this.discardPile = discardPile;
	}

	// checks who the winner should be, given that the game has ended
	public String endGame() {
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

	public static void main(String[] args) {
		Game game = new Game();

		Player_Good player1 = new Player_Good(game.h1);
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

			System.out.println("Round: " + count);
			System.out.println("Discard: " + game.discardPile.toString());
			System.out.println("Deck: " + game.deck.toString());

			System.out.println("Player1 hand: ");
			System.out.println(player1);
			System.out.println("Player1 deadWood: " + player1.hand.deadWood());
			System.out.println("Player2 hand: ");
			System.out.println(player2);
			System.out.println("Player2 deadWood: " + player2.hand.deadWood());

			// make Player 1's move
			player1.setTopOfDiscard(game.discardPile.peekBottomCard());
			s1 = player1.makeMove();

			if (s1.equals("deck")) {
				player1.draw(game.deck.removeTopCard());
			}
			if (s1.equals("discardPile")) {
				player1.draw(game.discardPile.removeBottomCard());
			}
			System.out.println("Player1 move: " + s1);

			// make Player 1's discard
			discard = player1.discard();
			game.discardPile.addSpecificCard(discard);
			System.out.println("Player1 Discard: " + discard);

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
			System.out.println("Player2 move: " + s2);

			// make Player 2's discard
			discard = player2.discard();
			game.discardPile.addSpecificCard(discard);
			System.out.println("Player2 Discard: " + discard);
			System.out.print("\n");
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
		//System.out.println("Player1 deadWood: " + player1.hand.deadWood());
		//System.out.println("Player2 deadWood: " + player2.hand.deadWood());

		System.out.println("Player1 final hand: " + game.h1);
		System.out.println("Player2 final hand: " + game.h2);

		System.out.println("Player1 check: " + game.h1.deadWood());
		System.out.println("Player2 check: " + game.h2.deadWood());

		System.out.println("Knocker: " + knocker);
		System.out.println("Winner: " + winner);

	}
}