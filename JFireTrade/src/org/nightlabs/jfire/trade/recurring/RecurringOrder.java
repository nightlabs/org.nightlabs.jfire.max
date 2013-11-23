package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;


/**
 *
 * A {@link RecurringOrder} is an extension of {@link Order} that manages {@link RecurringOffer}s.
 *
 * @author Fitas Amine <fitas@nightlabs.de>
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Order"
 *		detachable="true"
 *		table="JFireTrade_RecurringOrder"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_RecurringOrder")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RecurringOrder
extends Order
implements RecurringArticleContainer
{

	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurringOrder() { }

	public RecurringOrder(LegalEntity vendor, LegalEntity customer,
			String orderIDPrefix, long orderID, Currency currency, User user) {
		super(vendor, customer, orderIDPrefix, orderID, currency, user);
	}

}