package org.nightlabs.jfire.simpletrade.notification;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

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
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.id.ProductTypeID;

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
	@Deprecated
	protected SimpleProductTypeNotificationReceiver() { }

	public SimpleProductTypeNotificationReceiver(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
	}

	public SimpleProductTypeNotificationReceiver(NotificationFilter notificationFilter)
	{
		super(notificationFilter);
	}

	@Override
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
		replicateSimpleProductTypes(notificationBundle.getOrganisationID(), productTypeIDs_load, productTypeIDs_delete);
	}

	public void replicateSimpleProductTypes(String emitterOrganisationID, Set<ProductTypeID> productTypeIDs_load, Set<ProductTypeID> productTypeIDs_delete)
	throws NamingException, RemoteException, CreateException
	{
		if (productTypeIDs_load.isEmpty())
			return;

		PersistenceManager pm = getPersistenceManager();

		Hashtable<?,?> initialContextProperties = Lookup.getInitialContextProperties(pm, emitterOrganisationID);
		SimpleTradeManager simpleTradeManager = SimpleTradeManagerUtil.getHome(initialContextProperties).create();
		Collection<SimpleProductType> productTypes = simpleTradeManager.getSimpleProductTypesForReseller(productTypeIDs_load);

		Set<PriceConfigID> priceConfigIDs = new HashSet<PriceConfigID>();

		Store store = Store.getStore(pm);
		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		int previousProductTypesSize = productTypes.size();
		while (!productTypes.isEmpty()) {
			for (Iterator<SimpleProductType> itPT = productTypes.iterator(); itPT.hasNext(); ) {
				SimpleProductType simpleProductType = itPT.next();

				if (simpleProductType.getExtendedProductType() == null || NLJDOHelper.exists(pm, simpleProductType.getExtendedProductType())) {
					if (simpleProductType.getPackagePriceConfig() != null)
						priceConfigIDs.add((PriceConfigID) JDOHelper.getObjectId(simpleProductType.getPackagePriceConfig()));

					try {

						NLJDOHelper.makeDirtyAllFieldsRecursively(simpleProductType);

						if (NLJDOHelper.exists(pm, simpleProductType))
							simpleProductType = pm.makePersistent(simpleProductType);
						else {
							simpleProductType = (SimpleProductType) store.addProductType(
									user,
									simpleProductType);
						}

						if (simpleProductType.getName() == null)
							throw new IllegalStateException("simpleProductType.getName() == null after replication!");
					} catch (Exception x) {
						logger.error("Adding SimpleProductType \"" + simpleProductType.getPrimaryKey() + "\" to Store failed!", x);
						throw x instanceof RuntimeException ? (RuntimeException)x : new RuntimeException(x);
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
			for (Iterator<SimpleProductType> it = productTypes.iterator(); it.hasNext(); ) {
				SimpleProductType simpleProductType = it.next();
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
