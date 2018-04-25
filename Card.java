/******************************************************************************
A respresentation of a playing card in a standard 52-card deck (no jokers).
Ranks start from 1 (ace) and go to 13 (king)
Suits are encoded as follows:
	1: Spades
	2: Hearts
	3: Diamonds
	4: Clubs
A card with invalid suit or rank will be set to suit -1, rank = -1.
Cards can be compared and sorted with respect to suit or rank.
******************************************************************************/
import java.util.Comparator;

public class Card implements Comparable<Card> {

	public int rank;
	public int suit;

	public Card(int rank, int suit) {
		// invalid inputs lead to the creation of a card with suit = rank = -1
		if (rank < 1 || rank > 13 || suit < 1 || suit > 4) {
			this.rank = -1;
			this.suit = -1;
		}

		else {
			this.rank = rank;
			this.suit = suit;
		}
	}

	public boolean isValid() {
		if (rank < 1 || rank > 13 || suit < 1 || suit > 4)
			return false;
		else return true;
	}

	public String toString() {
		String s = "";

		// invalid card
		if (rank < 0) {
			s += "Invalid Card";
			s += rank;
			s += " ";
			s += suit;
			return s;
		}

		// determine suit
		if (rank == 1) s += "Ace";
		else if (rank == 11) s += "Jack";
		else if (rank == 12) s += "Queen";
		else if (rank == 13) s += "King";
		else s+= rank;

		s += " of ";

		// determine rank
		if (suit == 1) s += "Spades";
		if (suit == 2) s += "Hearts";
		if (suit == 3) s += "Diamonds";
		if (suit == 4) s += "Clubs";

		return s;
	}

	// check if two cards are the same
	public boolean equals(Card that) {
		if (this.rank == that.rank && this.suit == that.suit) return true;
		else return false;
	}

	public int points() {
		if (rank < 0) return 0;
		if (rank < 11) {
			return rank;
		}
		else return 10;
	}

	// comparator for sorting by suit
	public static Comparator<Card> bySuitOrder() {
		return new suitOrder();
	}

	private static class suitOrder implements Comparator<Card> {
		public int compare(Card c1, Card c2) {
			return c1.suit - c2.suit;
		}
	}

	// default sorting is by rank
	public int compareTo(Card that) {
		return this.rank - that.rank;
	}

	public static void main(String[] args) {

	}
}