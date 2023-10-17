
public enum Value{
	TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"),
	EIGHT("8"), NINE("9"), TEN("10"), JACK("J"), QUEEN("Q"), KING("K"), ACE("A");
	
	String name;
	
	Value(String name){
		this.name = name;
	}
	
	String getName() {
		return name;
	}
}