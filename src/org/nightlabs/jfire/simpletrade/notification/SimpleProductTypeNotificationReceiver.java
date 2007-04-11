package org.nightlabs.jfire.simpletrade.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfigUtil;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class SimpleProductTypeNotificationReceiver
extends NotificationReceiver
{
	private static final Logger logger = Logger.getLogger(SimpleProductTypeNotificationReceiver.class);

	/**
	 * @deprecated Only for JDO!
	 */
	protected SimpleProductTypeNotificationReceiver() { }

	public SimpleProductTypeNotificationReceiver(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
	}

	public SimpleProductTypeNotificationReceiver(NotificationFilter notificationFilter)
	{
		super(notificationFilter);
	}

	@SuppressWarnings("unchecked")
	@Implement
	public void onReceiveNotificationBundle(NotificationBundle notificationBundle)
	throws Exception
	{
		HashSet productTypeIDs_load = new HashSet();
		HashSet productTypeIDs_delete = new HashSet();
		for (DirtyObjectID dirtyObjectID : notificationBundle.getDirtyObjectIDs()) {
			if (JDOLifecycleState.DELETED.equals(dirtyObjectID.getLifecycleState())) {
				productTypeIDs_delete.add(dirtyObjectID.getObjectID());
				productTypeIDs_load.remove(dirtyObjectID.getObjectID());
			}
			else if (JDOLifecycleState.NEW.equals(dirtyObjectID.getLifecycleState())) {
				productTypeIDs_delete.remove(dirtyObjectID.getObjectID());
				productTypeIDs_load.add(dirtyObjectID.getObjectID());
			}
			else
				productTypeIDs_load.add(dirtyObjectID.getObjectID());
		}

		if (productTypeIDs_load.isEmpty())
			return;

		PersistenceManager pm = getPersistenceManager();

		Hashtable initialContextProperties = Lookup.getInitialContextProperties(pm, notificationBundle.getOrganisationID());
		SimpleTradeManager simpleTradeManager = SimpleTradeManagerUtil.getHome(initialContextProperties).create();
		Collection productTypes = simpleTradeManager.getSimpleProductTypesForReseller(productTypeIDs_load, false);

		Set<PriceConfigID> priceConfigIDs = new HashSet<PriceConfigID>();

		int previousProductTypesSize = productTypes.size();
		while (!productTypes.isEmpty()) {
			for (Iterator itPT = productTypes.iterator(); itPT.hasNext(); ) {
				SimpleProductType simpleProductType = (SimpleProductType) itPT.next();
				if (simpleProductType.getExtendedProductType() == null || NLJDOHelper.exists(pm, simpleProductType.getExtendedProductType())) {

//					// TODO remove this JPOX bug workaround
//					try {
//						if (simpleProductType.getPackagePriceConfig() != null) {
//							StablePriceConfig pc = (StablePriceConfig) Utils.cloneSerializable(simpleProductType.getPackagePriceConfig());
//							for (Iterator itCurr = simpleProductType.getPackagePriceConfig().getCurrencies().iterator(); itCurr.hasNext(); ) {
//								Currency currency = (Currency) itCurr.next();
//								pc.removeCurrency(currency.getCurrencyID());
//							}
//							// now there should not exist any PriceCoordinate anymore in pc
//							pm.makePersistent(pc);
//						}
//					} catch (Throwable t) {
//						logger.warn("Workaround for JPOX bug failed!", t);
//					}
//					// TODO end workaround

					if (simpleProductType.getPackagePriceConfig() != null)
						priceConfigIDs.add((PriceConfigID) JDOHelper.getObjectId(simpleProductType.getPackagePriceConfig()));

					try {
						simpleProductType = (SimpleProductType) pm.makePersistent(simpleProductType);
					} catch (Exception x) {
						logger.error("Persisting SimpleProductType \"" + simpleProductType.getPrimaryKey() + "\" failed!", x);
						throw x;
					}

					itPT.remove();
				}
			}

			if (previousProductTypesSize == productTypes.size())
				break;

			previousProductTypesSize = productTypes.size();
		}

		if (!priceConfigIDs.isEmpty())
			GridPriceConfigUtil.assertConsistency(pm, priceConfigIDs);

		if (!productTypes.isEmpty()) {
			logger.error("Could not persist the following SimpleProductTypes because of missing extendedProductType:");
			for (Iterator it = productTypes.iterator(); it.hasNext(); ) {
				SimpleProductType simpleProductType = (SimpleProductType) it.next();
				String name;
				try {
					name = simpleProductType.getName().getText();
				} catch (Exception x) {
					name = "ERROR: " + x.getMessage();
				}
				logger.error("  - " + simpleProductType.getPrimaryKey() + " (" + name + ")");
			}
		}
	}
}
