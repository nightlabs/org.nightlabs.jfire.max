/*
 * Created 	on Sep 20, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting.book;

public interface IMoneyFlowDimension {
	
	/**
	 * Returns the ID of this MoneyFlowDimension.
	 */
	public abstract String getMoneyFlowDimensionID();
	
	/**
	 * Returns all possible values this dimension might have.
	 */
	public abstract String[] getValues();

}
