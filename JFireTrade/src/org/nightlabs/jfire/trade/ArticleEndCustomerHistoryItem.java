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
import org.nightlabs.jfire.trade.id.ArticleEndCustomerHistoryItemID;

@PersistenceCapable(
		objectIdClass=ArticleEndCustomerHistoryItemID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_ArticleEndCustomerHistoryItem"
)
@FetchGroups({
	@FetchGroup(
			name=ArticleEndCustomerHistoryItem.FETCH_GROUP_ARTICLE,
			members=@Persistent(name=ArticleEndCustomerHistoryItem.FieldName.article)
	),
	@FetchGroup(
			name=ArticleEndCustomerHistoryItem.FETCH_GROUP_OLD_END_CUSTOMER,
			members=@Persistent(name=ArticleEndCustomerHistoryItem.FieldName.oldEndCustomer)
	),
	@FetchGroup(
			name=ArticleEndCustomerHistoryItem.FETCH_GROUP_NEW_END_CUSTOMER,
			members=@Persistent(name=ArticleEndCustomerHistoryItem.FieldName.newEndCustomer)
	),
	@FetchGroup(
			name=ArticleEndCustomerHistoryItem.FETCH_GROUP_USER,
			members=@Persistent(name=ArticleEndCustomerHistoryItem.FieldName.user)
	)
})
@Queries({
		@Query(
				name="getArticleEndCustomerHistoryItemsForArticle",
				value="SELECT WHERE this.article == :article"
		)
})
public class ArticleEndCustomerHistoryItem
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final class FieldName {
		public static final String article = "article";
		public static final String oldEndCustomer = "oldEndCustomer";
		public static final String newEndCustomer = "newEndCustomer";
		public static final String user = "user";
	}

	public static final String FETCH_GROUP_ARTICLE = "ArticleEndCustomerHistoryItem.article";
	public static final String FETCH_GROUP_OLD_END_CUSTOMER = "ArticleEndCustomerHistoryItem.oldEndCustomer";
	public static final String FETCH_GROUP_NEW_END_CUSTOMER = "ArticleEndCustomerHistoryItem.newEndCustomer";
	public static final String FETCH_GROUP_USER = "ArticleEndCustomerHistoryItem.user";

	public static Collection<? extends ArticleEndCustomerHistoryItem> getArticleContainerEndCustomerHistoryItems(PersistenceManager pm, Article article)
	{
		javax.jdo.Query q = pm.newNamedQuery(ArticleEndCustomerHistoryItem.class, "getArticleEndCustomerHistoryItemsForArticle");
		@SuppressWarnings("unchecked")
		Collection<? extends ArticleEndCustomerHistoryItem> c = (Collection<? extends ArticleEndCustomerHistoryItem>) q.execute(article);
		return c;
	}

	@PrimaryKey
	private String organisationID;

	@PrimaryKey
	private long articleEndCustomerHistoryItemID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private Article article;

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
	protected ArticleEndCustomerHistoryItem() { }

	public ArticleEndCustomerHistoryItem(
			String organisationID, long articleEndCustomerHistoryItemID,
			Article article, LegalEntity oldEndCustomer, LegalEntity newEndCustomer, User user
	)
	{
		if (article == null)
			throw new IllegalArgumentException("article must not be null!");

		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		this.organisationID = organisationID;
		this.articleEndCustomerHistoryItemID = articleEndCustomerHistoryItemID;
		this.article = article;
		this.oldEndCustomer = oldEndCustomer;
		this.newEndCustomer = newEndCustomer;
		this.timestamp = new Date();
		this.user = user;
	}

	public ArticleEndCustomerHistoryItem(Article article, LegalEntity oldEndCustomer, LegalEntity newEndCustomer, User user) {
		this(
				IDGenerator.getOrganisationID(), IDGenerator.nextID(ArticleEndCustomerHistoryItem.class),
				article, oldEndCustomer, newEndCustomer, user
		);
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public long getArticleEndCustomerHistoryItemID() {
		return articleEndCustomerHistoryItemID;
	}

	public Article getArticle() {
		return article;
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
