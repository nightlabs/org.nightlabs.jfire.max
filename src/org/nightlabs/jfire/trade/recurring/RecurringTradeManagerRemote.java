package org.nightlabs.jfire.trade.recurring;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.version.MalformedVersionException;

public interface RecurringTradeManagerRemote {

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	String ping(String message);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws IOException, MalformedVersionException;

	/**
	 * Creates a new Sale Recurring order. This method is intended to be called by a user (not another
	 * organisation).
	 *
	 * @param customerID An <tt>Order</tt> is defined between a vendor (this <tt>Organisation</tt>) and a customer. This ID defines the customer.
	 * @param currencyID What <tt>Currency</tt> to use for the new <tt>Order</tt>.
	 * @param segmentTypeIDs May be <tt>null</tt>. If it is not <tt>null</tt>, a {@link Segment} will be created for each defined {@link SegmentType}. For each <tt>null</tt> entry within the array, a <tt>Segment</tt> with the {@link SegmentType#DEFAULT_SEGMENT_TYPE_ID} will be created.
	 * @param fetchGroups What fields should be detached.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOrder"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOrder")
	RecurringOrder createSaleRecurringOrder(AnchorID customerID,
			String orderIDPrefix, CurrencyID currencyID,
			SegmentTypeID[] segmentTypeIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Creates a new {@link RecurringOffer} within a given {@link RecurringOrder}.
	 *
	 * @param orderID The orderID defining the Order in which to create a new Offer.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	RecurringOffer createRecurringOffer(OrderID orderID, String offerIDPrefix,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * Stores the given {@link RecurringOfferConfiguration}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.editOffer"
	 * @ejb.transaction type="Required"
	 **/
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.trade.editOffer")
	RecurringOfferConfiguration storeRecurringOfferConfiguration(
			RecurringOfferConfiguration configuration, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOrders"
	 * @!ejb.transaction type="Supports"
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOrders")
	List<RecurringOrder> getRecurringOrders(Set<OrderID> orderIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.trade.queryOffers"
	 * @!ejb.transaction type="Supports"
	 */
	@RolesAllowed("org.nightlabs.jfire.trade.queryOffers")
	List<RecurringOffer> getRecurringOffers(Set<OfferID> offerIDs,
			String[] fetchGroups, int maxFetchDepth);

}