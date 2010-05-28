package org.nightlabs.jfire.trade.link;

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
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.DeleteCallback;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkTypeID;
import org.nightlabs.util.Util;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		objectIdClass=ArticleContainerLinkTypeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_ArticleContainerLinkType"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
		@FetchGroup(
				name=ArticleContainerLinkType.FETCH_GROUP_NAME,
				members={@Persistent(name="name")}
		),
		@FetchGroup(
				name=ArticleContainerLinkType.FETCH_GROUP_DESCRIPTION,
				members={@Persistent(name="description")}
		)
})
public class ArticleContainerLinkType
implements Serializable, DeleteCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "ArticleContainerLinkType.name";
	public static final String FETCH_GROUP_DESCRIPTION = "ArticleContainerLinkType.description";

	public static final ArticleContainerLinkTypeID ID_OFFER_REPLACING = ArticleContainerLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "offer.replacing");
	public static final ArticleContainerLinkTypeID ID_OFFER_REPLACED = ArticleContainerLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "offer.replaced");

	private static void populateArticleContainerLinkTypeSet(Set<ArticleContainerLinkType> articleContainerLinkTypes, Query q, Class<?> clazz, Set<Class<?>> processedClassesAndInterfaces)
	{
		if (!processedClassesAndInterfaces.add(clazz))
			return;

		while (clazz != null) {
			@SuppressWarnings("unchecked")
			Collection<ArticleContainerLinkType> c = (Collection<ArticleContainerLinkType>) q.execute(clazz.getName());
			articleContainerLinkTypes.addAll(c);

			Class<?>[] interfaces = clazz.getInterfaces();
			if (interfaces != null) {
				for (Class<?> iface : interfaces) {
					populateArticleContainerLinkTypeSet(articleContainerLinkTypes, q, iface, processedClassesAndInterfaces);
				}
			}

			clazz = clazz.getSuperclass();
		}
	}

	/**
	 * Get those <code>ArticleContainerLinkType</code>s that are applicable for a certain combination of
	 * from-class and to-class. The arguments <code>fromClass</code> and <code>toClass</code> can be <code>null</code>
	 * (either one of them or even both) - in this case, the result won't be filtered for the side that is <code>null</code>.
	 * <p>
	 * For a detailed explanation of when an <code>ArticleContainerLinkType</code> is applicable for a certain class,
	 * please consult the javadoc of {@link #isFromClassIncluded(Class)}.
	 * </p>
	 *
	 * @param pm the door to the datastore. This argument must not be <code>null</code>.
	 * @param fromClass the class on the from-side of the link. This argument might be <code>null</code> (and thus won't be used as a filter).
	 * @param toClass the class on the to-side of the link. This argument might be <code>null</code> (and thus won't be used as a filter).
	 * @return all matching <code>ArticleContainerLinkType</code>s (and if both <code>fromClass</code> and <code>toClass</code> are <code>null</code>, this will be all instances existing in the datastore).
	 */
	public static Collection<ArticleContainerLinkType> getArticleContainerLinkTypes(PersistenceManager pm, Class<? extends ArticleContainer> fromClass, Class<? extends ArticleContainer> toClass)
	{
		// The following algorithm is not very efficient, but given the low number of instances
		// of ArticleContainerLinkType (<= 100) in our datastore, we can live fine with it. If it
		// ever becomes a problem, we still can improve this algorithm.
		// Marco.

		// First look for all ArticleContainerLinkTypes that are candidates.
		Query q = pm.newQuery(ArticleContainerLinkType.class);
		Set<ArticleContainerLinkType> articleContainerLinkTypes;
		if (fromClass != null) {
			q.setFilter("this.fromClassesIncluded.contains(:className)");
			articleContainerLinkTypes = new HashSet<ArticleContainerLinkType>();
			populateArticleContainerLinkTypeSet(articleContainerLinkTypes, q, fromClass, new HashSet<Class<?>>());
		}
		else if (toClass != null) {
			q.setFilter("this.toClassesIncluded.contains(:className)");
			articleContainerLinkTypes = new HashSet<ArticleContainerLinkType>();
			populateArticleContainerLinkTypeSet(articleContainerLinkTypes, q, toClass, new HashSet<Class<?>>());
		}
		else {
			@SuppressWarnings("unchecked")
			Collection<ArticleContainerLinkType> c = (Collection<ArticleContainerLinkType>) q.execute();
			articleContainerLinkTypes = new HashSet<ArticleContainerLinkType>(c);
		}
		// These candidates might not yet be correct, because the excluded classes were not yet taken into account.
		// This means there might now be too many instances in the Set articleContainerLinkTypes.

		// Now, we iterate our candidates and check each whether it's really applicable.
		if (fromClass != null) {
			for (Iterator<ArticleContainerLinkType> it = articleContainerLinkTypes.iterator(); it.hasNext(); ) {
				ArticleContainerLinkType articleContainerLinkType = it.next();
				if (!articleContainerLinkType.isFromClassIncluded(fromClass))
					it.remove();
			}
		}

		if (toClass != null) {
			for (Iterator<ArticleContainerLinkType> it = articleContainerLinkTypes.iterator(); it.hasNext(); ) {
				ArticleContainerLinkType articleContainerLinkType = it.next();
				if (!articleContainerLinkType.isToClassIncluded(toClass))
					it.remove();
			}
		}

		// ...and finally return our result.
		return articleContainerLinkTypes;
	}

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String articleContainerLinkTypeID;

	@Persistent(nullValue=NullValue.EXCEPTION) // , dependent="true") DATANUCLEUS WORKAROUND: see jdoPreDelete()
	private ArticleContainerLinkTypeName name;

	@Persistent(nullValue=NullValue.EXCEPTION) // , dependent="true") DATANUCLEUS WORKAROUND: see jdoPreDelete()
	private ArticleContainerLinkTypeDescription description;

	@Persistent(table="JFireTrade_ArticleContainerLinkType_fromClassesIncluded")
	@Join
	private Set<String> fromClassesIncluded;

	@Persistent(table="JFireTrade_ArticleContainerLinkType_fromClassesExcluded")
	@Join
	private Set<String> fromClassesExcluded;

	@Persistent(table="JFireTrade_ArticleContainerLinkType_toClassesIncluded")
	@Join
	private Set<String> toClassesIncluded;

	@Persistent(table="JFireTrade_ArticleContainerLinkType_toClassesExcluded")
	@Join
	private Set<String> toClassesExcluded;

	private String reverseArticleContainerLinkTypeID;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient ArticleContainerLinkTypeID _reverseArticleContainerLinkTypeID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ArticleContainerLinkType() { }

	public ArticleContainerLinkType(ArticleContainerLinkTypeID articleContainerLinkTypeID) {
		this(
				articleContainerLinkTypeID.organisationID,
				articleContainerLinkTypeID.articleContainerLinkTypeID
		);
	}

	public ArticleContainerLinkType(String organisationID, String articleContainerLinkTypeID) {
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(articleContainerLinkTypeID, "articleContainerLinkTypeID");

		this.organisationID = organisationID;
		this.articleContainerLinkTypeID = articleContainerLinkTypeID;

		name = new ArticleContainerLinkTypeName(this);
		description = new ArticleContainerLinkTypeDescription(this);

		fromClassesIncluded = new HashSet<String>();
		fromClassesExcluded = new HashSet<String>();
		toClassesIncluded = new HashSet<String>();
		toClassesExcluded = new HashSet<String>();
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getArticleContainerLinkTypeID() {
		return articleContainerLinkTypeID;
	}

	public ArticleContainerLinkTypeName getName() {
		return name;
	}

	public ArticleContainerLinkTypeDescription getDescription() {
		return description;
	}


	public void addFromClassIncluded(Class<?> clazz)
	{
		fromClassesIncluded.add(clazz.getName());
	}

	public void removeFromClassIncluded(Class<?> clazz)
	{
		fromClassesIncluded.remove(clazz.getName());
	}

	public void addFromClassExcluded(Class<?> clazz)
	{
		fromClassesExcluded.add(clazz.getName());
	}

	public void removeFromClassExcluded(Class<?> clazz)
	{
		fromClassesExcluded.remove(clazz.getName());
	}


	public void addToClassIncluded(Class<?> clazz)
	{
		toClassesIncluded.add(clazz.getName());
	}

	public void removeToClassIncluded(Class<?> clazz)
	{
		toClassesIncluded.remove(clazz.getName());
	}

	public void addToClassExcluded(Class<?> clazz)
	{
		toClassesExcluded.add(clazz.getName());
	}

	public void removeToClassExcluded(Class<?> clazz)
	{
		toClassesExcluded.remove(clazz.getName());
	}

	public Set<String> getFromClassNamesIncluded() {
		return Collections.unmodifiableSet(fromClassesIncluded);
	}
	public Set<String> getFromClassNamesExcluded() {
		return Collections.unmodifiableSet(fromClassesExcluded);
	}

	public Set<String> getToClassNamesIncluded() {
		return Collections.unmodifiableSet(toClassesIncluded);
	}
	public Set<String> getToClassNamesExcluded() {
		return Collections.unmodifiableSet(toClassesExcluded);
	}

	/**
	 * Find out, whether this instance of <code>ArticleContainerLinkType</code> is applicable for a concrete class on
	 * the "from"-side of the link.
	 * <p>
	 * An <code>ArticleContainerLinkType</code> is applicable for a certain <code>ArticleContainer</code>-implementation,
	 * if the implementation-class or one of its super-classes or one of the interfaces (direct or indirect) have been
	 * added via {@link #addFromClassIncluded(Class)}. An exclude via {@link #addFromClassExcluded(Class)} has a higher
	 * priority than an include, if it is encountered earlier or at the same time during the recursive class+interface-analysis.
	 * </p>
	 * <p>
	 * Said analysis starts at the concrete class and then iterates through its super-classes. For each class (and super-class),
	 * it checks the implemented interfaces in the declaration order. Note, that it checks the interfaces before checking the super-class.
	 * If an interface extends other interfaces, the algorithm first checks these other interfaces before continuing with the next
	 * implemented interface (or the super-class, if there is no more interface to be checked).
	 * </p>
	 * <p>
	 * Example 1: Class Cc extends Cb, which extends Ca. Ca implements {@link ArticleContainer}. If <code>ArticleContainer</code>
	 * was added to the include-list, but Cb was added to the exclude-list, the link-type would <b>not</b> be applicable for Cc, because the resolve-
	 * mechanism goes from Cc to Cb before coming to Ca. Since Cb is already excluded, the result is clear and no further
	 * analysis happens.
	 * </p>
	 * <p>
	 * Example 2: Again the same as example 1, but this time, class Cc additionally implements interface Ic and Ic has been added
	 * to the include-list. Before the resolve-algorithm goes from class Cc to its super-class, it checks the interfaces implemented by
	 * Cc. Since it finds out that Ic is in the include-list, the class Cc is now applicable and the fact that its super-class is excluded
	 * does not matter.
	 * </p>
	 *
	 * @param clazz the concrete class.
	 * @return <code>true</code>, if the class was directly or indirectly (via super-class or interface) included
	 * (via {@link #addFromClassIncluded(Class)}) and not excluded (via {@link #addFromClassExcluded(Class)}).
	 */
	public boolean isFromClassIncluded(Class<? extends ArticleContainer> clazz)
	{
		Boolean b = isClassIncluded(clazz, fromClassesIncluded, fromClassesExcluded);
		if (b != null)
			return b;
		else
			return false;
	}

	/**
	 * Find out, whether this instance of <code>ArticleContainerLinkType</code> is applicable for a concrete class on
	 * the "to"-side of the link.
	 * <p>
	 * Further information can be found in the documentation of {@link #isFromClassIncluded(Class)} (both sides - "from" and "to" - work
	 * the same).
	 * </p>
	 *
	 * @param clazz the concrete class.
	 * @return <code>true</code>, if the class was directly or indirectly (via super-class or interface) included
	 * (via {@link #addToClassIncluded(Class)}) and not excluded (via {@link #addToClassExcluded(Class)}).
	 * @see #isFromClassIncluded(Class)
	 */
	public boolean isToClassIncluded(Class<? extends ArticleContainer> clazz)
	{
		Boolean b = isClassIncluded(clazz, toClassesIncluded, toClassesExcluded);
		if (b != null)
			return b;
		else
			return false;
	}


	private static Boolean isClassIncluded(Class<?> clazz, Set<String> classesIncluded, Set<String> classesExcluded)
	{
		Class<?> c = clazz;
		while (c != null) {
			String cn = c.getName();

			// excludes have priority! thus, we check them first.
			if (classesExcluded.contains(cn))
				return Boolean.FALSE;

			if (classesIncluded.contains(cn))
				return Boolean.TRUE;

			Class<?>[] interfaces = c.getInterfaces();
			if (interfaces != null) {
				for (Class<?> iface : interfaces) {
					Boolean b = isClassIncluded(iface, classesIncluded, classesExcluded);
					if (b != null)
						return b;
				}
			}

			c = c.getSuperclass();
		}
		return null;
	}

	public ArticleContainerLinkTypeID getReverseArticleContainerLinkTypeID() {
		if (_reverseArticleContainerLinkTypeID == null) {
			if (reverseArticleContainerLinkTypeID == null)
				return null;

			try {
				_reverseArticleContainerLinkTypeID = new ArticleContainerLinkTypeID(reverseArticleContainerLinkTypeID);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return _reverseArticleContainerLinkTypeID;
	}

	public void setReverseArticleContainerLinkTypeID(ArticleContainerLinkTypeID reverseArticleContainerLinkTypeID) {
		String reverseArticleContainerLinkTypeIDString = reverseArticleContainerLinkTypeID == null ? null : reverseArticleContainerLinkTypeID.toString();
		if (Util.equals(reverseArticleContainerLinkTypeIDString, this.reverseArticleContainerLinkTypeID))
			return;

		this._reverseArticleContainerLinkTypeID = reverseArticleContainerLinkTypeID;
		this.reverseArticleContainerLinkTypeID = reverseArticleContainerLinkTypeIDString;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((articleContainerLinkTypeID == null) ? 0 : articleContainerLinkTypeID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		ArticleContainerLinkType other = (ArticleContainerLinkType) obj;
		return (
				Util.equals(this.articleContainerLinkTypeID, other.articleContainerLinkTypeID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + articleContainerLinkTypeID + ']';
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager from this! " + this);

		return pm;
	}

	protected ArticleContainerLinkType getReverseArticleContainerLinkType()
	{
		PersistenceManager pm = getPersistenceManager();

		ArticleContainerLinkTypeID reverseArticleContainerLinkTypeID = getReverseArticleContainerLinkTypeID();
		if (reverseArticleContainerLinkTypeID == null)
			return this;

		return (ArticleContainerLinkType) pm.getObjectById(reverseArticleContainerLinkTypeID);
	}

	public void postArticleContainerLinkCreated(ArticleContainerLink articleContainerLink)
	{
		PersistenceManager pm = getPersistenceManager();

		if (!isFromClassIncluded(articleContainerLink.getFrom().getClass()))
			throw new IllegalStateException("From class not included: Cannot create ArticleContainerLink with type '" + getOrganisationID() + '/' + getArticleContainerLinkTypeID() + "' for this 'from': " + articleContainerLink.getFrom());

		if (!isToClassIncluded(articleContainerLink.getTo().getClass()))
			throw new IllegalStateException("To class not included: Cannot create ArticleContainerLink with type '" + getOrganisationID() + '/' + getArticleContainerLinkTypeID() + "' for this 'to': " + articleContainerLink.getTo());

		ArticleContainerLinkType reverseArticleContainerLinkType = getReverseArticleContainerLinkType();
		ArticleContainer reverseTo = articleContainerLink.getFrom();
		ArticleContainer reverseFrom = articleContainerLink.getTo();

		Collection<? extends ArticleContainerLink> reverseRelations = ArticleContainerLink.getArticleContainerLinks(
				pm,
				reverseArticleContainerLinkType,
				reverseFrom,
				reverseTo
		);

		// there should only exist 0 or 1 reverse relation for the reverseArticleContainerLinkType
		ArticleContainerLink reverseArticleContainerLink = null;
		for (ArticleContainerLink r : reverseRelations) {
			if (JDOHelper.isDeleted(r))
				continue;

			if (reverseArticleContainerLink != null)
				throw new IllegalStateException("There should be only one reverse relation with this type! r1=" + reverseArticleContainerLink + " r2=" + r);

			reverseArticleContainerLink = r;
		}

		if (reverseArticleContainerLink == null) {
			reverseArticleContainerLink = reverseArticleContainerLinkType.createArticleContainerLink(
					reverseFrom,
					reverseTo
			);
		}
	}

	public ArticleContainerLink createArticleContainerLink(ArticleContainer from, ArticleContainer to)
	{
		ArticleContainerLink articleContainerLink = new ArticleContainerLink(this, from, to);

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm != null)
			articleContainerLink = pm.makePersistent(articleContainerLink);

		return articleContainerLink;
	}

	public void preArticleContainerLinkDelete(ArticleContainerLink articleContainerLink)
	{
		PersistenceManager pm = getPersistenceManager();

		ArticleContainerLinkType reverseArticleContainerLinkType = getReverseArticleContainerLinkType();
		ArticleContainer reverseTo = articleContainerLink.getFrom();
		ArticleContainer reverseFrom = articleContainerLink.getTo();

		Collection<? extends ArticleContainerLink> reverseRelations = ArticleContainerLink.getArticleContainerLinks(
				pm,
				reverseArticleContainerLinkType,
				reverseFrom,
				reverseTo
		);

		for (ArticleContainerLink r : reverseRelations) {
			if (JDOHelper.isDeleted(r))
				continue;

			pm.deletePersistent(r);
		}
	}

	@Override
	public void jdoPreDelete() {
		NLJDOHelper.deleteAfterPrimaryObjectDeleted(this, name, description);
	}
}
