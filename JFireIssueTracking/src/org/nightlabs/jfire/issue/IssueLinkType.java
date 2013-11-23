package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * The {@link IssueLinkType} class represents a relation between {@link Issue}s or between {@link Issue} and the other object.
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueLinkTypeID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueLinkType"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueLinkTypeID"
 *
 * @jdo.fetch-group name="IssueLinkType.linkedObjectClassNames" fields="linkedObjectClassNames"
 * @jdo.fetch-group name="IssueLinkType.name" fields="name"
 *
 * @jdo.query
 *		name="getIssueLinkTypesForLinkedObjectClassName"
 *		query="SELECT WHERE this.linkedObjectClassNames.contains(:linkedObjectClassName)"
 */
@PersistenceCapable(
	objectIdClass=IssueLinkTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueLinkType")
@FetchGroups({
	@FetchGroup(
		name=IssueLinkType.FETCH_GROUP_LINKABLE_OBJECT_CLASS_NAMES,
		members=@Persistent(name="linkedObjectClassNames")
	),
	@FetchGroup(
		name=IssueLinkType.FETCH_GROUP_NAME,
		members=@Persistent(name="name")
	),
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members=@Persistent(name="name")
	)
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries(
	@javax.jdo.annotations.Query(
		name="getIssueLinkTypesForLinkedObjectClassName",
		value="SELECT WHERE this.linkedObjectClassNames.contains(:linkedObjectClassName)")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class IssueLinkType
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_LINKABLE_OBJECT_CLASS_NAMES = "IssueLinkType.linkedObjectClassNames";
	public static final String FETCH_GROUP_NAME = "IssueLinkType.name";

	// Of course there can be other types of IssueLinks (even created by the user without programming!), but
	// the following ones are very basic and thus predefined by the jfire team. Some more constants can be found in the
	// subclasses of IssueLinkType (e.g. IssueLinkTypeParentChild).
	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_RELATED = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "related");

	@SuppressWarnings("unchecked")
	private static void populateIssueLinkTypes(PersistenceManager pm, Query q, Class<?> linkedObjectClass, Set<IssueLinkType> issueLinkTypes)
	{
		Class<?> loc = linkedObjectClass;
		while (loc != null) {
			issueLinkTypes.addAll((Collection<? extends IssueLinkType>) q.execute(loc.getName()));

			for (Class<?> locInterface : loc.getInterfaces())
				populateIssueLinkTypes(pm, q, locInterface, issueLinkTypes);

			loc = loc.getSuperclass();
		}
	}

	public static Set<IssueLinkType> getIssueLinkTypes(PersistenceManager pm, Class<?> linkedObjectClass)
	{
		Query q = pm.newNamedQuery(IssueLinkType.class, "getIssueLinkTypesForLinkedObjectClassName");

		Set<IssueLinkType> issueLinkTypes = new HashSet<IssueLinkType>();
		populateIssueLinkTypes(pm, q, linkedObjectClass, issueLinkTypes);

		iterateIssueLinkType : for (Iterator<IssueLinkType> it = issueLinkTypes.iterator(); it.hasNext(); ) {
			IssueLinkType issueLinkType = it.next();

			Class<?> loc = linkedObjectClass;
			while (loc != null) {
				if (issueLinkType.getLinkedObjectClassNames().contains(loc.getName())) {
					continue iterateIssueLinkType;
				}
				if (issueLinkType.getNotLinkedObjectClassNames().contains(loc.getName())) {
					it.remove();
					continue iterateIssueLinkType;
				}

				for (Class<?> locInterface : loc.getInterfaces()) {
					if (issueLinkType.getLinkedObjectClassNames().contains(locInterface.getName())) {
						continue iterateIssueLinkType;
					}
					if (issueLinkType.getNotLinkedObjectClassNames().contains(locInterface.getName())) {
						it.remove();
						continue iterateIssueLinkType;
					}
				}

				loc = loc.getSuperclass();
			}
		}

		return issueLinkTypes;
	}

	/**
	 * This is the organisationID to which the issue link type belongs. Within one organisation,
	 * all the issue link types have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey
	private String issueLinkTypeID;

	/**
	 * String of the referenced object class names.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		dependent-value="true" // this is IMHO wrong. Changed it to @Element (instead of @Value) below.
	 *		table="JFireIssueTracking_IssueLinkType_linkedObjectClassNames"
	 *
	 * @jdo.join
	 */	@Join
	@Persistent(
		table="JFireIssueTracking_IssueLinkType_linkedObjectClassNames",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Element(dependent="true")
	private Set<String> linkedObjectClassNames;

	@Join
	@Persistent(
		table="JFireIssueTracking_IssueLinkType_notLinkedObjectClassNames",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Element(dependent="true")
	private Set<String> notLinkedObjectClassNames;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issueLinkType"
	 */	@Persistent(
		dependent="true",
		mappedBy="issueLinkType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueLinkTypeName name;
	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected IssueLinkType() {}

	/**
	 * Constructs a new IssueLinkType.
	 * @param issueLinkTypeID
	 */
	public IssueLinkType(IssueLinkTypeID issueLinkTypeID) {
		this(issueLinkTypeID.organisationID, issueLinkTypeID.issueLinkTypeID);
	}

	/**
	 * Constructs a new IssueLinkType.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueLinkType</code>
	 * @param issueLinkTypeID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueLinkType.class</code> to create an id.
	 */
	public IssueLinkType(String organisationID, String issueLinkTypeID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(issueLinkTypeID, "issueLinkTypeID");
		this.organisationID = organisationID;
		this.issueLinkTypeID = issueLinkTypeID;

		this.linkedObjectClassNames = new HashSet<String>();
		this.notLinkedObjectClassNames = new HashSet<String>();
		this.name = new IssueLinkTypeName(this);
	}

	public Set<String> getLinkedObjectClassNames() {
		return Collections.unmodifiableSet(linkedObjectClassNames);
	}

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Set<Class<?>> linkedObjectClasses;

	public Set<Class<?>> getLinkedObjectClasses()
	throws ClassNotFoundException
	{
		if (linkedObjectClasses == null) {
			Set<Class<?>> set = new HashSet<Class<?>>(linkedObjectClassNames.size());
			for (String linkedObjectClassName : linkedObjectClassNames)
				set.add(Class.forName(linkedObjectClassName));

			linkedObjectClasses = Collections.unmodifiableSet(set);
		}
		return linkedObjectClasses;
	}

	public void clearLinkedObjectClasses()
	{
		linkedObjectClassNames.clear();
		linkedObjectClasses = null;
	}

	public boolean addLinkedObjectClass(Class<?> linkedObjectClass)
	{
		boolean res = linkedObjectClassNames.add(linkedObjectClass.getName());
		linkedObjectClasses = null;
		return res;
	}

	public boolean removeLinkedObjectClass(Class<?> linkedObjectClass)
	{
		boolean res = linkedObjectClassNames.remove(linkedObjectClass.getName());
		linkedObjectClasses = null;
		return res;
	}

	public Set<String> getNotLinkedObjectClassNames() {
		return Collections.unmodifiableSet(notLinkedObjectClassNames);
	}

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Set<Class<?>> notLinkedObjectClasses;

	public Set<Class<?>> getNotLinkedObjectClasses()
	throws ClassNotFoundException
	{
		if (notLinkedObjectClasses == null) {
			Set<Class<?>> set = new HashSet<Class<?>>(notLinkedObjectClassNames.size());
			for (String notLinkedObjectClassName : notLinkedObjectClassNames)
				set.add(Class.forName(notLinkedObjectClassName));

			notLinkedObjectClasses = Collections.unmodifiableSet(set);
		}
		return notLinkedObjectClasses;
	}

	public void clearNotLinkedObjectClasses()
	{
		notLinkedObjectClassNames.clear();
		notLinkedObjectClasses = null;
	}

	public boolean addNotLinkedObjectClass(Class<?> notLinkedObjectClass)
	{
		boolean res = notLinkedObjectClassNames.add(notLinkedObjectClass.getName());
		notLinkedObjectClasses = null;
		return res;
	}

	public boolean removeNotLinkedObjectClass(Class<?> notLinkedObjectClass)
	{
		boolean res = notLinkedObjectClassNames.remove(notLinkedObjectClass.getName());
		notLinkedObjectClasses = null;
		return res;
	}

	/**
	 *
	 * @return
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 *
	 * @return
	 */
	public String getIssueLinkTypeID() {
		return issueLinkTypeID;
	}

	/**
	 *
	 * @return
	 */
	public IssueLinkTypeName getName() {
		return name;
	}

	/**
	 * Callback method triggered after a new {@link IssueLink} instance has been created and persisted to the datastore.
	 * <p>
	 * Override this method in your subclass of <code>IssueLinkType</code>, if you need to perform some code on creation
	 * of <code>IssueLink</code>s.
	 * </p>
	 *
	 * @param newIssueLink the newly created and already persisted (in the same transaction) IssueLink.
	 * @see #preDeleteIssueLink(IssueLink)
	 */
	protected void postCreateIssueLink(PersistenceManager pm, IssueLink newIssueLink) {
		// [Observation I, 19.06.2009]: Kai.
		//   Here is something interesting. This base IssueLinkType class is identified essentially as 'ISSUE_LINK_TYPE_ID_RELATED'.
		//   And as such, the codes presently do not process the generation of the reverse link between two Issues when they
		//   are 'Related'. That is, the general codes handling 'ISSUE_LINK_TYPE_ID_PARENT', 'ISSUE_LINK_TYPE_ID_CHILD', and
		//   'ISSUE_LINK_TYPE_ID_DUPLICATE' are in the subclass IssueLinkTypeIssueToIssue!
		//
		//   So, while in the general case, in which an IssueLink is uni-directional linking an Issue to an arbitrary linkedObject,
		//   the default IssueLinkType is correctly set to 'ISSUE_LINK_TYPE_ID_RELATED' with no need to generate a reverse link, it
		//   is not true for the case that the linkedObject is also an Issue.
		//
		// [Proposed solution to overcome this weird situation]:
		//   1. Check to see if the linkedObject is an Issue.
		//   2. If so, then we generate the reverse link.

		// Note [by Marco]: (1) A cleaner solution would be to have separate "action-handlers" registered
		// (on a IssueType-class+linkedObject-class base) that can then attach functionality from the outside
		// to existing IssueTypes. In this case, we probably wouldn't even need to subclass IssueLinkType at all.
		// (2) Alternatively, we could somehow suppress the basic "related"-relationship (=IssueLinkType) for Issue-Issue-
		// relations. Since the related-issue-type is registered for java.lang.Object (and all sub-classes), it currently
		// applies to Issue as well. See IssueManagerBean.initialise():
		//
		//			issueLinkTypeRelated.addLinkedObjectClass(Object.class);
		//
		// (3) Alternatively, we could stop assigning a generic "related" to java.lang.Object and instead register
		// individual IssueLinkTypes like "related-to-Person", "related-to-ArticleContainer" etc.
		// (4) Or 4th possibility: these modules simply add their classes in their initialise.
		// The 3rd and 4th solution has the disadvantage of requiring new modules (JFireTrade must not know JFireIssueTracking,
		// thus for the registration of o.n.j.trade.ArticleContainer to the "related" IssueLinkType, we'd need a new module
		// having a dependency on both JFireTrade and JFireIssueTracking).
		// The solutions (1) and (2) don't require new modules.
		// (1) renders being able to subclass IssueLinkType meaningless and makes things IMHO more complex.
		// (2) is IMHO the best solution, though it does require an extension of the resolve mechanism
		// (resolving which linked-object-class fits which IssueLinkTypes) to allow for blacklisting (and thus we'd only have a
		// special "related-to-Issue" replacing the generic "related", but use the "related" for all other Objects).
		// But the concept of blacklisting is pretty easy to understand.
		// The resolve logic would become quite complex, though, because it should implement the blacklist in a hierarchical way.
		// That means, if java.lang.Object is included and org.nightlabs.A (extends Object) is blacklisted, then
		// org.nightlabs.B (extends A) should be blacklisted as well. However, if org.nightlabs.C (extends B) is registered
		// explicitely, it should not be blacklisted.
		// Therefore, I recommend that we leave it as it currently is for now - we might refactor this later. Marco.

//		Object linkedObject = newIssueLink.getLinkedObject();
//		if (linkedObject instanceof Issue) {
//			if (!(this instanceof IssueLinkTypeIssueToIssue))
//				throw new IllegalStateException("An issue should be linked by an instance of IssueLinkTypeIssueToIssue!");
////			IssueLinkTypeIssueToIssue.generateIssueToIssueReverseLink(pm, newIssueLink, (Issue)linkedObject);
//		}

		// [Observation II, 22.06.2009]: Kai
		// --> Mhm... then by the logic of the new approach, as noted (by any of the four solutions) above, it becomes clear that this method 'postCreateIssueLink()'
		//     in this base class IssueLinkType is self-sufficient without any additional codes. That is, it is sufficient until it becaomes
		//     necessary to define 'specific' behaviour(s), such as the method in the current subclass IssueLinkTypeIssueToIssue.
	}

	/**
	 * Callback method triggered before an {@link IssueLink} instance has been deleted from the datastore.
	 *
	 * @param issueLinkToBeDeleted the <code>IssueLink</code> that is about to be deleted.
	 * @see #postCreateIssueLink(IssueLink)
	 */
	protected void preDeleteIssueLink(PersistenceManager pm, IssueLink issueLinkToBeDeleted) {
		// See notes in [Observation 19.06.2009] in postCreateIssueLink(). Kai.
		// The reverse of the argument is also true here.

//		Object linkedObject = issueLinkToBeDeleted.getLinkedObject();
//		if (linkedObject instanceof Issue) {
//			if (!(this instanceof IssueLinkTypeIssueToIssue))
//				throw new IllegalStateException("An issue should be linked by an instance of IssueLinkTypeIssueToIssue!");
////			IssueLinkTypeIssueToIssue.removeIssueToIssueReverseLink(pm, issueLinkToBeDeleted, (Issue)linkedObject);
//		}
	}

	/*
	 * (non-Javadoc)
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of IssueLinkType is currently not persistent! Cannot obtain a PersistenceManager!");
		return pm;
	}

	@Override
	/*
	 * (non-Javadoc)
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() != this.getClass()) return false;
		IssueLinkType o = (IssueLinkType) obj;
		return (
				Util.equals(this.issueLinkTypeID, o.issueLinkTypeID) &&
				Util.equals(this.organisationID, o.organisationID)
		);
	}

	@Override
	/*
	 * (non-Javadoc)
	 */
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) ^ Util.hashCode(issueLinkTypeID);
	}

	@Override
	/*
	 * (non-Javadoc)
	 */
	public String toString() {
		return (
				this.getClass().getName()
				+ '@'
				+ Integer.toHexString(System.identityHashCode(this))
				+ '['
				+ organisationID
				+ ','
				+ issueLinkTypeID
				+ ']'
		);
	}
}