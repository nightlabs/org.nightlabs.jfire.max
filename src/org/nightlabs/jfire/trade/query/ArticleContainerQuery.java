package org.nightlabs.jfire.trade.query;

import java.util.Date;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author marco schulze - marco at nightlabs dot de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 * 
 * @deprecated this should not be used anymore see {@link AbstractArticleContainerQuickSearchQuery}! (marius)
 */
@Deprecated
public class ArticleContainerQuery<R extends ArticleContainer>
	extends AbstractJDOQuery<R>
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ArticleContainerQuery.class);
	
	public ArticleContainerQuery(Class articleContainerClass)
	{
		if (articleContainerClass == null)
			throw new IllegalArgumentException("Param articleContainerClass must not be null");
		
		if (!ArticleContainer.class.isAssignableFrom(articleContainerClass))
			throw new IllegalArgumentException("Param articleContainerClass must implement the interface "+ArticleContainer.class);

		if (Offer.class.isAssignableFrom(articleContainerClass) && !(this instanceof OfferQuery))
			throw new IllegalStateException("Instantiate an instance of OfferQuery instead!");
	}
	
	@Override
	protected Query prepareQuery()
	{
		Query q = getPersistenceManager().newQuery(getResultType());
		StringBuffer filter = new StringBuffer();
		
		filter.append(" true");
		
		if (articleCountMin >= 0)
			filter.append("\n && :articleCountMin < this.articles.size()");
		
		if (articleCountMax >= 0)
			filter.append("\n && :articleCountMax > this.articles.size()");

		if (createDTMin != null)
			filter.append("\n && this.createDT >= :createDTMin");

		if (createDTMax != null)
			filter.append("\n && this.createDT <= :createDTMax");
		
		if (createUserID != null)
		{
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//		filter.append("\n && JDOHelper.getObjectId(this.createUser) == :createUserID");
		// WORKAROUND:
		filter.append("\n && (" +
				"this.createUser.organisationID == \""+createUserID.organisationID+"\" && " +
				"this.createUser.userID == \""+createUserID.userID+"\"" +
						")");
		}
	  // own to method to allow override for Offer where it is different
		checkVendor(filter);
		checkCustomer(filter);
		
//		if (currency != null)
//			filter.append("\n && this.price.currency == :currency");
//
//		if (priceAmountMin >= 0)
//			filter.append("\n && this.price.amount >= :priceAmountMin");
//
//		if (priceAmountMax >= 0)
//			filter.append("\n && this.price.amount <= :priceAmountMax");
		
		logger.debug("filter = "+filter);
		
		q.setFilter(filter.toString());
		
		return q;
	}

	protected void checkVendor(StringBuffer filter) {
		if (vendorID != null)
		{
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("\n && JDOHelper.getObjectId(this.vendor) == :vendorID");
			// WORKAROUND:
			filter.append("\n && (" +
					"this.vendor.organisationID == \""+vendorID.organisationID+"\" && " +
					"this.vendor.anchorTypeID == \""+vendorID.anchorTypeID+"\" && " +
					"this.vendor.anchorID == \""+vendorID.anchorID+"\"" +
							")");
		}	
	}

	// own to method to allow override for Offer where it is different
	protected void checkCustomer(StringBuffer filter) {
		if (getCustomerID() != null)
		{
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("\n && JDOHelper.getObjectId(this.customer) == :customerID");
			// WORKAROUND:
			filter.append("\n && (" +
					"this.customer.organisationID == \""+customerID.organisationID+"\" && " +
					"this.customer.anchorTypeID == \""+customerID.anchorTypeID+"\" && " +
					"this.customer.anchorID == \""+customerID.anchorID+"\"" +
							")");
		}
	}
	
	private int articleCountMin = -1;
	public int getArticleCountMin() {
		return articleCountMin;
	}
	public void setArticleCountMin(int articleCountMin) {
		this.articleCountMin = articleCountMin;
	}
	
	private int articleCountMax = -1;
	public int getArticleCountMax() {
		return articleCountMax;
	}
	public void setArticleCountMax(int articleCountMax) {
		this.articleCountMax = articleCountMax;
	}
	
	private Date createDTMin = null;
	public Date getCreateDTMin() {
		return createDTMin;
	}
	public void setCreateDTMin(Date createDTMin) {
		this.createDTMin = createDTMin;
	}
	
	private Date createDTMax = null;
	public Date getCreateDTMax() {
		return createDTMax;
	}
	public void setCreateDTMax(Date createDTMax) {
		this.createDTMax = createDTMax;
	}
	
	private UserID createUserID = null;
	public UserID getCreateUserID() {
		return createUserID;
	}
	public void setCreateUserID(UserID createUserID) {
		this.createUserID = createUserID;
	}
	
	private AnchorID vendorID = null;
	public AnchorID getVendorID() {
		return vendorID;
	}
	public void setVendorID(AnchorID vendorID) {
		this.vendorID = vendorID;
	}
	
	private AnchorID customerID = null;
	public AnchorID getCustomerID() {
		return customerID;
	}
	public void setCustomerID(AnchorID customerID) {
		this.customerID = customerID;
	}

	@Override
	protected Class<R> init()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
//	private Currency currency = null;
//	public Currency getCurrency() {
//		return currency;
//	}
//	public void setCurrency(Currency currency) {
//		this.currency = currency;
//	}
//
//	private long priceAmountMin = -1;
//	public long getPriceAmountMin() {
//		return priceAmountMin;
//	}
//	public void setPriceAmountMin(long priceAmountMin) {
//		this.priceAmountMin = priceAmountMin;
//	}
//
//	private long priceAmountMax = -1;
//	public long getPriceAmountMax() {
//		return priceAmountMax;
//	}
//	public void setPriceAmountMax(long priceAmountMax) {
//		this.priceAmountMax = priceAmountMax;
//	}
}
