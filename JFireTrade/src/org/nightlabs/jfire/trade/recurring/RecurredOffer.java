package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;


/**
 * {@link RecurredOffer}s are created by a {@link Task} and will be built 
 * from a template {@link RecurringOffer}. The {@link RecurringOffer} that
 * served as template for the creation of a {@link RecurredOffer} is linked
 * to it see {@link #getRecurringOffer()}.
 * 
 * @author Fitas Amine <fitas@nightlabs.de>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Offer"
 *		detachable="true"
 *		table="JFireTrade_RecurredOffer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.implements name="org.nightlabs.jfire.trade.ArticleContainer"
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.Statable"
 * 
 * @jdo.fetch-group name="RecurringOffer.recurringOffer" fields="recurringOffer"
 * 
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_RecurredOffer")
@FetchGroups(
	@FetchGroup(
		name="RecurringOffer.recurringOffer",
		members=@Persistent(name="recurringOffer"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RecurredOffer extends Offer {

	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private RecurringOffer recurringOffer;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RecurredOffer() {}

	/**
	 * Create a new {@link RecurredOffer}.
	 * 
	 * @param recurringOffer The {@link RecurringOffer} that was the template for this {@link RecurredOffer}.
	 * @param user The user that initiated the creation of this offer.
	 * @param order The {@link Order} the new Offer should be part of.
	 * @param offerIDPrefix The orderIdPrefix primary-key-value.
	 * @param offerID The orderId primary-key-value.
	 */
	public RecurredOffer(RecurringOffer recurringOffer, User user, Order order, String offerIDPrefix, long offerID) {
		super(user, order, offerIDPrefix, offerID);
		this.recurringOffer = recurringOffer;
	}

	/**
	 * Returns the {@link RecurringOffer} that was the template 
	 * for this {@link RecurredOffer}.
	 *   
	 * @return the {@link RecurringOffer} that was the template for this {@link RecurredOffer}.
	 */
	public RecurringOffer getRecurringOffer() {
		return recurringOffer;
	}

}
