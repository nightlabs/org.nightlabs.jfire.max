package org.nightlabs.jfire.trade.query;

import java.util.Date;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * {@link AbstractJDOQuery} implementation which can be used together with
 * {@link AbstractArticleContainerQuickSearchEntry}s
 * 
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractArticleContainerQuery<R extends ArticleContainer>
	extends AbstractJDOQuery<R>
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AbstractArticleContainerQuery.class);
	
	// Property IDs used for the PropertyChangeListeners
	private static final String PROPERTY_PREFIX = "AbstractArticleContainerQuery.";
	public static final String PROPERTY_ARTICLE_COUNT_MAX = PROPERTY_PREFIX + "articleCountMax";
	public static final String PROPERTY_ARTICLE_COUNT_MIN = PROPERTY_PREFIX + "articleCountMin";
	public static final String PROPERTY_ARTICLE_CONTAINER_ID = PROPERTY_PREFIX + "articleContainerID";
	public static final String PROPERTY_CREATE_DATE_MAX = PROPERTY_PREFIX + "createDTMax";
	public static final String PROPERTY_CREATE_DATE_MIN = PROPERTY_PREFIX + "createDTMin";
	public static final String PROPERTY_CREATE_USER_ID = PROPERTY_PREFIX + "createUserID";
	public static final String PROPERTY_CREATOR_NAME = PROPERTY_PREFIX + "creatorName";
	public static final String PROPERTY_CUSTOMER_ID = PROPERTY_PREFIX + "customerID";
	public static final String PROPERTY_CUSTOMER_NAME = PROPERTY_PREFIX + "customerName";
	public static final String PROPERTY_VENDOR_ID = PROPERTY_PREFIX + "vendorID";
	public static final String PROPERTY_VENDOR_NAME = PROPERTY_PREFIX + "vendorName";
	
	@Override
	protected Query prepareQuery()
	{
		Query q = getPersistenceManager().newQuery(getResultType());
		StringBuffer filter = new StringBuffer();
		
		filter.append("true");
		
		// own methods to allow override e.g. for Offer where for customerName it is different
		checkCustomerName(filter);
		checkVendorName(filter);
		checkArticleContainerID(filter);
		
		// check creation time and counts
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
		
		// append filter for the additional fields of the implementing class.
		checkAdditionalFields(filter);
		q.setFilter(filter.toString());
//		q.setRange(rangeFromIncl, rangeToExcl);
		
		if (logger.isDebugEnabled())
			logger.debug("query = "+filter);
		
		return q;
	}

	/**
	 * Here you should check the fields of the subclass of ArticleContainer.
	 * @param filter the already constructed filter consisting of the vendor, customer and article id
	 * 	check.
	 */
	protected abstract void checkAdditionalFields(StringBuffer filter);

	protected void checkVendorName(StringBuffer filter)
	{
		if (getVendorName() != null)
			filter.append("\n && (this.vendor.person.displayName.toLowerCase().indexOf(\""+vendorName.toLowerCase()+"\") >= 0)");
	}
	
	protected void checkCustomerName(StringBuffer filter)
	{
		if (getCustomerName() != null)
			filter.append("\n && (this.customer.person.displayName.toLowerCase().indexOf(\""+customerName.toLowerCase()+"\") >= 0)");
	}
	
	protected void checkCreatorName(StringBuilder filter)
	{
		if (creatorName != null)
		{
			filter.append("\n && (this.createUser.person.displayName.toLowerCase().indexOf(\""+creatorName.toLowerCase()+"\") >= 0)");
		}		
	}
	
	protected void checkArticleContainerID(StringBuffer filter)
	{
		if (getArticleContainerID() != null && !getArticleContainerID().trim().equals(""))
//			filter.append("\n && (this."+getArticleContainerIDMemberName()+" == \""+ObjectIDUtil.parseLongObjectIDField(articleContainerID)+"\"");
			filter.append("\n && (this."+getArticleContainerIDMemberName()+" == "+ObjectIDUtil.parseLongObjectIDField(articleContainerID)+")");
	}
	
	/**
	 * Crops all elements from given vendor anchor. 
	 * @param filter the filter to write the query into.
	 */
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
	/**
	 * Crops all elements from given customer anchor. 
	 * @param filter the filter to write the query into.
	 */
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
	
	private String creatorName = null;
	/**
	 * @return the creatorName
	 */
	public String getCreatorName()
	{
		return creatorName;
	}

	/**
	 * @param creatorName the creatorName to set
	 */
	public void setCreatorName(String creatorName)
	{
		String oldCreatorName = this.creatorName;
		this.creatorName = creatorName;
		notifyListeners(PROPERTY_CREATOR_NAME, oldCreatorName, creatorName);
	}

	private String customerName;
	/**
	 * returns the customerName
	 * @return the customerName
	 */
	public String getCustomerName() {
		return customerName;
	}

	/**
	 * sets the customerName
	 * @param customerName the customerName to set
	 */
	public void setCustomerName(String customerName)
	{
		final String oldCustomerName = this.customerName;
		this.customerName = customerName;
		notifyListeners(PROPERTY_CUSTOMER_NAME, oldCustomerName, customerName);
	}
	
	private String vendorName;
	/**
	 * returns the vendorName
	 * @return the vendorName
	 */
	public String getVendorName() {
		return vendorName;
	}

	/**
	 * sets the vendorName
	 * @param vendorName the vendorName to set
	 */
	public void setVendorName(String vendorName)
	{
		final String oldVendorName = this.vendorName;
		this.vendorName = vendorName;
		notifyListeners(PROPERTY_VENDOR_NAME, oldVendorName, vendorName);
	}
	
	private String articleContainerID;
	/**
	 * returns the articleContainerID
	 * @return the articleContainerID
	 */
	public String getArticleContainerID() {
		return articleContainerID;
	}

	/**
	 * sets the articleContainerID
	 * @param articleContainerID the articleContainerID to set
	 */
	public void setArticleContainerID(String articleContainerID) {
		String oldID = this.articleContainerID;
		this.articleContainerID = articleContainerID;
		notifyListeners(PROPERTY_ARTICLE_CONTAINER_ID, oldID, articleContainerID);
	}
	
	private int articleCountMin = -1;
	public int getArticleCountMin() {
		return articleCountMin;
	}
	public void setArticleCountMin(int articleCountMin)
	{
		int oldArticleCountMin = this.articleCountMin;
		this.articleCountMin = articleCountMin;
		notifyListeners(PROPERTY_ARTICLE_COUNT_MIN, oldArticleCountMin, articleCountMin);
	}
	
	private int articleCountMax = -1;
	public int getArticleCountMax() {
		return articleCountMax;
	}
	public void setArticleCountMax(int articleCountMax)
	{
		int oldCountMax = this.articleCountMax;
		this.articleCountMax = articleCountMax;
		notifyListeners(PROPERTY_ARTICLE_COUNT_MAX, oldCountMax, articleCountMax);
	}
	
	private Date createDTMin = null;
	public Date getCreateDTMin() {
		return createDTMin;
	}
	public void setCreateDTMin(Date createDTMin)
	{
		final Date oldCreateDTMin = this.createDTMin;
		this.createDTMin = createDTMin;
		notifyListeners(PROPERTY_CREATE_DATE_MIN, oldCreateDTMin, createDTMin);
	}
	
	private Date createDTMax = null;
	public Date getCreateDTMax() {
		return createDTMax;
	}
	public void setCreateDTMax(Date createDTMax)
	{
		final Date oldCreateDTMax = this.createDTMax; 
		this.createDTMax = createDTMax;
		notifyListeners(PROPERTY_CREATE_DATE_MAX, oldCreateDTMax, createDTMax);
	}
	
	private UserID createUserID = null;
	public UserID getCreateUserID() {
		return createUserID;
	}
	public void setCreateUserID(UserID createUserID)
	{
		final UserID oldCreateUserID = this.createUserID;
		this.createUserID = createUserID;
		notifyListeners(PROPERTY_CREATE_USER_ID, oldCreateUserID, createUserID);
	}
	
	private AnchorID vendorID = null;
	public AnchorID getVendorID() {
		return vendorID;
	}
	public void setVendorID(AnchorID vendorID)
	{
		final AnchorID oldVendorID = this.vendorID;
		this.vendorID = vendorID;
		notifyListeners(PROPERTY_VENDOR_ID, oldVendorID, vendorID);
	}
	
	private AnchorID customerID = null;
	public AnchorID getCustomerID() {
		return customerID;
	}
	public void setCustomerID(AnchorID customerID)
	{
		final AnchorID oldCustomerID = this.customerID;
		this.customerID = customerID;
		notifyListeners(PROPERTY_CUSTOMER_ID, oldCustomerID, customerID);
	}
	
//	/**
//	 * returns the class of the articleContainer
//	 * @return the class of the articleContainer
//	 */
//	public abstract Class<R> getArticleContainerClass();
	
	/**
	 * returns the name of the articleContainerID member
	 * 
	 * @return the name of the member which defines the value
	 * which is returned by {@link ArticleContainer#getArticleContainerID()}
	 */
	public abstract String getArticleContainerIDMemberName();
	
	/**
	 * returns the name of the articleContainerIDPrefix member
	 * 
	 * @return the name of the member which defines the value
	 * which is returned by {@link ArticleContainer#getArticleContainerIDPrefix()}
	 */
	public abstract String getArticleContainerIDPrefixMemberName();

//	/**
//	 * the beginning of the range to include from the result
//	 * by default 0
//	 */
//	private long rangeFromIncl = 0;
//	public long getRangeFromIncl(){
//		return rangeFromIncl;
//	}
//	public void setRangeFromIncl(long rangeFromIncl) {
//		this.rangeFromIncl = rangeFromIncl;
//	}
//
//	/**
//	 * the end of the range to exclude from the result
//	 * by default {@link Long#MAX_VALUE}
//	 */
//	private long rangeToExcl = Long.MAX_VALUE;
//	public long getRangeToExcl() {
//		return rangeToExcl;
//	}
//	public void setRangeToExcl(long rangeToExcl) {
//		this.rangeToExcl = rangeToExcl;
//	}
}
