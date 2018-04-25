/******************************************************************************
Coordinate a game between two players
******************************************************************************/
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameInteractive {
	public Deck deck;
	public boolean current_player; // player1 = false, player 2 = true
	public Deck discardPile; // 

	public GameInteractive() {
		deck = new Deck();
		deck.shuffle();
		current_player = false;
		discardPile = new Deck(0);
	}

	public void deal(Hand h1, Hand h2) {
		for (int i = 0; i < 10; i++) {
			h1.draw(deck.removeTopCard());
			h2.draw(deck.removeTopCard());
		}
	}

	// checks who the winner should be, given that the game has ended
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

	public static void main(String[] args) {
		GameInteractive game = new GameInteractive();
		Hand h1 = new Hand();
		Hand h2 = new Hand();

		// deal two hands and assign them to the players
		game.deal(h1, h2);
		Player_Good player1 = new Player_Good(h1);
		Player_Good player2 = new Player_Good(h2);

		String s1; // player 1's last move 
		String s2; // player 2's last move
		String winner;
		String knocker;

		Card discard;

		// add one card to the discard pile
		game.discardPile.addSpecificCard(game.deck.removeTopCard());
		int count = 0; // number of rounds

		Scanner sc = new Scanner(System.in);

		// take turns between player 1 and 2 until one of then knocks
		while (true) {

			// INTERACTIVE GAME
			System.out.println("Round: " + count);
			System.out.println("Discard Top: " + game.discardPile.peekBottomCard().toString());

			player1.setTopOfDiscard(game.discardPile.peekBottomCard());
			s1 = player1.makeMove();


			// make Player 1's move
			if (s1.equals("deck")) {
				player1.draw(game.deck.removeTopCard());
			}
			if (s1.equals("discardPile")) {
				player1.draw(game.discardPile.removeBottomCard());
			}

			// make Player 1's discard
			discard = player1.discard();
			game.discardPile.addSpecificCard(discard);
			System.out.println("Player1 Discard: " + discard);
			System.out.println("Player1 move: " + s1);

			if (game.deck.size() <= 2) { 
				winner = game.endGame(player1.hand, player2.hand);
				knocker = "Deck";
				break;
			}
			if (player1.knock()) {
				winner = game.endGame(player1.hand, player2.hand);
				knocker = "player1";
				break;
			}

			System.out.println("Your hand: ");
			System.out.println(player2);
			System.out.println("Your deadWood: " + player2.hand.deadWood());

			player2.setTopOfDiscard(game.discardPile.peekBottomCard());
			System.out.println("Please enter a move: (discardPile, deck, or knock)");
			s2 = sc.next();

			// make Player 2's move
			if (s2.equals("deck")) {
				player2.draw(game.deck.removeTopCard());
			}
			if (s2.equals("discardPile")) {
				player2.draw(game.discardPile.removeBottomCard());
			}

			System.out.println("Current hand: ");
			System.out.println(player2);

			// make Player 2's discard
			System.out.println("Please enter discard card's rank: ");
			int rank = sc.nextInt();
			System.out.println("Please enter discard card's suit: ");
			int suit = sc.nextInt();
			discard = new Card(rank, suit);

			player2.hand.discard(discard);
			game.discardPile.addSpecificCard(discard);
			System.out.println("Player2 Discard: " + discard);
			System.out.print("\n");
			count++;

			System.out.println("Would you like to knock? (y/n)");
			String knock = sc.next();
			if (knock.equals("y")) {
				winner = game.endGame(player1.hand, player2.hand);
				knocker = "player2";
				break;
			}
			if (game.deck.size() <= 2) { 
				winner = game.endGame(player1.hand, player2.hand);
				knocker = "Deck";
				break;
			}
			System.out.println("Your hand now: ");
			System.out.println(player2);
			System.out.println("Your deadWood now: " + player2.hand.deadWood());

		}
		System.out.println("Knocker: " + knocker);
		System.out.println("Winner: " + winner);
		System.out.println("Deck : " + game.deck);
		System.out.println("Discard Pile: " + game.discardPile);
		System.out.println("Player 1's hand: " + player1);
		System.out.println("Player 1's deadwood: " + player1.hand.deadWood());
		System.out.println("Player 2's hand: " + player2);
		System.out.println("Player 2's deadwood: " + player2.hand.deadWood());
		sc.close();

	}
}