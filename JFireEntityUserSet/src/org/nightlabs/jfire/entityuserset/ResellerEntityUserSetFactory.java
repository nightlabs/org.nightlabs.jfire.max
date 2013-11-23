package org.nightlabs.jfire.entityuserset;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.util.reflect.ReflectUtil;

import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.entityuserset.id.ResellerEntityUserSetFactoryID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.entityuserset.id.ResellerEntityUserSetFactoryID"
 *		detachable="true"
 *		table="JFireEntityUserSet_ResellerEntityUserSetFactory"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.query name="getResellerEntityUserSetFactoryForEntityClassName" query="SELECT UNIQUE WHERE this.entityClassName == :entityClassName"
 */
@PersistenceCapable(
	objectIdClass=ResellerEntityUserSetFactoryID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireEntityUserSet_ResellerEntityUserSetFactory")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries(
	@javax.jdo.annotations.Query(
		name="getResellerEntityUserSetFactoryForEntityClassName",
		value="SELECT UNIQUE WHERE this.entityClassName == :entityClassName")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class ResellerEntityUserSetFactory<Entity>
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ResellerEntityUserSetFactory.class);

	public static <T> ResellerEntityUserSetFactory<T> getResellerEntityUserSetFactory(PersistenceManager pm, Class<T> entityClass, boolean throwExceptionIfNotFound)
	{
		return getResellerEntityUserSetFactory(pm, entityClass.getName(), throwExceptionIfNotFound);
	}

	@SuppressWarnings("unchecked")
	public static <T> ResellerEntityUserSetFactory<T> getResellerEntityUserSetFactory(PersistenceManager pm, String entityClassName, boolean throwExceptionIfNotFound)
	{
		Query q = pm.newNamedQuery(ResellerEntityUserSetFactory.class, "getResellerEntityUserSetFactoryForEntityClassName");
		ResellerEntityUserSetFactory<T> result = (ResellerEntityUserSetFactory<T>) q.execute(entityClassName);
		if (throwExceptionIfNotFound && result == null)
			throw new IllegalStateException("There is no ResellerEntityUserSetFactory for this entityClass: " + entityClassName);

		return result;
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String resellerEntityUserSetFactoryID;

	/**
	 * @jdo.field persistence-modifier="persistent" unique="true"
	 */
	@Element(unique="true")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String entityClassName;

	protected ResellerEntityUserSetFactory() { }

	public ResellerEntityUserSetFactory(String organisationID, String resellerEntityUserSetFactoryID, Class<? extends Entity> entityClass) {
		this.organisationID = organisationID;
		this.resellerEntityUserSetFactoryID = resellerEntityUserSetFactoryID;
		this.entityClassName = entityClass.getName();
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getResellerEntityUserSetFactoryID() {
		return resellerEntityUserSetFactoryID;
	}

	public String getEntityClassName() {
		return entityClassName;
	}

	protected abstract Class<? extends IResellerEntityUserSet<Entity>> getResellerEntityUserSetClass();

	private static final Map<Class<? extends IResellerEntityUserSet<?>>, String> resellerEntityUserSetClass2backendEntityUserSetFieldName = new HashMap<Class<? extends IResellerEntityUserSet<?>>, String>();

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager from this instance: " + this);
		return pm;
	}

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

	protected abstract IResellerEntityUserSet<Entity> createResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Entity> backendEntityUserSet);

	@SuppressWarnings("unchecked")
	public IResellerEntityUserSet<Entity> getResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Entity> backendEntityUserSet, boolean throwExceptionIfNotFound)
	{
		if (backendEntityUserSet == null)
			throw new IllegalArgumentException("backendEntityUserSet must not be null!");

		PersistenceManager pm = getPersistenceManager();
		Query q = pm.newQuery(getResellerEntityUserSetClass());
		q.setFilter("this." + getBackendEntityUserSetFieldName() + " == :backendEntityUserSet");
		q.setUnique(true);
		IResellerEntityUserSet<Entity> resellerEntityUserSet = (IResellerEntityUserSet<Entity>) q.execute(backendEntityUserSet);
		if (throwExceptionIfNotFound && resellerEntityUserSet == null)
			throw new IllegalStateException("There is no resellerEntityUserSet for this backendEntityUserSet: " + backendEntityUserSet);

		return resellerEntityUserSet;
	}

	public IResellerEntityUserSet<Entity> configureResellerEntityUserSetForBackendEntityUserSet(IEntityUserSet<Entity> backendEntityUserSet)
	{
		PersistenceManager pm = getPersistenceManager();
		IResellerEntityUserSet<Entity> resellerEntityUserSet = getResellerEntityUserSetForBackendEntityUserSet(backendEntityUserSet, false);
		if (resellerEntityUserSet == null) {
			resellerEntityUserSet = createResellerEntityUserSetForBackendEntityUserSet(backendEntityUserSet);
			resellerEntityUserSet = pm.makePersistent(resellerEntityUserSet);
		}
		for (AuthorizedObjectRef<Entity> authorizedObjectRef : resellerEntityUserSet.getAuthorizedObjectRefs()) {
			logger.info("configureResellerEntityUserSetForBackendEntityUserSet: " + authorizedObjectRef);
		}
		pm.refresh(resellerEntityUserSet);
		for (AuthorizedObjectRef<Entity> authorizedObjectRef : resellerEntityUserSet.getAuthorizedObjectRefs()) {
			logger.info("configureResellerEntityUserSetForBackendEntityUserSet: " + authorizedObjectRef);
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
