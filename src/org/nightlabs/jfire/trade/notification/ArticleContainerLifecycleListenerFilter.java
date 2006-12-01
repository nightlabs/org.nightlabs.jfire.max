package org.nightlabs.jfire.trade.notification;

import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.transfer.id.AnchorID;

public class ArticleContainerLifecycleListenerFilter
extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ArticleContainerLifecycleListenerFilter.class);

	private JDOLifecycleState[] jdoLifecycleStates;

	private AnchorID customerID;
	private AnchorID vendorID;

	private AnchorID customerIDOrVendorID;

	public ArticleContainerLifecycleListenerFilter(JDOLifecycleState[] jdoLifecycleStates,
			AnchorID customerIDOrVendorID)
	{
		this(jdoLifecycleStates, null, null, customerIDOrVendorID);
	}

	public ArticleContainerLifecycleListenerFilter(JDOLifecycleState[] jdoLifecycleStates,
			AnchorID customerID, AnchorID vendorID)
	{
		this(jdoLifecycleStates, customerID, vendorID, null);
	}

	public ArticleContainerLifecycleListenerFilter(JDOLifecycleState[] jdoLifecycleStates,
			AnchorID customerID, AnchorID vendorID, AnchorID customerIDOrVendorID)
	{
		if (jdoLifecycleStates == null)
			throw new IllegalArgumentException("jdoLifecycleStates must not be null!");
		
		this.jdoLifecycleStates = jdoLifecycleStates;
		this.customerID = customerID;
		this.vendorID = vendorID;
		this.customerIDOrVendorID = customerIDOrVendorID;
	}

	@Implement
	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		if (logger.isDebugEnabled())
			logger.debug("filter entered with " + event.getDirtyObjectIDs().size() + " DirtyObjectIDs.");

		if (customerID == null && vendorID == null && customerIDOrVendorID == null) {
			if (logger.isDebugEnabled())
				logger.debug("filter: customerID, vendorID and customerIDOrVendorID are all null => returning all DirtyObjectIDs.");

			return event.getDirtyObjectIDs();
		}

		PersistenceManager pm = event.getPersistenceManager();

		Collection<DirtyObjectID> res = null;
		iterateDirtyObjectID: for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
			ArticleContainer articleContainer = (ArticleContainer) pm.getObjectById(dirtyObjectID.getObjectID());

			AnchorID accID = articleContainer.getCustomerID();
			AnchorID acvID = articleContainer.getVendorID();
			if (customerID != null && !customerID.equals(accID)) {
				if (logger.isDebugEnabled())
					logger.debug("filter: customerID does not match. Skipping: " + dirtyObjectID);

				continue iterateDirtyObjectID;
			}

			if (vendorID != null && !vendorID.equals(acvID)) {
				if (logger.isDebugEnabled())
					logger.debug("filter: vendorID does not match. Skipping: " + dirtyObjectID);

				continue iterateDirtyObjectID;
			}

			if (customerIDOrVendorID != null && !customerIDOrVendorID.equals(accID) && !customerIDOrVendorID.equals(acvID)) {
				if (logger.isDebugEnabled())
					logger.debug("filter: customerIDOrVendorID does not match. Skipping: " + dirtyObjectID);

				continue iterateDirtyObjectID;
			}

			if (res == null)
				res = new ArrayList<DirtyObjectID>(event.getDirtyObjectIDs().size());

			res.add(dirtyObjectID);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("filter: returning " + (res == null ? null : res.size()) + " DirtyObjectIDs.");
			if (res != null) {
				for (DirtyObjectID dirtyObjectID : res)
					logger.debug("filter:     " + dirtyObjectID);
			}
		}

		return res;
	}

//	private static final Class[] candidateClasses = { ArticleContainer.class };
// TODO the FilterRegistry does not (yet?) support interfaces. Marco.
	private static final Class[] candidateClasses = { Order.class, Offer.class, Invoice.class, DeliveryNote.class };

	@Implement
	public Class[] getCandidateClasses()
	{
		return candidateClasses;
	}

	@Implement
	public JDOLifecycleState[] getLifecycleStates()
	{
		return jdoLifecycleStates;
	}

	public AnchorID getCustomerID()
	{
		return customerID;
	}
	public AnchorID getVendorID()
	{
		return vendorID;
	}
	public AnchorID getCustomerIDOrVendorID()
	{
		return customerIDOrVendorID;
	}
}
