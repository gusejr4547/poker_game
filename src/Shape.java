
public enum Shape {
	CLOVER("��"), HEART("��"), DIAMOND("��"), SPADE("��");
	
	String name;
	
	Shape(String shape){
		this.name = shape;
	}
	
	String getName() {
		return name;
	}
}
