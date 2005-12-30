/*
 * Created on Oct 21, 2005
 */
package org.nightlabs.jfire.store.book;

import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * The <code>Storekeeper</code> does a similar job in the store as the
 * {@link org.nightlabs.jfire.accounting.book.Accountant} does in the
 * accounting. This means, it is responsible for local transfers between
 * {@link org.nightlabs.jfire.store.Repository}s when a
 * {@link org.nightlabs.jfire.store.deliver.Delivery} is done.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.book.id.StorekeeperID"
 *		detachable="true"
 *		table="JFireTrade_Storekeeper"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, storekeeperID"
 */
public abstract class Storekeeper
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
	private String storekeeperID;

	/**
	 * @deprecated Only for JDO!
	 */
	protected Storekeeper() { }

	public Storekeeper(String organisationID, String storekeeperID)
	{
		this.organisationID = organisationID;
		this.storekeeperID = storekeeperID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getStorekeeperID()
	{
		return storekeeperID;
	}

	public abstract void bookTransfer(User user, LegalEntity mandator, ProductTransfer transfer, Map involvedAnchors);

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Storekeeper has no PersistenceManager assigned!");
		return pm;
	}
}
