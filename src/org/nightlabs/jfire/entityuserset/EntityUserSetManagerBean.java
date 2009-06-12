package org.nightlabs.jfire.entityuserset;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.entityuserset.id.EntityUserSetID;
import org.nightlabs.jfire.entityuserset.notification.EntityUserSetNotificationFilter;
import org.nightlabs.jfire.entityuserset.notification.EntityUserSetNotificationReceiver;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote;
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class EntityUserSetManagerBean
extends BaseSessionBeanImpl
implements EntityUserSetManagerRemote
{
	private static final long serialVersionUID = 1L;

	/**
	 * This method is called by the organisation initialisation mechanism.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void importEntityUserSetsOnCrossOrganisationRegistration(Context context) throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			String emitterOrganisationID = context.getOtherOrganisationID();
			Hashtable<?, ?> initialContextProperties = getInitialContextProperties(emitterOrganisationID);

			EntityUserSetNotificationFilter entityUserSetNotificationFilter = new EntityUserSetNotificationFilter(
					emitterOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, getOrganisationID(),
					EntityUserSetNotificationFilter.class.getName());
			EntityUserSetNotificationReceiver entityUserSetNotificationReceiver = new EntityUserSetNotificationReceiver(entityUserSetNotificationFilter);
			entityUserSetNotificationReceiver = pm.makePersistent(entityUserSetNotificationReceiver);

			PersistentNotificationEJBRemote persistentNotificationEJB = JFireEjb3Factory.getRemoteBean(PersistentNotificationEJBRemote.class, initialContextProperties);
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
	@RolesAllowed("_Guest_")
	@Override
	public Set<EntityUserSetID> getEntityUserSetIDs(String organisationID, Class<?> entityClass)
	{
		PersistenceManager pm = createPersistenceManager();
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
	@RolesAllowed("_Guest_")
	@Override
	public Collection<? extends EntityUserSet<?>> getEntityUserSetsForReseller(Collection<EntityUserSetID> entityUserSetIDs)
	{
		PersistenceManager pm = createPersistenceManager();
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
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	@Override
	public List<EntityUserSet<?>> getEntityUserSets(Set<EntityUserSetID> entityUserSetsIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			// Do we need security checks?
//			List<EntityUserSet<?>> entityUserSets = NLJDOHelper.getObjectList(pm, entityUserSetsIDs, EntityUserSet.class);
//			entityUserSets = Authority.filterIndirectlySecuredObjects(
//					pm,
//					entityUserSets,
//					getPrincipal(),
//					RoleConstants.seeProductType,
//					ResolveSecuringAuthorityStrategy.allow);
//			entityUserSets = (List<EntityUserSet<?>>) pm.detachCopyAll(entityUserSets);
//			return entityUserSets;

			return NLJDOHelper.getDetachedObjectList(pm, entityUserSetsIDs, EntityUserSet.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public EntityUserSet<?> storeEntityUserSet(EntityUserSet<?> entityUserSet, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		// TODO Do we need a new right for storing EntityUserSets?
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, entityUserSet, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
