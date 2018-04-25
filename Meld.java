/******************************************************************************
A "meld" can be a set (three  cards of the same rank), or a run 
(three cards of consecutive rank, and of the same suit)
******************************************************************************/
import java.util.*;

public class Meld implements Comparable<Meld> {
	public Card[] cards;
	public boolean set;
	public boolean run;

	public Meld(Card a, Card b, Card c) {
		// only allow valid melds
		if (!isMeld(a, b, c)) {
			cards = null;
			set = false;
			run = false;
		}
		cards = new Card[3];
		cards[0] = a;
		cards[1] = b;
		cards[2] = c;

		if (a.rank == b.rank && b.rank == c.rank) set = true;
		else run = true;
	}

	public String toString() {
		String s = "";
		for (Card card : cards) {
			s += card.toString();
			s += "\n";
		}
		return s;
	}


	// confirm that three cards form a valid meld
	public static boolean isMeld(Card a, Card b, Card c) {
		// the three cards form a set
		if (a.rank == b.rank && b.rank == c.rank) return true;

		// if the three cards do not form a set, they must be the same suit
		// in order to form a run
		if (a.suit != b.suit || a.suit != c.suit || b.suit != c.suit)
			return false;

		// check possible ascending runs
		if (a.rank == b.rank - 1 && b.rank == c.rank - 1) return true;
		if (a.rank == c.rank - 1 && c.rank == b.rank - 1) return true;
		if (b.rank == c.rank - 1 && c.rank == a.rank - 1) return true;
		if (b.rank == a.rank - 1 && a.rank == c.rank - 1) return true;
		if (c.rank == b.rank - 1 && b.rank == a.rank - 1) return true;
		if (c.rank == a.rank - 1 && a.rank == b.rank - 1) return true;

		return false;

	}

	// check if a meld contains a card
	public boolean containsCard(Card c) {
		for (Card card : cards) {
			if (c.suit == card.suit && c.rank == card.suit)
				return true;
		}
		return false;
	}

	// check if, in a group of multiple melds, one card is used multiple
	// times
	public static ArrayList<Card> intersection(ArrayList<Meld> melds) {
		ArrayList<Card> intersections = new ArrayList<Card>();

		// check every combination of two melds
		for (int i = 0; i < melds.size(); i++) {
			for (int j = i; j < melds.size(); j++) {
				if (i != j) {
					Meld m1 = melds.get(i);
					Meld m2 = melds.get(j);
					for (int k = 0; k < 3; k++) {
						Card c1 = m1.cards[k];
						for (int l = 0; l < 3; l++) {
							Card c2 = m2.cards[l];

							// if the card is in both melds, add it to the list
							if (c1.equals(c2)) intersections.add(c1);
						}
					}
				}
			}
		}
		return intersections;
	}

	// return the number of points that a meld is worth
	public int points() {
		return cards[0].points() + cards[1].points() + cards[2].points();
	}

	// compareTo ranks Melds by how many points they are worth, so that melds
	// worth more points should be ranked higher 
	public int compareTo(Meld that) {
		return that.points() - this.points();
	}

}