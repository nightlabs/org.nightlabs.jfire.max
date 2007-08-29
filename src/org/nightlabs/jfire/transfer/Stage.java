package org.nightlabs.jfire.transfer;

import java.util.EnumSet;

/**
 * The different stages of the transfer process.
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public enum Stage {
	Initial, ClientBegin, ServerBegin, ClientDoWork, ServerDoWork, ClientEnd, ServerEnd;
	
	public EnumSet<Stage> after() {
		EnumSet<Stage> after = EnumSet.noneOf(Stage.class);
		boolean add = false;
		for (Stage stage : values()) {
			if (add)
				after.add(this);
			
			if (stage == this)
				add = true;
		}
		return after;
	}
	
	public EnumSet<Stage> before() {
		EnumSet<Stage> before = EnumSet.noneOf(Stage.class);
		for (Stage stage : values()) {
			if (stage == this)
				break;
			
			before.add(this);
		}
		return before;
	}
}