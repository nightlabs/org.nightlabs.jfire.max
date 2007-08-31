package org.nightlabs.jfire.transfer;

import java.util.EnumSet;

/**
 * The different stages of the transfer process.
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public enum Stage {
	Initial, ClientBegin, ServerBegin, ClientDoWork, ServerDoWork, ClientEnd, ServerEnd;
	
	public EnumSet<Stage> after(boolean inclusive) {
		EnumSet<Stage> after = EnumSet.noneOf(Stage.class);
		boolean add = false;
		for (Stage stage : values()) {
			if (add)
				after.add(stage);
			
			if (stage == this) {
				add = true;
				
				if (inclusive)
					after.add(stage);
			}
		}
		after.remove(Stage.Initial);
		
		return after;
	}
	
	public EnumSet<Stage> before(boolean inclusive) {
		EnumSet<Stage> before = EnumSet.noneOf(Stage.class);
		for (Stage stage : values()) {
			if (stage == this) {
				if (inclusive)
					before.add(stage);
				
				break;
			}
			
			before.add(stage);
		}
		before.remove(Stage.Initial);
		
		return before;
	}
	
	/**
	 * Returns a boolean indicating whether this stage takes place later than the given stage. 
	 * @param stage The Stage in question.
	 * @return A boolean indicating whether this stage takes place later than the given stage.
	 */
	public boolean isAfter(Stage stage) {
		return stage.ordinal() < this.ordinal();
	}
	
	/**
	 * Returns a boolean indicating whether this stage takes place earlier than the given stage. 
	 * @param stage The Stage in question.
	 * @return A boolean indicating whether this stage takes place earlier than the given stage.
	 */
	public boolean isBefore(Stage stage) {
		return stage.ordinal() > this.ordinal();
	}
}