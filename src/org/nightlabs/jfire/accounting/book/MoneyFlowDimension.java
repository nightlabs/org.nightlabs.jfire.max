/*
 * Created 	on Sep 18, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting.book;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.store.ProductType;

/**
 * Abstract Dimension for MoneyFlowMappings.
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.book.id.MoneyFlowDimensionID"
 *		detachable="true"
 *		table="JFireTrade_MoneyFlowDimension"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *
 * @jdo.query
 *		name="getMoneyFlowDimension"
 *		query="SELECT UNIQUE this
 *			WHERE moneyFlowDimensionID == paramMoneyFlowDimensionID
 *			PARAMETERS String paramMoneyFlowDimensionID
 *			IMPORTS import java.lang.String"
 */
public abstract class MoneyFlowDimension {

	private static final String FETCH_GROUP_GET_MONEY_FLOW_DIMENSION = "getMoneyFlowDimension";
	
	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="200"
	 */
	private String moneyFlowDimensionID;
	
	public MoneyFlowDimension() {
		this.moneyFlowDimensionID = getMoneyFlowDimensionID();
	}
	
	/**
	 * Returns the ID of this MoneyFlowDimension.
	 */
	public abstract String getMoneyFlowDimensionID();

	/**
	 * Returns all possible values this dimension might have.
	 */
	public abstract String[] getValues(ProductType productType);
	
	
	/**
	 * Returns the MoneyFlowDimension with the given organisationID and
	 * moneyFlowDimensionID. 
	 * 
	 * @param pm The PersistenceManager to use.
	 * @param organisationID The organisationID of the dimension.
	 * @param moneyFlowDimensionID The moneyFlowDimensionID of the dimension.
	 * @return The MoneyFlowDimension with for the given keys.  
	 */
	public static MoneyFlowDimension getMoneyFlowDimension(PersistenceManager pm, String moneyFlowDimensionID) {
		Query q = pm.newNamedQuery(MoneyFlowDimension.class, FETCH_GROUP_GET_MONEY_FLOW_DIMENSION);
		return (MoneyFlowDimension)q.execute(moneyFlowDimensionID);
	}
	
}
