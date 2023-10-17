
public enum Shape {
	CLOVER("¢À"), HEART("¢¾"), DIAMOND("¡ß"), SPADE("¢¼");
	
	String name;
	
	Shape(String shape){
		this.name = shape;
	}
	
	String getName() {
		return name;
	}
}
