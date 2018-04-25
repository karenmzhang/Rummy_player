/******************************************************************************
A respresentation of a standard 52-card deck (no jokers).
Ranks start from 1 (ace) and go to 13 (king)
Suits are encoded as follows:
	1: Spades
	2: Hearts
	3: Diamonds
	4: Clubs
The deck can be shuffled, and cards can be removed or added.
******************************************************************************/

import java.util.*;

public class Deck {

	public ArrayList<Card> cards;

	public Deck() {
		cards = new ArrayList<Card>();
		for (int rank = 1; rank <= 13; rank++) {
			for (int suit = 1; suit <= 4; suit++) {
				Card c = new Card(rank, suit);
				cards.add(c);
			}
		}
	}

	// create a deck with a specified number of cards
	public Deck(int numCards) {
		cards = new ArrayList<Card>();

		// create a full deck and shuffle it
		ArrayList<Card> fullDeck = new ArrayList<Card>();
		for (int rank = 1; rank <= 13; rank++) {
			for (int suit = 1; suit <= 4; suit++) {
				Card c = new Card(rank, suit);
				fullDeck.add(c);
			}
		}
		Collections.shuffle(fullDeck);

		// add numCards number of cards to the deck
		int count = numCards;
		while (count > 0) {
			cards.add(fullDeck.remove(0));
		}
	}

	// create a deep copy of a deck
	public Deck(Deck deckThat) {
		this.cards = new ArrayList<Card>();
		for (Card c : deckThat.cards) {
			Card cNew = new Card(c.rank, c.suit);
			this.cards.add(cNew);
		}
	}

	public int size() {
		return cards.size();
	}

	// remove a specified card, return false if the card was not in the deck
	public boolean removeSpecificCard(Card c) {
		Card cInDeck = new Card(-1,-1);
		for (Card card : cards) {
			if (card.equals(c)) {
				cInDeck = card;
			}
		}
		return cards.remove(cInDeck);
	}

	// add a specified card, return false if the card was already in the deck
	public boolean addSpecificCard(Card c) {
		boolean b = true;
		for (Card card : cards) {
			if (card.equals(c))
				b = false;
		}
		if (b) cards.add(c);
		return b;
	}

	// shuffle the deck
	public void shuffle() {
		Collections.shuffle(cards);
	}

	// remove the top card and return it
	public Card removeTopCard() {
		return cards.remove(0);
	}

	// remove the bottom card and return it
	public Card removeBottomCard() {
		return cards.remove(cards.size() - 1);
	}

	// return bottom card without removing it
	public Card peekBottomCard() {
		return cards.get(cards.size() - 1);
	}

	// return top card without removing it
	public Card peekTopCard() {
		return cards.get(0);
	}

	public String toString() {
		String s = "";
		for (Card c : cards) {
			s += c.toString();
			s += "\n";
		}
		return s;
	}

	public static void main(String[] args) {
		Deck d = new Deck();
		//System.out.println(d.peekTopCard());
		d.shuffle();
		System.out.println(d.peekTopCard());

		Collections.sort(d.cards, Card.bySuitOrder());
		Collections.sort(d.cards);
		
		for (Card c: d.cards) {
			System.out.println(c);
		}
		//System.out.println(d.removeTopCard());
		//Card c = d.peekTopCard();
		//System.out.println(c);
		//Card copy = new Card(c.rank, c.suit);
		//System.out.println(d.removeSpecificCard(copy));
		/*Card c = d.removeTopCard();
		System.out.println(c);
		System.out.println(d.removeSpecificCard(c));
		System.out.println(d.addSpecificCard(c));
		Card e = d.peekTopCard();
		System.out.println(e);
		System.out.println(d.addSpecificCard(e));*/

	}
}