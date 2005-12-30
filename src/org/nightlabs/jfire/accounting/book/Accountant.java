 /*
 * Created on 27.10.2004
 */
package org.nightlabs.jfire.accounting.book;

import java.io.Serializable;
import java.util.Map;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * An Accountant is responsible for splitting money into several accounts and for
 * vetoing if a money transfer cannot be done. One Accountant can be responsible for an
 * undefinite number of Account-s or for only one. The main job of the Accountant is
 * to split amounts e.g. into certain taxes.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.book.id.AccountantID"
 *		detachable="true"
 *		table="JFireTrade_Accountant"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, accountantID"
 */
public abstract class Accountant implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String accountantID;

	/**
	 * @deprecated Do not use! Only for JDO!
	 */
	protected Accountant() { }

	public Accountant(String organisationID, String accountantID)
	{
		this.organisationID = organisationID;
		this.accountantID = accountantID;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}	
	/**
	 * @return Returns the accountantID.
	 */
	public String getAccountantID()
	{
		return accountantID;
	}
	public abstract void bookTransfer(User user, LegalEntity mandator, MoneyTransfer transfer, Map involvedAnchors);
}
