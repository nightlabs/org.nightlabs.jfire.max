package org.nightlabs.jfire.trade.link;

import java.io.Serializable;
import java.util.ArrayList;
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
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreCallback;
import javax.jdo.listener.StoreLifecycleListener;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkID;
import org.nightlabs.util.Util;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(
		objectIdClass=ArticleContainerLinkID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_ArticleContainerLink"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
		@FetchGroup(
				name=ArticleContainerLink.FETCH_GROUP_ARTICLE_CONTAINER_LINK_TYPE,
				members={@Persistent(name="articleContainerLinkType")}
		),
		@FetchGroup(
				name=ArticleContainerLink.FETCH_GROUP_FROM,
				members={@Persistent(name="from")}
		),
		@FetchGroup(
				name=ArticleContainerLink.FETCH_GROUP_TO,
				members={@Persistent(name="to")}
		)
})
public class ArticleContainerLink
implements Serializable, AttachCallback, DetachCallback, StoreCallback, DeleteCallback
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ArticleContainerLink.class);

	public static final String FETCH_GROUP_ARTICLE_CONTAINER_LINK_TYPE = "ArticleContainerLink.articleContainerLinkType";
	public static final String FETCH_GROUP_FROM = "ArticleContainerLink.from";
	public static final String FETCH_GROUP_TO = "ArticleContainerLink.to";
	public static final String FETCH_GROUP_FROM_ID = "ArticleContainerLink.fromID";
	public static final String FETCH_GROUP_TO_ID = "ArticleContainerLink.toID";

	private static Query createArticleContainerLinkQuery(PersistenceManager pm, ArticleContainerLinkType articleContainerLinkType, ArticleContainer fromArticleContainer, ArticleContainer toArticleContainer)
	{
		Query q = pm.newQuery(ArticleContainerLink.class);

		StringBuilder filter = new StringBuilder();
		if (articleContainerLinkType != null)
			filter.append("this.articleContainerLinkType == :articleContainerLinkType");

		if (fromArticleContainer != null) {
			if (filter.length() > 0)
				filter.append(" && ");

			filter.append("this.from == :fromArticleContainer");
		}

		if (toArticleContainer != null) {
			if (filter.length() > 0)
				filter.append(" && ");

			filter.append("this.to == :toArticleContainer");
		}
		q.setFilter(filter.toString());

		return q;
	}

	public static long getArticleContainerLinkCount(PersistenceManager pm, ArticleContainerLinkType articleContainerLinkType, ArticleContainer fromArticleContainer, ArticleContainer toArticleContainer)
	{
		Query q = createArticleContainerLinkQuery(pm, articleContainerLinkType, fromArticleContainer, toArticleContainer);
		q.setResult("count(this)");

		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("articleContainerLinkType", articleContainerLinkType);
		params.put("fromArticleContainer", fromArticleContainer);
		params.put("toArticleContainer", toArticleContainer);

		return (Long) q.executeWithMap(params);
	}

	public static Collection<? extends ArticleContainerLink> getArticleContainerLinks(PersistenceManager pm, ArticleContainerLinkType articleContainerLinkType, ArticleContainer fromArticleContainer, ArticleContainer toArticleContainer)
	{
		Query q = createArticleContainerLinkQuery(pm, articleContainerLinkType, fromArticleContainer, toArticleContainer);

		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("articleContainerLinkType", articleContainerLinkType);
		params.put("fromArticleContainer", fromArticleContainer);
		params.put("toArticleContainer", toArticleContainer);

		@SuppressWarnings("unchecked")
		Collection<? extends ArticleContainerLink> c = (Collection<? extends ArticleContainerLink>) q.executeWithMap(params);

		// TODO DATANUCLEUS WORKAROUND BEGIN
		ArrayList<ArticleContainerLink> l = new ArrayList<ArticleContainerLink>(c.size());
		for (ArticleContainerLink articleContainerLink : c) {
			if (articleContainerLinkType != null && !articleContainerLinkType.equals(articleContainerLink.getArticleContainerLinkType())) {
				logger.warn("getArticleContainerLinks: JDO query found objects that should not have been found! ArticleContainerLinkType mismatch: " + articleContainerLink + " (expected '" + articleContainerLinkType + "' but found '" + articleContainerLink.getArticleContainerLinkType() + "')");
				continue;
			}
			if (fromArticleContainer != null && !fromArticleContainer.equals(articleContainerLink.getFrom())) {
				logger.warn("getArticleContainerLinks: JDO query found objects that should not have been found! fromArticleContainer mismatch: " + articleContainerLink + " (expected '" + fromArticleContainer + "' but found '" + articleContainerLink.getFrom() + "')");
				continue;
			}
			if (toArticleContainer != null && !toArticleContainer.equals(articleContainerLink.getTo())) {
				logger.warn("getArticleContainerLinks: JDO query found objects that should not have been found! toArticleContainer mismatch: " + articleContainerLink + " (expected '" + toArticleContainer + "' but found '" + articleContainerLink.getTo() + "')");
				continue;
			}

			l.add(articleContainerLink);
		}
		c = l;
		// TODO DATANUCLEUS WORKAROUND END

		return c;
	}


	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long articleContainerLinkID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ArticleContainerLinkType articleContainerLinkType;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ArticleContainer from;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ArticleContainer to;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private ArticleContainerID fromID;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private ArticleContainerID toID;

	protected ArticleContainerLink() { }

	public ArticleContainerLink(
			ArticleContainerLinkType articleContainerLinkType,
			ArticleContainer from, ArticleContainer to
	)
	{
		this(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(ArticleContainerLink.class),
				articleContainerLinkType,
				from,
				to
		);
	}

	public ArticleContainerLink(
			String organisationID, long articleContainerLinkID,
			ArticleContainerLinkType articleContainerLinkType,
			ArticleContainer from, ArticleContainer to
	)
	{
		Organisation.assertValidOrganisationID(organisationID);

		if (articleContainerLinkType == null)
			throw new IllegalArgumentException("articleContainerLinkType must not be null!");

		this.organisationID = organisationID;
		this.articleContainerLinkID = articleContainerLinkID;
		this.articleContainerLinkType = articleContainerLinkType;
		this.from = from;
		this.to = to;
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public long getArticleContainerLinkID() {
		return articleContainerLinkID;
	}

	public ArticleContainerLinkType getArticleContainerLinkType() {
		return articleContainerLinkType;
	}

	public ArticleContainer getFrom() {
		return from;
	}

	public ArticleContainer getTo() {
		return to;
	}


	public ArticleContainerID getFromID() {
		if (fromID == null)
			fromID = (ArticleContainerID) JDOHelper.getObjectId(from);

		return fromID;
	}
	public ArticleContainerID getToID() {
		if (toID == null)
			toID = (ArticleContainerID) JDOHelper.getObjectId(to);

		return toID;
	}


	@Override
	public void jdoPostDetach(Object o) {
		ArticleContainerLink detached = this;
		ArticleContainerLink attached = (ArticleContainerLink) o;

		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		Set<?> fetchGroups = pm.getFetchPlan().getGroups();
		if (fetchGroups.contains(FETCH_GROUP_FROM_ID)) {
			detached.fromID = attached.getFromID();
		}
		if (fetchGroups.contains(FETCH_GROUP_TO_ID)) {
			detached.toID = attached.getToID();
		}
	}

	@Override
	public void jdoPreDetach() { }

	@Override
	public void jdoPostAttach(Object o) { }

	@Override
	public void jdoPreAttach() { }

	@Override
	public void jdoPreStore() {
		final PersistenceManager pm = JDOHelper.getPersistenceManager(this);

		// Prevent duplicate relations. A relation is considered duplicate, if it
		// is between the same two objects *AND* in the same direction *AND* of the
		// same type (ArticleContainerLinkType).
		Collection<? extends ArticleContainerLink> relations = ArticleContainerLink.getArticleContainerLinks(
				pm,
				getArticleContainerLinkType(),
				getFrom(),
				getTo()
		);
		for (ArticleContainerLink r : relations) {
			if (ArticleContainerLink.this.equals(r))
				continue;

			if (JDOHelper.isDeleted(r))
				continue;

			throw new DuplicateArticleContainerLinkException(r);
		}

		pm.addInstanceLifecycleListener(
				new StoreLifecycleListener() {
					@Override
					public void preStore(InstanceLifecycleEvent event) { }

					@Override
					public void postStore(InstanceLifecycleEvent event) {
						ArticleContainerLink articleContainerLink = (ArticleContainerLink) event.getPersistentInstance();
						if (!ArticleContainerLink.this.equals(articleContainerLink))
							return;

						pm.removeInstanceLifecycleListener(this);
						getArticleContainerLinkType().postArticleContainerLinkCreated(articleContainerLink);
					}
				},
				ArticleContainerLink.class
		);
	}

	/**
	 * This flag is a guard against eternal recursion of jdoPreDelete() methods, because two reverse
	 * relations try to delete each other forever.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient boolean isDeleting = false;

	@Override
	public void jdoPreDelete() {
		if (isDeleting)
			return;

		isDeleting = true;

		articleContainerLinkType.preArticleContainerLinkDelete(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (articleContainerLinkID ^ (articleContainerLinkID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		ArticleContainerLink other = (ArticleContainerLink) obj;
		return (
				Util.equals(this.articleContainerLinkID, other.articleContainerLinkID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(articleContainerLinkID) + ']';
	}
}
