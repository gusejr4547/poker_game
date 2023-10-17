import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DeckPower implements Serializable {
	enum Power {
		NO_PAIR("노페어"), ONE_PAIR("원페어"), TWO_PAIR("투페어"), THREEPLE("트리플"), STRAIGHT("스트레이트"), BACK_STRAIGHT("백 스트레이트"),
		MOUNTAIN("마운틴"), FLUSH("플러시"), FULL_HOUSE("풀하우스"), FOUR_CARD("포카드"), STRAIGHT_FLUSH("스트레이트 플러시"), 
		BACK_STRAIGHT_FLUSH("백 스트레이트 플러시"), ROYAL_STRAIGHT_FLUSH("로얄 스트레이트 플러시");

		String name;

		Power(String name) {
			this.name = name;
		}

		String getName() {
			return name;
		}
	}

	Power power = Power.NO_PAIR;
	List<Card> pair_card;
	int[] pair_check;
	Card best_card;
	Card second_card;
	List<Card> mycard;

	DeckPower(List<Card> mycard){
		this.mycard = new ArrayList<Card>();
		this.mycard.addAll(mycard);
		pair_card = new ArrayList<Card>();
		//best_card = new Card();
		Cal();
	}
	
	//string type으로 받아도 변환가능
	DeckPower(String mycard_string){
		String tmp = mycard_string.substring(mycard_string.indexOf("[")+1, mycard_string.indexOf("]")).replace(" ", "");
		List<String> tmp_list = Arrays.asList(tmp.split(","));
		
		List<Card> card_list = new ArrayList<Card>();
		
		for(int i=0; i<tmp_list.size(); i++) {
			Card card = new Card(tmp_list.get(i));
			card_list.add(card);
		}
		
		this.mycard = card_list;
		pair_card = new ArrayList<Card>();
		Cal();
	}

	private void Cal(){
		pair_check = new int[2];
		int tmp_ind=0;
		for(int i=0; i<5; i++) {
			for(int j=i+1; j<5; j++) {
				if(!mycard.get(i).ValueEquals(mycard.get(j))) {
					if(pair_check[tmp_ind] != 0) {
						pair_card.add(mycard.get(i));
						i += pair_check[tmp_ind];
						tmp_ind++;
					}
					break;
				}
				else {
					pair_card.add(mycard.get(j));
					pair_check[tmp_ind]++;
					if(j == 4) {
						pair_card.add(mycard.get(i));
						i += pair_check[tmp_ind];
						tmp_ind++;
					}
				}
			}
		}
		if(pair_check[0] == 1) {
			power = Power.ONE_PAIR;
			if(pair_check[1] == 1) {
				power = Power.TWO_PAIR;
			}
			else if(pair_check[1] == 2) {
				power = Power.FULL_HOUSE;
			}
		}
		else if(pair_check[0] == 2) {
			power = Power.THREEPLE;
			if(pair_check[1] == 1) {
				power = Power.FULL_HOUSE;
			}
		}
		else if(pair_check[0] == 3) {
			power = Power.FOUR_CARD;
		}
		//----------------------------
		checkFlush();
		checkStraight();
		checkMountain();

		setBestCard();
	}

	void checkFlush() {
		Iterator<Card> it = mycard.iterator();
		Shape previous = null;
		while(it.hasNext()) {
			if(previous == null) {
				previous = it.next().getShape();
			}
			else {
				Shape current = it.next().getShape();
				if(previous.equals(current)) {
					previous = current;
				}
				else {
					return;
				}
			}
		}
		power = Power.FLUSH;
	}

	void checkStraight() {
		Iterator<Card> it = mycard.iterator();
		int previous = -99;
		boolean back_straight = false;
		while(it.hasNext()) {
			if(previous == -99) {
				previous = it.next().getValue().ordinal();
				if(previous == 12) {
					previous = -1;
					back_straight = true;
				}
			}
			else {
				int current = it.next().getValue().ordinal();
				if(previous+1 == current) {
					previous = current;
				}
				else {
					back_straight = false;
					return;
				}
			}
		}
		if(back_straight) {
			if(power.equals(Power.FLUSH)) {
				power = Power.BACK_STRAIGHT_FLUSH;
			}
			else {
				if(power.ordinal() < Power.BACK_STRAIGHT.ordinal()) {
					power = Power.BACK_STRAIGHT;
				}
			}

		}
		else {
			if(power == Power.FLUSH) {
				power = Power.STRAIGHT_FLUSH;
			}
			else {
				if(power.ordinal() < Power.STRAIGHT.ordinal()) {
					power = Power.STRAIGHT;
				}
			}
		}

	}

	void checkMountain() {
		ArrayList<Value> mountain = new ArrayList<Value>();
		mountain.add(Value.ACE);
		mountain.add(Value.TEN);
		mountain.add(Value.JACK);
		mountain.add(Value.QUEEN);
		mountain.add(Value.KING);

		ArrayList<Value> my = new ArrayList<Value>();
		Iterator<Card> it = mycard.iterator();
		while(it.hasNext()) {
			my.add(it.next().getValue());
		}

		if(mountain.equals(my)) {
			if(power == Power.FLUSH) {
				power = Power.ROYAL_STRAIGHT_FLUSH;
			}
			else {
				power = Power.MOUNTAIN;
			}
		}
	}

	void setBestCard() {
		if(!pair_card.isEmpty()) {
			Collections.sort(pair_card, new NewComparator());
		}
		Collections.sort(mycard, new NewComparator());

		switch(power) {
		case ONE_PAIR:
			best_card = pair_card.get(1);
			break;
		case TWO_PAIR:
			best_card = pair_card.get(3);
			second_card = pair_card.get(1);
			break;
		case THREEPLE:
			best_card = pair_card.get(2);
			break;
		case FULL_HOUSE:
			int[] tmp = new int[2] ;
			int ind = 0;
			int previous=0;
			for(int i=0; i<5; i++) {
				if(i==0) {
					previous = pair_card.get(i).getValue().ordinal();
					tmp[ind]++;
				}
				else {
					if(previous == pair_card.get(i).getValue().ordinal()) {
						tmp[ind]++;
					}
					else {
						previous  = pair_card.get(i).getValue().ordinal();
						ind++;
						tmp[ind]++;
					}
				}
			}
			if(tmp[0] == 2) {
				best_card = pair_card.get(4);
				second_card = pair_card.get(1);
			}
			else if(tmp[0] == 3) {
				best_card = pair_card.get(2);
				second_card = pair_card.get(4);
			}
			break;
		case FOUR_CARD:
			best_card = pair_card.get(3);
			break;
		case NO_PAIR:
			best_card = mycard.get(4);
		default:
			break;
		}
	}

	/*1 compare 2, if 1 big->true, small->false*/
	boolean compare(DeckPower p) {
		if(power.ordinal() > p.power.ordinal()) {
			return true;
		}
		else if(power.ordinal() == p.power.ordinal()) {
			if(best_card != null) {
				if(best_card.getValue().ordinal() > p.best_card.getValue().ordinal()) {
					return true;
				}
				else if(best_card.getValue().ordinal() == p.best_card.getValue().ordinal()) {
					if(second_card != null) {
						if(second_card.getValue().ordinal() > p.second_card.getValue().ordinal()) {
							return true;
						}
						else if(second_card.getValue().ordinal() == p.second_card.getValue().ordinal()) {
							if(best_card.getShape().ordinal() > p.best_card.getShape().ordinal()) {
								return true;
							}
							else if(best_card.getShape().ordinal() == p.best_card.getShape().ordinal()) {
								if(second_card.getShape().ordinal() > p.second_card.getShape().ordinal()) {
									return true;
								}
							}
						}
					}
				}
			}
			else {
				int i=4;
				while(i >= 0) {
					if(mycard.get(i).getValue().ordinal() > p.mycard.get(i).getValue().ordinal()) {
						return true;
					}
					else if(mycard.get(i).getValue().ordinal() == p.mycard.get(i).getValue().ordinal()) {
						i--;
					}
					else {
						break;
					}
				}
				if(i == -1) {
					i = 4;
					while(i>=0) {
						if(mycard.get(i).getShape().ordinal() > p.mycard.get(i).getShape().ordinal()) {
							return true;
						}
						else if(mycard.get(i).getShape().ordinal() == p.mycard.get(i).getShape().ordinal()) {
							i--;
						}
						else {
							break;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		if(power.equals(Power.NO_PAIR)){
			return best_card.getValue().getName() + " 탑";
		}
		if(best_card != null) {
			if(second_card != null) {
				return best_card.getValue().getName()+","+second_card.getValue().getName()+" "+power.getName();
			}
			return best_card.getValue().getName()+" "+power.getName();
		}
		return power.getName();
	}
}

class NewComparator implements Comparator<Card>{
	@Override
	public int compare(Card c1, Card c2) {
		// TODO Auto-generated method stub
		if(c1.getValue().ordinal() > c2.getValue().ordinal()) {
			return 1;
		}
		else if(c1.getValue().ordinal() == c2.getValue().ordinal()) {
			if(c1.getShape().ordinal() > c2.getShape().ordinal()) {
				return 1;
			}
		}
		return -1;
	}
}

