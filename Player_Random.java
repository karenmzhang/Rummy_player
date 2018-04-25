/******************************************************************************
A player who randomly draws and discards
******************************************************************************/
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Player_Random {
	public Hand hand;
	private Card lastDrawn;
	private boolean lastDrawnFromDiscard;

	public Player_Random(Hand h) {
		hand = h;
		lastDrawn = null;
		lastDrawnFromDiscard = false;
	}

	// decide which card in the hand to discard
	public Card discard(){
		hand.shuffle(); // consider optimizing to not need a shuffle (choose
						// a random index instead)

		// cannot discard a card you just drew from the discard pile
		if (lastDrawnFromDiscard) {
			if (hand.allCards.get(0).equals(lastDrawn)){
				Card c = hand.allCards.get(1);
				hand.discard(c);
				return c;
			}
		}

		Card c = hand.allCards.get(0);
		hand.discard(c);
		return hand.allCards.get(0);
	}

	// choose to make a move. Valid choices are: draw from discard pile or
	// draw from top of deck.
	public String makeMove() {
		Random rand = ThreadLocalRandom.current();
		Double r = rand.nextDouble();
		if (r < 0.5) {
			lastDrawnFromDiscard = false;
			return "deck";
		}
		else {
			lastDrawnFromDiscard = true;
			return "discardPile";
		}
	}

	// see if this player could knock right now
	public boolean knock() {
		if (hand.deadWood() <= 10) return true;
		else return false;
	}

	// draw a new card into the deck
	public void draw(Card c) {
		lastDrawn = c;
		hand.draw(c);
	}

	public String toString() {
		String s = "";
		Collections.sort(hand.allCards);
		Collections.sort(hand.allCards, Card.bySuitOrder());
		s += hand.toString();
		if (lastDrawn != null) s+= ("Last Drawn: " + lastDrawn.toString() + "\n");
		s+= lastDrawnFromDiscard;

		return s;
	}

	public static void main(String[] args) {
		Hand h = new Hand();
		Deck d = new Deck();
		d.shuffle();
		for (int i = 0; i < 10; i++) {
			h.draw(d.removeTopCard());
		}
		Player_Random p = new Player_Random(h);
		System.out.println(h);
		for (int i = 0; i < 10; i++) {
			System.out.println(p.makeMove());
			Card c = p.discard();
			System.out.println(c);
			p.hand.discard(c);
			c = d.removeTopCard();
			p.hand.draw(c);
			System.out.println(c);
			System.out.println();
		}
		System.out.println();
		System.out.println(p.hand);
	}
}