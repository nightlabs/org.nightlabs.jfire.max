package org.nightlabs.jfire.transfer;

public class Tuple<E1, E2> {
	private E1 elem1;
	private E2 elem2;
	
	public Tuple(E1 elem1, E2 elem2) {
		super();
		this.elem1 = elem1;
		this.elem2 = elem2;
	}
	
	public E1 getElement1() {
		return elem1;
	}
	
	public E2 getElement2() {
		return elem2;
	}
}
