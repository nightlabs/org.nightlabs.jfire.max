package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreCallback;
import javax.jdo.listener.StoreLifecycleListener;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.id.IssueLinkID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * The {@link IssueLink} class represents a link between an {@link Issue} and the other object.
 * <p>
 * The IssueLink is normally a relation that holds the information that
 * the issue is be related with which object and what kind of relation it is.
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author marco schulze - marco at nightlabs dot de
 */
@javax.jdo.annotations.PersistenceCapable(
		objectIdClass=IssueLinkID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireIssueTracking_IssueLink"
)
@FetchGroups({
	@FetchGroup(
			fetchGroups={"default"},
			name=IssueLink.FETCH_GROUP_ISSUE,
			members=@Persistent(name="issue")
	),
	@FetchGroup(
			fetchGroups={"default"},
			name=IssueLink.FETCH_GROUP_ISSUE_LINK_TYPE,
			members=@Persistent(name="issueLinkType")
	),
	@FetchGroup(
			name=IssueLink.FETCH_GROUP_THIS_ISSUE_LINK,
			members={@Persistent(name="issue"), @Persistent(name="issueLinkType"), @Persistent(name="linkedObjectID")}
	)
})
@Queries({
	@javax.jdo.annotations.Query(
			name="getIssueLinksByIssueAndIssueLinkTypeAndLinkedObjectID",
			value="SELECT WHERE this.issue == :issue && this.issueLinkType == :issueLinkType && this.linkedObjectID == :linkedObjectID"
	),
	@javax.jdo.annotations.Query( // deprecated! see below!
			name="getIssueLinksByOrganisationIDAndLinkedObjectID",
			value="SELECT WHERE this.organisationID == :organisationID && this.linkedObjectID == :linkedObjectID"
	),
	@javax.jdo.annotations.Query(
			name="getIssueLinksByLinkedObjectID",
			value="SELECT WHERE this.linkedObjectID == :linkedObjectID"
	),
	@javax.jdo.annotations.Query(
			name="getIssueLinkIDsByLinkedObjectID",
			value="SELECT JDOHelper.getObjectId(this) WHERE this.linkedObjectID == :linkedObjectID"
	),
	@javax.jdo.annotations.Query(
			name="getIssueLinkCountByLinkedObjectID",
			value="SELECT count(this) WHERE this.linkedObjectID == :linkedObjectID"
	),
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueLink
implements Serializable, DetachCallback, StoreCallback, DeleteCallback
{
	private static final Logger logger = Logger.getLogger(IssueLink.class);

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_ISSUE_LINK = "IssueLink.this";
	public static final String FETCH_GROUP_ISSUE_LINK_TYPE = "IssueLink.issueLinkType";
	public static final String FETCH_GROUP_ISSUE = "IssueLink.issue";

	/**
	 * Virtual fetch-group (not processed by JDO) to obtain the non-persistent property
	 * {@link #getLinkedObject()}. This is done in the {@link DetachCallback} method
	 * {@link #jdoPostDetach(Object)}.
	 */
	public static final String FETCH_GROUP_LINKED_OBJECT = "IssueLink.linkedObject";

	/**
	 * Virtual fetch-group to obtain the class of the non-persistent property {@link #getLinkedObject()} -
	 * i.e. the property
	 */
	public static final String FETCH_GROUP_LINKED_OBJECT_CLASS = "IssueLink.linkedObjectClass";

	/**
	 * @deprecated Use {@link #getIssueLinks(PersistenceManager, ObjectID)} or {@link #getIssueLinkIDs(PersistenceManager, ObjectID)} instead.
	 */
	@Deprecated
	public static Collection<IssueLink> getIssueLinksByOrganisationIDAndLinkedObjectID(PersistenceManager pm, String organisationID, ObjectID linkedObjectID)
	{
		Query q = pm.newNamedQuery(IssueLink.class, "getIssueLinksByOrganisationIDAndLinkedObjectID");
		Map<String, Object> params = new HashMap<String, Object>(2);
		params.put("organisationID", organisationID);
		params.put("linkedObjectID", linkedObjectID.toString());
		@SuppressWarnings("unchecked")
		Collection<IssueLink> c = (Collection<IssueLink>) q.executeWithMap(params);
		return c;
	}

	public static long getIssueLinkCount(PersistenceManager pm, ObjectID linkedObjectID)
	{
		Query q = pm.newNamedQuery(IssueLink.class, "getIssueLinkCountByLinkedObjectID");
		return (Long) q.execute(linkedObjectID.toString());
	}

	public static Collection<IssueLink> getIssueLinks(PersistenceManager pm, ObjectID linkedObjectID)
	{
		Query q = pm.newNamedQuery(IssueLink.class, "getIssueLinksByLinkedObjectID");
		@SuppressWarnings("unchecked")
		Collection<IssueLink> c = (Collection<IssueLink>) q.execute(linkedObjectID.toString());
		return c;
	}

	public static Collection<IssueLinkID> getIssueLinkIDs(PersistenceManager pm, ObjectID linkedObjectID)
	{
		Query q = pm.newNamedQuery(IssueLink.class, "getIssueLinkIDsByLinkedObjectID");
		@SuppressWarnings("unchecked")
		Collection<IssueLinkID> c = (Collection<IssueLinkID>) q.execute(linkedObjectID.toString());
		return c;
	}

	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue link belongs. Within one organisation,
	 * all the issue links have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long issueLinkID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueLinkType issueLinkType;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	private String linkedObjectID;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient ObjectID _linkedObjectID;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Object linkedObject;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Class<?> linkedObjectClass;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected IssueLink() {}

	/**
	 * Create an instance of <code>IssueLink</code>.
	 *
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueLink</code>.
	 * @param issueLinkID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueLink.class</code> to create an id.
	 * @param issue the {@link Issue} to which this IssueLink belongs. Must not be <code>null</code>.
	 * @param issueLinkType the type of the new <code>IssueLink</code>. Must not be <code>null</code>.
	 * @param linkedObject The linked object (a persistence-capable JDO object).
	 */
	protected IssueLink(String organisationID, long issueLinkID, Issue issue, IssueLinkType issueLinkType, Object linkedObject) {
		Organisation.assertValidOrganisationID(organisationID);

		if (issueLinkID < 0)
			throw new IllegalArgumentException("issueLinkID < 0");

		if (issue == null)
			throw new IllegalArgumentException("issue must not be null!");

		if (linkedObject == null)
			throw new IllegalArgumentException("linkedObject must not be null!");

		if (!(linkedObject instanceof PersistenceCapable))
			throw new IllegalArgumentException("linkedObject must implement the interface "+ PersistenceCapable.class.getName() +" (or be the object-id of a PersistenceCapable object)! linkedObject is an instance of " + linkedObject.getClass().getName() + ": " + linkedObject);

		linkedObjectID = JDOHelper.getObjectId(linkedObject).toString();

		if (issueLinkType == null)
			throw new IllegalArgumentException("issueLinkType == null");

		this.organisationID = organisationID;
		this.issue = issue;
		this.issueLinkID = issueLinkID;

		this.linkedObject = linkedObject;
		this.issueLinkType = issueLinkType;
	}

	/**
	 * Create an instance of <code>IssueLink</code>.
	 *
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueLink</code>.
	 * @param issueLinkID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueLink.class</code> to create an id.
	 * @param issue the {@link Issue} to which this IssueLink belongs. Must not be <code>null</code>.
	 * @param issueLinkType the type of the new <code>IssueLink</code>. Must not be <code>null</code>.
	 * @param linkedObjectID an object-id (implementing {@link ObjectID}) identifying a persistence-capable JDO object.
	 */
	protected IssueLink(String organisationID, long issueLinkID, Issue issue, IssueLinkType issueLinkType, ObjectID linkedObjectID, Class<?> linkedObjectClass) {
		Organisation.assertValidOrganisationID(organisationID);
		if (issueLinkID < 0)
			throw new IllegalArgumentException("issueLinkID < 0");

		if (issue == null)
			throw new IllegalArgumentException("issue must not be null!");

		if (!(linkedObjectID instanceof ObjectID))
			throw new IllegalArgumentException("The object-id of linkedObject is not an instance of " + ObjectID.class.getName() + "! It's an instance of: " + (linkedObjectID == null ? null : linkedObjectID.getClass().getName()));

		if (issueLinkType == null)
			throw new IllegalArgumentException("issueLinkType == null");

		this.organisationID = organisationID;
		this.issue = issue;
		this.issueLinkID = issueLinkID;
		this.linkedObjectID = linkedObjectID.toString();
		this.issueLinkType = issueLinkType;

		this._linkedObjectID = linkedObjectID;
		this.linkedObjectClass = linkedObjectClass;
	}

	/**
	 * Returns the organisation id.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	public long getIssueLinkID() {
		return issueLinkID;
	}

	public Issue getIssue() {
		return issue;
	}

	public ObjectID getLinkedObjectID() {
		if (_linkedObjectID == null)
			_linkedObjectID = ObjectIDUtil.createObjectID(this.linkedObjectID);

		return _linkedObjectID;
	}

	public IssueLinkType getIssueLinkType() {
		return issueLinkType;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of IssueLink is currently not persistent! Cannot obtain a PersistenceManager!");
		return pm;
	}

	public Object getLinkedObject() {
		if (this.linkedObject == null)
			this.linkedObject = getPersistenceManager().getObjectById(getLinkedObjectID());

		return linkedObject;
	}

	public Class<?> getLinkedObjectClass() {
		if (this.linkedObjectClass == null) {
			if (linkedObject != null)
				this.linkedObjectClass = linkedObject.getClass();
			else
				this.linkedObjectClass = getPersistenceManager().getObjectById(getLinkedObjectID()).getClass();
		}

		return linkedObjectClass;
	}

	private static ThreadLocal<long[]> detach_linkedObject_recursionCounter = new ThreadLocal<long[]> () {
		@Override
		protected long[] initialValue() {
			long[] res = new long[1];
			res[0] = 0;
			return res;
		}
	};

	/**
	 * This method queries all <code>IssueLink</code>s which belong to an <code>Issue</code> and a specific object id.
	 *
	 * @param pm The <code>PersistenceManager</code> that should be used to access the datastore.
	 * @param linkedObjectID
	 * @return Returns instances of <code>IssueLink</code>.
	 */
	protected static Collection<IssueLink> getIssueLinksByIssueAndIssueLinkTypeAndLinkedObjectID(PersistenceManager pm, Issue issue, IssueLinkType issueLinkType, String linkedObjectID)
	{
		Query q = pm.newNamedQuery(IssueLink.class, "getIssueLinksByIssueAndIssueLinkTypeAndLinkedObjectID");
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("issue", issue);
		params.put("issueLinkType", issueLinkType);
		params.put("linkedObjectID", linkedObjectID);

		return CollectionUtil.castCollection( q.executeWithMap(params) );
	}

	@Override
	public void jdoPostDetach(Object object) {
		IssueLink detached = this;
		IssueLink attached = (IssueLink) object;

		PersistenceManager pm = attached.getPersistenceManager();
		Set<?> fetchGroups = pm.getFetchPlan().getGroups();
		if (fetchGroups.contains(FETCH_GROUP_LINKED_OBJECT)) {
			long[] currentRecursionLevel = detach_linkedObject_recursionCounter.get();
			if (currentRecursionLevel[0] < 1) {
				++currentRecursionLevel[0];
				try {
					detached.linkedObject = pm.detachCopy(attached.getLinkedObject());
				} finally {
					--currentRecursionLevel[0];
				}
			}
		}

		if (fetchGroups.contains(FETCH_GROUP_LINKED_OBJECT_CLASS))
			detached.linkedObjectClass = attached.getLinkedObjectClass();
	}

	public String getPrimaryKey()
	{
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(issueLinkID);
	}

	@Override
	public void jdoPreDetach() {
	}

	@Override
	public void jdoPreStore() {
		if (logger.isDebugEnabled())
			logger.debug("jdoPreStore: about to store IssueLink: " + getPrimaryKey());

		// an object A should only have one IssueLink with the same type to another object B!
		// There can be multiple links with different types between the same 2 objects, but never
		// should there be multiple links with the same type to the same object.
		//
		// Imagine, we have issue 0A, issue 0B, linkedObject 1A, linkedObject 1B, IssueLinkType 01 and IssueLinkType 02:
		// legal states:
		// 1)
		//		issue 0A --- IssueLinkType 01 --- linkedObject 1A
		//		issue 0A --- IssueLinkType 01 --- linkedObject 1B
		//
		// 2)
		//		issue 0A --- IssueLinkType 01 --- linkedObject 1A
		//		issue 0A --- IssueLinkType 02 --- linkedObject 1A
		//
		// 3)
		//		issue 0A --- IssueLinkType 01 --- linkedObject 1A
		//		issue 0B --- IssueLinkType 01 --- linkedObject 1A
		//
		// ILlegal state:
		//		issue 0A --- IssueLinkType 01 --- linkedObject 1A
		//		issue 0A --- IssueLinkType 01 --- linkedObject 1A

		// check the states with some queries only if this instance of IssueLink is new (otherwise nothing is added)
		// => use NLJDOHelper.exists(getPersistenceManager(), this)

		boolean isExisting = NLJDOHelper.exists(getPersistenceManager(), this); // we check in jdoPreStore, because in postStore, it of course always exists (since postStore is called *after* the data is written)
		if (isExisting) {
			if (logger.isDebugEnabled())
				logger.debug("jdoPreStore: the IssueLink " + getPrimaryKey() + " already exists - no need to call the IssueLinkType's postCreateIssueLink callback method.");
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("jdoPreStore: the IssueLink " + getPrimaryKey() + " does NOT yet exist - registering StoreLifecycleListener in order to react on postStore.");

			Collection<IssueLink> issueLinks = IssueLink.getIssueLinksByIssueAndIssueLinkTypeAndLinkedObjectID(getPersistenceManager(), issue, issueLinkType, linkedObjectID);
			// Since this IssueLink is not yet existing in the datastore (we checked above for existance), issueLinks
			// must be empty. If it's not, we throw an exception
			if (!issueLinks.isEmpty()) {
				IssueLink otherIssueLink = issueLinks.iterator().next();
				throw new DuplicateIssueLinkException("There is already an IssueLink with the same issue and issueLinkType! otherIssueLink=" + otherIssueLink + " thisIssueLink=" + this + " issueLinkType=" + this.issueLinkType + " linkedObjectID=" + linkedObjectID);
			}

			getPersistenceManager().addInstanceLifecycleListener(
					new StoreLifecycleListener()
					{
						@Override
						public void preStore(InstanceLifecycleEvent event)
						{
							// nothing to do
						}
						@Override
						public void postStore(InstanceLifecycleEvent event)
						{
							if (!IssueLink.this.equals(event.getPersistentInstance())) {
								if (logger.isDebugEnabled())
									logger.debug("jdoPreStore: StoreLifecycleListener.postStore: triggered for the wrong object (" + JDOHelper.getObjectId(event.getPersistentInstance()) + ") instead of the IssueLink " + getPrimaryKey() + ". Thus, we silently ignore it.");

								// sth. else has been persisted => return and ignore it.
								return;
							}

							// Since the listener is triggered for the right object, it has done what it is supposed to do and should be
							// removed *before* anything can go wrong.
							getPersistenceManager().removeInstanceLifecycleListener(this);

							if (logger.isDebugEnabled())
								logger.debug("jdoPreStore: StoreLifecycleListener.postStore: the IssueLink " + getPrimaryKey() + " does NOT yet exist - calling the IssueLinkType's postCreateIssueLink callback method.");

							getIssueLinkType().postCreateIssueLink(getPersistenceManager(), IssueLink.this);
						}
					},
					new Class[] { IssueLink.class }
			);

		}
	}

	@Override
	public void jdoPreDelete() {
		if (logger.isDebugEnabled())
			logger.debug("jdoPreDelete: about to delete IssueLink: " + getPrimaryKey());

		getIssueLinkType().preDeleteIssueLink(getPersistenceManager(), IssueLink.this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (issueLinkID ^ (issueLinkID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final IssueLink other = (IssueLink) obj;

		return Util.equals(this.organisationID, other.organisationID) && Util.equals(this.issueLinkID, other.issueLinkID);
	}

	@Override
	public String toString() {
		return (
				this.getClass().getName()
				+ '@'
				+ Integer.toHexString(System.identityHashCode(this))
				+ '['
				+ organisationID
				+ ','
				+ ObjectIDUtil.longObjectIDFieldToString(issueLinkID)
				+ ']'
		);
	}
}
