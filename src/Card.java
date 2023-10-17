import java.io.Serializable;

public class Card implements Serializable{
	Shape shape;
	Value value;

	Card(Shape shape, Value value){
		this.shape = shape;
		this.value = value;
	}

	Card(String sv){
		String s = sv.substring(0,1);
		String v = sv.substring(1,sv.length());

		switch(s){
		case "¢À":
			this.shape = Shape.CLOVER;
			break;
		case "¢¾":
			this.shape = Shape.HEART;
			break;
		case "¡ß":
			this.shape = Shape.DIAMOND;
			break;
		case "¢¼":
			this.shape = Shape.SPADE;
			break;
		default:
			break;
		}
		
		switch(v) {
		case "A":
			this.value = Value.ACE;
			break;
		case "2":
			this.value = Value.TWO;
			break;
		case "3":
			this.value = Value.THREE;
			break;
		case "4":
			this.value = Value.FOUR;
			break;
		case "5":
			this.value = Value.FIVE;
			break;
		case "6":
			this.value = Value.SIX;
			break;
		case "7":
			this.value = Value.SEVEN;
			break;
		case "8":
			this.value = Value.EIGHT;
			break;
		case "9":
			this.value = Value.NINE;
			break;
		case "10":
			this.value = Value.TEN;
			break;
		case "J":
			this.value = Value.JACK;
			break;
		case "Q":
			this.value = Value.QUEEN;
			break;
		case "K":
			this.value = Value.KING;
			break;
		default:
			break;
		}
	}

	public Shape getShape() {
		return shape;
	}

	public Value getValue() {
		return value;
	}

	public boolean ShapeEquals(Card card) {
		if(this.shape != card.getShape()) {
			return false;
		}
		return true;
	}

	public boolean ValueEquals(Card card) {
		if(this.value != card.getValue()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return shape.getName()+value.getName();
	}
}
