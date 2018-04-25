/******************************************************************************
A player who plays according to some good 
******************************************************************************/
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Player_Good {
	public Hand hand;
	private Card lastDrawn;
	private boolean lastDrawnFromDiscard;
	private ArrayList<Card> potentialMelds;
	private ArrayList<Card> desired;
	private Card topOfDiscard;

	public Player_Good(Hand h) {
		hand = h;
		lastDrawn = null;
		lastDrawnFromDiscard = false;
		potentialMelds = new ArrayList<Card>();
		desired = new ArrayList<Card>();
		topOfDiscard = null;
	}

	// find what potential melds could be made with your hand
	public void findPotentialMelds() {
		potentialMelds = new ArrayList<Card>();

		ArrayList<Meld> melds = hand.findMelds();
		for (Meld meld : melds) {
			if (meld.cards[0].isValid()) potentialMelds.add(meld.cards[0]);
			if (meld.cards[1].isValid()) potentialMelds.add(meld.cards[1]);
			if (meld.cards[2].isValid()) potentialMelds.add(meld.cards[2]);
		}
	}

	// find what cards would be desired
	public void findDesired() {
		desired = new ArrayList<Card>();

		// sort in rank order to find sets
		Collections.sort(hand.allCards); 
		for (int i = 0; i < hand.allCards.size() - 1; i++) {
			// if found a double
			if (hand.allCards.get(i).rank == hand.allCards.get(i+1).rank) {
				// just add all the cards of this rank 
				for (int j = 1; j < 5; j++) {
					Card c = new Card(hand.allCards.get(i).rank, j);
					if (c.isValid()) desired.add(c);
				}
			}
		}

		// sort in suit order to find runs		
		Collections.sort(hand.allCards, Card.bySuitOrder());
		for (int i = 0; i < hand.allCards.size() - 1; i++) {
			// if found two cards of the same suit
			if (hand.allCards.get(i).suit == hand.allCards.get(i+1).suit) {

				// if the two cards are consecutive
				if (hand.allCards.get(i).rank == hand.allCards.get(i+1).rank - 1) {
					Card c1 = new Card(hand.allCards.get(i).rank - 1, hand.allCards.get(i).suit);
					if (c1.isValid()) desired.add(c1);
					Card c2 = new Card(hand.allCards.get(i+1).rank + 1, hand.allCards.get(i).suit);
					if (c2.isValid()) desired.add(c2);
				}
			}
		}
	}

	public boolean listContainsCard(Card c) {
		for (Card card : potentialMelds) {
			if (card.equals(c))
				return true;
		}
		return false;
	}

	public boolean desiredContainsCard(Card c) {
		for (Card card : desired) {
			if (card.equals(c))
				return true;
		}
		return false;
	}

	// decide which card in the hand to discard
	public Card discard(){
		this.findPotentialMelds();
		Collections.sort(hand.allCards); 
		Card c = hand.allCards.get(hand.allCards.size() - 1);

		// want to discard higher rank, unused cards
		for (int i = hand.allCards.size() - 1; i >= 0; i--) {
			// if the card is not something you want to keep
			if (!this.listContainsCard(hand.allCards.get(i))) {
				c = hand.allCards.get(i);
				if (c.isValid()) {
					hand.discard(c);
					return c;
				}
			}

		}

		// if failed to find a good card, discard the highest one
		/*if (!c.isValid()) {
			hand.shuffle();

			if (lastDrawnFromDiscard) {
				if (hand.allCards.get(0).equals(lastDrawn))
					c = hand.allCards.get(1);
			}

			else c = hand.allCards.get(0);
		}*/

		hand.discard(c);
		return c;
	}

	// keep track of what is on the top of the discard pile
	public void setTopOfDiscard(Card c) {
		topOfDiscard = c;
	}

	// choose to make a move. Valid choices are: draw from discard pile or
	// draw from top of deck 
	public String makeMove() {
		this.findDesired();

		// if top of discard is something you desire, take it
		if (topOfDiscard != null) {
			if (this.desiredContainsCard(topOfDiscard)) {
				lastDrawnFromDiscard = true;
				return "discardPile";
			}
		}

		else {
			lastDrawnFromDiscard = false;
		}

		return "deck";
	}

	// see if this player could knock right now
	public boolean knock() {
		if (hand.deadWood() <= 10) return true;
		else return false;
	}

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
		Card[] cards = new Card[10];
		cards[0] = new Card(2, 1);
		cards[1] = new Card(3, 1);
		cards[2] = new Card(1, 2);
		cards[3] = new Card(2,2);
		cards[4] = new Card(3,2);
		cards[5] = new Card(6,2);
		cards[6] = new Card(4,3);
		cards[7] = new Card(6,3);
		cards[8] = new Card(9,3);
		cards[9] = new Card(1,4);
		for (int i = 0; i < 10; i++) {
			h.draw(cards[i]);
		}
		Player_Good p = new Player_Good(h);
		System.out.println(h);
		System.out.println(p.makeMove());
		Card c = p.discard();
		System.out.println(c);

		System.out.println(p.potentialMelds);
		System.out.println(p.desired);
		
	}
}