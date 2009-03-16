package org.nightlabs.jfire.entityuserset;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.entityuserset.id.EntityUserSetID;
import org.nightlabs.jfire.entityuserset.notification.EntityUserSetNotificationFilter;
import org.nightlabs.jfire.entityuserset.notification.EntityUserSetNotificationReceiver;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJB;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBUtil;
import org.nightlabs.jfire.jdo.notification.persistent.SubscriptionUtil;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.util.CollectionUtil;

/**
 * @ejb.bean name="jfire/ejb/JFireEntityUserSet/EntityUserSetManager"
 *					 jndi-name="jfire/ejb/JFireEntityUserSet/EntityUserSetManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class EntityUserSetManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException { }

	/**
	 * {@inheritDoc}
	 *
	 * @ejb.permission unchecked="true"
	 */
	@Override
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * This method is called by the organisation initialisation mechanism.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise() throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			SecurityChangeListenerEntityUserSet.register(pm);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void importEntityUserSetsOnCrossOrganisationRegistration(Context context) throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			String emitterOrganisationID = context.getOtherOrganisationID();
			Hashtable<?, ?> initialContextProperties = getInitialContextProperties(emitterOrganisationID);

			EntityUserSetNotificationFilter entityUserSetNotificationFilter = new EntityUserSetNotificationFilter(
					emitterOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, getOrganisationID(),
					EntityUserSetNotificationFilter.class.getName());
			EntityUserSetNotificationReceiver entityUserSetNotificationReceiver = new EntityUserSetNotificationReceiver(entityUserSetNotificationFilter);
			entityUserSetNotificationReceiver = pm.makePersistent(entityUserSetNotificationReceiver);

			PersistentNotificationEJB persistentNotificationEJB = PersistentNotificationEJBUtil.getHome(initialContextProperties).create();
			persistentNotificationEJB.storeNotificationFilter(entityUserSetNotificationFilter, false, null, 1);

// We don't do this, because not all of them are used and especially with the ResellerEntityUserSets, we cause
// eternal cross-referencing. Thus, it's better to only keep existing (= already replicated before) EntityUserSets
// in-sync. We therefore replicate initially nothing and replicate only lazily what's needed.
//			EntityUserSetManager entityUserSetManager = JFireEjbFactory.getBean(EntityUserSetManager.class, initialContextProperties);
//			Set<EntityUserSetID> entityUserSetIDs = CollectionUtil.castSet(
//					entityUserSetManager.getEntityUserSetIDs(emitterOrganisationID, null)
//			);
//
//			entityUserSetNotificationReceiver.replicateEntityUserSets(emitterOrganisationID, entityUserSetIDs, null);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<EntityUserSetID> getEntityUserSetIDs(String organisationID, Class<?> entityClass)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(EntityUserSet.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Map<String, Object> params = new HashMap<String, Object>();
			StringBuilder filter = new StringBuilder();

			if (organisationID != null) {
				params.put("organisationID", organisationID);

				if (filter.length() > 0)
					filter.append(" && ");

				filter.append("this.organisationID == :organisationID");
			}

			if (entityClass != null) {
				params.put("entityClassName", entityClass.getName());

				if (filter.length() > 0)
					filter.append(" && ");

				filter.append("this.entityClassName == :entityClassName");
			}

			q.setFilter(filter.toString());

			Collection<EntityUserSetID> c = CollectionUtil.castCollection((Collection<?>)q.executeWithMap(params));
			return new HashSet<EntityUserSetID>(c);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection<? extends EntityUserSet<?>> getEntityUserSetsForReseller(Collection<EntityUserSetID> entityUserSetIDs)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			UserLocalID principalUserLocalID = UserLocalID.create(
				getPrincipal().getOrganisationID(),
				getPrincipal().getUserID(),
				getOrganisationID()
			);

			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT,
					FetchGroupsEntityUserSet.FETCH_GROUP_REPLICATE_TO_RESELLER
			});

			Collection<EntityUserSet<?>> entityUserSets = NLJDOHelper.getObjectSet(pm, entityUserSetIDs, EntityUserSet.class);
			entityUserSets = pm.detachCopyAll(entityUserSets);

			EntityUserSetController entityUserSetController = new EntityUserSetControllerServerImpl(pm);

			for (EntityUserSet<?> entityUserSet : entityUserSets) {
				entityUserSet.setEntityUserSetController(entityUserSetController);
				entityUserSet.retainAuthorizedObjectsForReplication(principalUserLocalID);
			}

			return entityUserSets;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@Override
	public String ping(String message) {
		return super.ping(message);
	}
}
