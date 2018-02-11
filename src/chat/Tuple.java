package chat;

import java.io.Serializable;

public class Tuple<X, Y> implements Serializable{ 

	private static final long serialVersionUID = 3054928314277776970L;
	public X x; 
	public Y y; 

	public Tuple(X x, Y y) { 
		this.x = x; 
		this.y = y; 
	}
	
	public X getX() {
		return x;
	}

	public void setX(X x) {
		this.x = x;
	}

	public Y getY() {
		return y;
	}

	public void setY(Y y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return x + "|" + y;
	}
} 
