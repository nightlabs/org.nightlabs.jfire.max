package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.id.ArticleContainerEndCustomerHistoryItemID;

@PersistenceCapable(
		objectIdClass=ArticleContainerEndCustomerHistoryItemID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_ArticleContainerEndCustomerHistoryItem"
)
@FetchGroups({
	@FetchGroup(
			name=ArticleContainerEndCustomerHistoryItem.FETCH_GROUP_ARTICLE_CONTAINER,
			members=@Persistent(name=ArticleContainerEndCustomerHistoryItem.FieldName.articleContainer)
	),
	@FetchGroup(
			name=ArticleContainerEndCustomerHistoryItem.FETCH_GROUP_OLD_END_CUSTOMER,
			members=@Persistent(name=ArticleContainerEndCustomerHistoryItem.FieldName.oldEndCustomer)
	),
	@FetchGroup(
			name=ArticleContainerEndCustomerHistoryItem.FETCH_GROUP_NEW_END_CUSTOMER,
			members=@Persistent(name=ArticleContainerEndCustomerHistoryItem.FieldName.newEndCustomer)
	),
	@FetchGroup(
			name=ArticleContainerEndCustomerHistoryItem.FETCH_GROUP_USER,
			members=@Persistent(name=ArticleContainerEndCustomerHistoryItem.FieldName.user)
	),
})
@Queries({
		@Query(
				name="getArticleContainerEndCustomerHistoryItemsForArticleContainer",
				value="SELECT WHERE this.articleContainer == :articleContainer"
		)
})
public class ArticleContainerEndCustomerHistoryItem
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final class FieldName {
		public static final String articleContainer = "articleContainer";
		public static final String oldEndCustomer = "oldEndCustomer";
		public static final String newEndCustomer = "newEndCustomer";
		public static final String user = "user";
	}

	public static final String FETCH_GROUP_ARTICLE_CONTAINER = "ArticleContainerEndCustomerHistoryItem.articleContainer";
	public static final String FETCH_GROUP_OLD_END_CUSTOMER = "ArticleContainerEndCustomerHistoryItem.oldEndCustomer";
	public static final String FETCH_GROUP_NEW_END_CUSTOMER = "ArticleContainerEndCustomerHistoryItem.newEndCustomer";
	public static final String FETCH_GROUP_USER = "ArticleContainerEndCustomerHistoryItem.user";

	public static Collection<? extends ArticleContainerEndCustomerHistoryItem> getArticleContainerEndCustomerHistoryItems(PersistenceManager pm, ArticleContainer articleContainer)
	{
		javax.jdo.Query q = pm.newNamedQuery(ArticleContainerEndCustomerHistoryItem.class, "getArticleContainerEndCustomerHistoryItemsForArticleContainer");
		@SuppressWarnings("unchecked")
		Collection<? extends ArticleContainerEndCustomerHistoryItem> c = (Collection<? extends ArticleContainerEndCustomerHistoryItem>) q.execute(articleContainer);
		return c;
	}

	@PrimaryKey
	private String organisationID;

	@PrimaryKey
	private long articleContainerEndCustomerHistoryItemID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ArticleContainer articleContainer;

	@Persistent
	private LegalEntity oldEndCustomer;

	@Persistent
	private LegalEntity newEndCustomer;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Date timestamp;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private User user;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ArticleContainerEndCustomerHistoryItem() { }

	public ArticleContainerEndCustomerHistoryItem(
			String organisationID, long articleContainerEndCustomerHistoryItemID,
			ArticleContainer articleContainer, LegalEntity oldEndCustomer, LegalEntity newEndCustomer, User user
	)
	{
		if (articleContainer == null)
			throw new IllegalArgumentException("articleContainer must not be null!");

		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		this.organisationID = organisationID;
		this.articleContainerEndCustomerHistoryItemID = articleContainerEndCustomerHistoryItemID;
		this.articleContainer = articleContainer;
		this.oldEndCustomer = oldEndCustomer;
		this.newEndCustomer = newEndCustomer;
		this.timestamp = new Date();
		this.user = user;
	}

	public ArticleContainerEndCustomerHistoryItem(ArticleContainer articleContainer, LegalEntity oldEndCustomer, LegalEntity newEndCustomer, User user) {
		this(
				IDGenerator.getOrganisationID(), IDGenerator.nextID(ArticleContainerEndCustomerHistoryItem.class),
				articleContainer, oldEndCustomer, newEndCustomer, user
		);
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public long getArticleContainerEndCustomerHistoryItemID() {
		return articleContainerEndCustomerHistoryItemID;
	}

	public ArticleContainer getArticleContainer() {
		return articleContainer;
	}

	public LegalEntity getOldEndCustomer() {
		return oldEndCustomer;
	}

	public LegalEntity getNewEndCustomer() {
		return newEndCustomer;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public User getUser() {
		return user;
	}
}
