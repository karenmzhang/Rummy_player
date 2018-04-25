/******************************************************************************
A representation of a Rummy player's hand. Cards can be drawn into the hand
or discarded from the hand, and melds can be identified. 
******************************************************************************/
import java.util.*;

public class Hand {
	public ArrayList<Card> allCards; // all the cards in the hand


	public Hand() {
		allCards = new ArrayList<Card>();
	}

	public Hand(Hand h) {
		allCards = new ArrayList<Card>();
		for (Card c : h.allCards) {
			Card copy = new Card(c.rank, c.suit);
			allCards.add(copy);
		}
	}

	// add a single card to the hand, if there is room 
	public boolean draw(Card c) {
		if (allCards.size() == 11) return false;
		allCards.add(c);
		return true;
	}

	// discard a single card
	public Card discard(Card c) {
		if (allCards.size() == 0) return null;
		if (!this.contains(c)) return null;

		Card cInDeck = new Card(-1,-1);
		for (Card card : allCards) {
			if (card.equals(c))
				cInDeck = card;
		}
		allCards.remove(cInDeck);
		return cInDeck;
	}

	// check if the hand contains a specific card
	public boolean contains(Card c) {
		for (Card card : allCards) {
			if (card.equals(c))
				return true;
		}
		return false;
	}

	// identify melds inside the hand currently
	public ArrayList<Meld> findMelds() {
		//int currentMeld = 0;
		ArrayList<Meld> melds = new ArrayList<Meld>();

		// sort in rank order to find sets
		Collections.sort(allCards); 
		for (int i = 0; i < allCards.size() - 2; i++) {
			// if found a set
			if (allCards.get(i).rank == allCards.get(i+1).rank && 
				allCards.get(i+1).rank == allCards.get(i + 2).rank) {
				Meld m = new Meld(allCards.get(i), allCards.get(i+1), 
								  allCards.get(i + 2));
				melds.add(m);
			}
		}

		// sort in suit order to find runs		
		Collections.sort(allCards, Card.bySuitOrder());
		for (int i = 0; i < allCards.size() - 2; i++) {
			// if found three cards of the same suit
			if (allCards.get(i).suit == allCards.get(i+1).suit &&
				allCards.get(i+1).suit == allCards.get(i+2).suit) {

				// if the three cards are consecutive
				if (allCards.get(i).rank == allCards.get(i+1).rank - 1 &&
					allCards.get(i+1).rank == allCards.get(i+2).rank - 1) {
					Meld m = new Meld(allCards.get(i), allCards.get(i+1), 
								  allCards.get(i + 2));
					melds.add(m);
				}
			}
		}

		return melds;
	}

	// return the number of points of deadwood in the hand. When there are 
	// melds that use the same card, this automatically chooses to count 
	// the melds that are worth the most number of points and leaves the
	// lower point melds as deadwood.
	public int deadWood() {
		ArrayList<Meld> melds = this.findMelds();
		ArrayList<Card> cardsInMelds = new ArrayList<Card>();

		// sort melds by how many points they are worth to consider 
		// higher-point melds first
		Collections.sort(melds); 
		for (Meld meld : melds) {
			// if none of the cards in this meld are being used yet, add
			// all the cards to cardsInMelds
			if (!cardsInMelds.contains(meld.cards[0]) &&
				!cardsInMelds.contains(meld.cards[1]) &&
				!cardsInMelds.contains(meld.cards[2])) {
				for (int i = 0; i < 3; i++) {
					cardsInMelds.add(meld.cards[i]);
				}
			}
		}

		int totalPoints = 0;
		int meldPoints = 0;

		// the total number of points in the hand
		for (Card card : allCards) {
			totalPoints += card.points();
		}

		// the number of points in the melds
		for (Card card : cardsInMelds) {
			meldPoints += card.points();
		}

		// the point value of the cards that are not in melds
		return totalPoints - meldPoints;
	}

	public String toString() {
		String s = "";
		for (Card card : allCards) {
			s += card.toString();
			s += "\n";
		}
		return s;
	}

	public void shuffle() {
		Collections.shuffle(allCards);
	}

	public static void main(String[] args) {
		Hand hand = new Hand();
		Deck deck = new Deck();
		deck.shuffle();
		Card c1 = new Card(3,1);
		Card c2 = new Card(3,2);
		Card c3 = new Card(3,3);
		Card c4 = new Card(4,3);
		Card c5 = new Card(5,3);
		Card c6 = new Card(10, 2);
		Card c7 = new Card(11, 2);
		Card c8 = new Card(12, 2);
		hand.draw(c1); 
		hand.draw(c2);
		hand.draw(c3); 
		hand.draw(c4);
		hand.draw(c5);
		hand.draw(c6); 
		hand.draw(c7);
		hand.draw(c8);  

		/*for (int i = 0; i < 10; i++) {
			hand.draw(deck.removeTopCard());
		}*/

		//Collections.sort(hand.allCards, Card.bySuitOrder());
		//System.out.println(hand);
		ArrayList<Meld> melds = hand.findMelds();
		//System.out.println(melds);
		ArrayList<Card> intersections = Meld.intersection(melds);
		//System.out.println(intersections);
		Collections.sort(melds);
		System.out.println(melds);

		/*for (Meld meld : melds) {
			System.out.println(meld.points());
		}*/

		System.out.println(hand.deadWood());

	}
}
