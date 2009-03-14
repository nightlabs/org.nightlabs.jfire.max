package org.nightlabs.jfire.entityuserset;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * @author marco schulze - marco at nightlabs dot de
 */
public abstract class ResellerEntityUserSetUtil<Entity, ResellerEntityUserSetImplementation extends IResellerEntityUserSet<Entity>>
{
	private static final long serialVersionUID = 1L;

	private PersistenceManager pm;

	public ResellerEntityUserSetUtil(PersistenceManager pm) {
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		this.pm = pm;
	}

	protected abstract Class<? extends IResellerEntityUserSet<Entity>> getResellerEntityUserSetClass();

	private static final Map<Class<? extends IResellerEntityUserSet<?>>, String> resellerEntityUserSetClass2backendEntityUserSetFieldName = new HashMap<Class<? extends IResellerEntityUserSet<?>>, String>();

	protected String getBackendEntityUserSetFieldName()
	{
		Class<? extends IResellerEntityUserSet<Entity>> resellerEntityUserSetClass = getResellerEntityUserSetClass();
		String fieldName = resellerEntityUserSetClass2backendEntityUserSetFieldName.get(resellerEntityUserSetClass);
		if (fieldName == null) {
			Field foundField = null;
			List<Field> fields = ReflectUtil.collectAllFields(resellerEntityUserSetClass, true);
			for (Field field : fields) {
				if (IEntityUserSet.class.isAssignableFrom(field.getType())) {
					if (foundField == null)
						foundField = field;
					else
						throw new IllegalStateException("The class " + resellerEntityUserSetClass.getName() + " contains multiple fields of type IEntityUserSet. You must override the method getBackendEntityUserSetFieldName() in your class " + this.getClass().getName() + "!");
				}
			}

			if (foundField == null)
				throw new IllegalStateException("The class " + resellerEntityUserSetClass.getName() + " does not contain any field of type IEntityUserSet. You must implement IResellerEntityUserSet correctly and maybe you must override the method getBackendEntityUserSetFieldName() in your class " + this.getClass().getName() + "!");

			fieldName = foundField.getName();

			resellerEntityUserSetClass2backendEntityUserSetFieldName.put(resellerEntityUserSetClass, fieldName);
		}
		return fieldName;
	}

	protected abstract ResellerEntityUserSetImplementation createResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Entity> backendEntityUserSet);

	@SuppressWarnings("unchecked")
	public ResellerEntityUserSetImplementation getResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Entity> backendEntityUserSet)
	{
		if (backendEntityUserSet == null)
			throw new IllegalArgumentException("backendEntityUserSet must not be null!");

		Query q = pm.newQuery(getResellerEntityUserSetClass());
		q.setFilter("this." + getBackendEntityUserSetFieldName() + " == :backendEntityUserSet");
		q.setUnique(true);
		return (ResellerEntityUserSetImplementation) q.execute(backendEntityUserSet);
	}

	public ResellerEntityUserSetImplementation configureResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Entity> backendEntityUserSet)
	{
		ResellerEntityUserSetImplementation resellerEntityUserSet = getResellerEntityUserSetForBackendEntityUserSet(backendEntityUserSet);
		if (resellerEntityUserSet == null) {
			resellerEntityUserSet = createResellerEntityUserSetForBackendEntityUserSet(backendEntityUserSet);
			resellerEntityUserSet = pm.makePersistent(resellerEntityUserSet);
		}

		if (backendEntityUserSet.getOrganisationID().equals(resellerEntityUserSet.getOrganisationID()))
			throw new IllegalStateException("backendEntityUserSet.organisationID == resellerEntityUserSet.organisationID == \"" + backendEntityUserSet.getOrganisationID() + "\" :: currentUser == " + SecurityReflector.getUserDescriptor());

		resellerEntityUserSet.setEntityUserSetController(new EntityUserSetControllerServerImpl(pm));

		UserLocalID resellerUserLocalID = UserLocalID.create(
				backendEntityUserSet.getOrganisationID(),
				User.USER_ID_PREFIX_TYPE_ORGANISATION + resellerEntityUserSet.getOrganisationID(),
				backendEntityUserSet.getOrganisationID()
		);

		AuthorizedObjectRef<Entity> resellerUserLocalAuthorizedObjectRef = backendEntityUserSet.getAuthorizedObjectRef(resellerUserLocalID);
		if (resellerUserLocalAuthorizedObjectRef == null) {
			// If this AuthorizedObjectRef does not exist, we simply remove everything from the resellerEntityUserSet.
			for (AuthorizedObjectRef<Entity> aor : new ArrayList<AuthorizedObjectRef<Entity>>(resellerEntityUserSet.getAuthorizedObjectRefs()))
				resellerEntityUserSet.removeAuthorizedObject(aor.getAuthorizedObjectIDAsOID());
		}
		else {
			// We have to assign exactly those rights to all local users that we have in the resellerUserLocalAuthorizedObjectRef.
			Collection<? extends UserLocal> userLocals = UserLocal.getLocalUserLocals(pm);
			Set<UserLocalID> userLocalIDs = NLJDOHelper.getObjectIDSet(userLocals);

			// Remove all AuthorizedObjectRef (e.g. user-security-groups) from the DefaultEntityUserSet that shouldn't be there.
			resellerEntityUserSet.retainAuthorizedObjects(userLocalIDs);

			Collection<? extends EntityRef<Entity>> entityRefs = resellerUserLocalAuthorizedObjectRef.getEntityRefs();
			Set<Entity> entities = new HashSet<Entity>(entityRefs.size());
			for (EntityRef<Entity> entityRef : entityRefs)
				entities.add(entityRef.getEntity());

			for (UserLocalID userLocalID : userLocalIDs) {
				AuthorizedObjectRef<Entity> authorizedObjectRef = resellerEntityUserSet.addAuthorizedObject(userLocalID);
				authorizedObjectRef.retainEntities(entities);
				authorizedObjectRef.addEntities(entities);
			}
		}

		return resellerEntityUserSet;
	}
}
