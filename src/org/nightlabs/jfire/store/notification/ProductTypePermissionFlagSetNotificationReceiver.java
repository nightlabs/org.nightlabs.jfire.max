package org.nightlabs.jfire.store.notification;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote;
import org.nightlabs.jfire.jdo.notification.persistent.SubscriptionUtil;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationReceiverID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.ProductTypePermissionFlagSet;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.jfire.trade.TradeManagerRemote;
import org.nightlabs.util.CollectionUtil;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ProductTypePermissionFlagSetNotificationReceiver
extends NotificationReceiver
{
	public static ProductTypePermissionFlagSetNotificationReceiver register(PersistenceManager pm, String emitterOrganisationID)
	throws NamingException, RemoteException, CreateException
	{
		String localOrganisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		pm.getExtent(ProductTypePermissionFlagSetNotificationReceiver.class);

		NotificationReceiverID productTypePermissionFlagSetNotificationReceiverID = NotificationReceiverID.create(
				emitterOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, localOrganisationID,
				ProductTypePermissionFlagSetNotificationFilter.class.getName()
		);
		try {
			ProductTypePermissionFlagSetNotificationReceiver productTypePermissionFlagSetNotificationReceiver = (ProductTypePermissionFlagSetNotificationReceiver) pm.getObjectById(productTypePermissionFlagSetNotificationReceiverID);
			return productTypePermissionFlagSetNotificationReceiver;
		} catch (JDOObjectNotFoundException x) {
			// register below
		}

		Hashtable<?, ?> initialContextProperties = Lookup.getInitialContextProperties(pm, emitterOrganisationID);

		PersistentNotificationEJBRemote persistentNotificationEJB = JFireEjb3Factory.getRemoteBean(PersistentNotificationEJBRemote.class, initialContextProperties);

		ProductTypePermissionFlagSetNotificationFilter productTypePermissionFlagSetNotificationFilter = new ProductTypePermissionFlagSetNotificationFilter(
				emitterOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, localOrganisationID,
				ProductTypePermissionFlagSetNotificationFilter.class.getName()
		);
		ProductTypePermissionFlagSetNotificationReceiver productTypePermissionFlagSetNotificationReceiver = new ProductTypePermissionFlagSetNotificationReceiver(productTypePermissionFlagSetNotificationFilter);
		productTypePermissionFlagSetNotificationReceiver = pm.makePersistent(productTypePermissionFlagSetNotificationReceiver);
		persistentNotificationEJB.storeNotificationFilter(productTypePermissionFlagSetNotificationFilter, false, null, 1);

		return productTypePermissionFlagSetNotificationReceiver;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductTypePermissionFlagSetNotificationReceiver() { }

	public ProductTypePermissionFlagSetNotificationReceiver(NotificationFilter notificationFilter) {
		super(notificationFilter);
	}

	public ProductTypePermissionFlagSetNotificationReceiver(String organisationID, String subscriberType, String subscriberID, String subscriptionID) {
		super(organisationID, subscriberType, subscriberID, subscriptionID);
	}

	@Override
	public void onReceiveNotificationBundle(NotificationBundle notificationBundle)
	throws Exception
	{
		HashSet<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs_load = new HashSet<ProductTypePermissionFlagSetID>();
		HashSet<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs_delete = new HashSet<ProductTypePermissionFlagSetID>();
		for (DirtyObjectID dirtyObjectID : notificationBundle.getDirtyObjectIDs()) {
			if (JDOLifecycleState.DELETED.equals(dirtyObjectID.getLifecycleState())) {
				productTypePermissionFlagSetIDs_delete.add((ProductTypePermissionFlagSetID) dirtyObjectID.getObjectID());
				productTypePermissionFlagSetIDs_load.remove(dirtyObjectID.getObjectID());
			}
			else if (JDOLifecycleState.NEW.equals(dirtyObjectID.getLifecycleState())) {
				productTypePermissionFlagSetIDs_delete.remove(dirtyObjectID.getObjectID());
				productTypePermissionFlagSetIDs_load.add((ProductTypePermissionFlagSetID) dirtyObjectID.getObjectID());
			}
			else
				productTypePermissionFlagSetIDs_load.add((ProductTypePermissionFlagSetID) dirtyObjectID.getObjectID());
		}

		PersistenceManager pm = getPersistenceManager();
		replicateProductTypePermissionFlagSets(pm, notificationBundle.getOrganisationID(), productTypePermissionFlagSetIDs_load, productTypePermissionFlagSetIDs_delete);
	}

	public static void replicateProductTypePermissionFlagSets(
			PersistenceManager pm,
			String emitterOrganisationID,
			Set<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs_load,
			Set<ProductTypePermissionFlagSetID> productTypePermissionFlagSetIDs_delete
	)
	throws NamingException, RemoteException, CreateException
	{
		if (productTypePermissionFlagSetIDs_load.isEmpty())
			return;

//		PersistenceManager pm = getPersistenceManager();

		Hashtable<?,?> initialContextProperties = Lookup.getInitialContextProperties(pm, emitterOrganisationID);
		TradeManagerRemote tradeManager = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, initialContextProperties);
		Collection<ProductTypePermissionFlagSet> productTypePermissionFlagSets = CollectionUtil.castCollection(tradeManager.getProductTypePermissionFlagSets(
				productTypePermissionFlagSetIDs_load,
				new String[] {
						FetchPlan.DEFAULT,
						ProductTypePermissionFlagSet.FETCH_GROUP_PRODUCT_TYPE,
						ProductTypePermissionFlagSet.FETCH_GROUP_USER
				},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
		));

//		Store store = Store.getStore(pm);
//		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		Set<ProductType> productTypes = new HashSet<ProductType>(productTypePermissionFlagSets.size());

		NLJDOHelper.enableTransactionSerializeReadObjects(pm);
		try {

			iterateProductTypePermissionFlagSets: for (ProductTypePermissionFlagSet productTypePermissionFlagSet : productTypePermissionFlagSets) {
				ProductTypeID productTypeID = ProductTypeID.create(
						productTypePermissionFlagSet.getProductTypeOrganisationID(),
						productTypePermissionFlagSet.getProductTypeID()
				);

				ProductType productType;
				try {
					productType = (ProductType) pm.getObjectById(productTypeID);
					productType.getProductTypeLocal(); // is this still necessary? does this DataNucleus bug still exist? (the one that the pm.getObjectById(...) succeeds, but the exception happens deferred when accessing a field)
				} catch (JDOObjectNotFoundException x) {
					continue iterateProductTypePermissionFlagSets; // if the ProductType is not yet here, we ignore this record
				}

				// is there already a newer version?
				ProductTypePermissionFlagSetID productTypePermissionFlagSetID = (ProductTypePermissionFlagSetID) JDOHelper.getObjectId(productTypePermissionFlagSet);
				ProductTypePermissionFlagSet persistentPTPFS;
				try {
					persistentPTPFS = (ProductTypePermissionFlagSet) pm.getObjectById(productTypePermissionFlagSetID);
				} catch (JDOObjectNotFoundException x) {
					persistentPTPFS = null;
				}

				if (persistentPTPFS == null || persistentPTPFS.getVersion() <= productTypePermissionFlagSet.getVersion()) {
					NLJDOHelper.makeDirtyAllFieldsRecursively(productTypePermissionFlagSet);
					productTypePermissionFlagSet = pm.makePersistent(productTypePermissionFlagSet);
					productTypes.add(productType);
				}
			}

		} finally {
			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
		}

		Set<ProductType> productTypesNeedingPermissionCalculation = new HashSet<ProductType>();
		for (ProductType productType : productTypes) {
			productTypesNeedingPermissionCalculation.addAll(
					ProductType.getProductTypesNestingThis(pm, productType)
			);
		}

		Set<ProductTypeID> productTypeIDsNeedingPermissionCalculation = NLJDOHelper.getObjectIDSet(productTypesNeedingPermissionCalculation);
		ProductTypeActionHandler.CalculateProductTypePermissionFlagSetsInvocation invocation = new ProductTypeActionHandler.CalculateProductTypePermissionFlagSetsInvocation(
				productTypeIDsNeedingPermissionCalculation
		);
		try {
			AsyncInvoke.exec(invocation, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
