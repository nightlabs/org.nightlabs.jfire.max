package org.nightlabs.jfire.trade.query;

import java.util.Date;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * {@link AbstractJDOQuery} implementation which can be used to send simple queries to the database
 * retrieving all kinds of ArticleContainers.
 *
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class AbstractArticleContainerQuery
	extends AbstractJDOQuery
{
	private static final long serialVersionUID = 20080811L;
	private static final Logger logger = Logger.getLogger(AbstractArticleContainerQuery.class);

	/**
	 * Static final class containing all field names of this query and which can be used to create the
	 * identifier for the enabled-flags of the corresponding fields via
	 * {@link #getEnabledFieldName(String)}.
	 */
	public static final class FieldName
	{
		public static final String articleCountMax = "articleCountMax";
		public static final String articleCountMin = "articleCountMin";
		public static final String articleContainerID = "articleContainerID";
		public static final String createDTMax = "createDTMax";
		public static final String createDTMin = "createDTMin";
		public static final String createUserID = "createUserID";
		public static final String creatorNameRegex = "creatorName";
		public static final String creatorName = "creatorName";
		public static final String customerID = "customerID";
		public static final String customerNameRegex = "creatorName";
		public static final String customerName = "customerName";
		public static final String vendorID = "vendorID";
		public static final String vendorNameRegex = "creatorName";
		public static final String vendorName = "vendorName";
		public static final String productID = "productID";
		public static final String productTypeID = "productTypeID";
	}

	@Override
	protected void prepareQuery(Query q)
	{
		StringBuilder filter = getFilter();

		filter.append("true");

		// own methods to allow override e.g. for Offer where for customerName it is different
		checkCustomerName(filter);
		checkVendorName(filter);
		checkArticleContainerID(filter);

		// check creation time and counts
		if (isFieldEnabled(FieldName.articleCountMin) && articleCountMin >= 0)
			filter.append("\n && :articleCountMin < this.articles.size()");

		if (isFieldEnabled(FieldName.articleCountMax) && articleCountMax >= 0)
			filter.append("\n && :articleCountMax > this.articles.size()");

		if (isFieldEnabled(FieldName.createDTMin) && createDTMin != null)
			filter.append("\n && this.createDT >= :createDTMin");

		if (isFieldEnabled(FieldName.createDTMax) && createDTMax != null)
			filter.append("\n && this.createDT <= :createDTMax");

		if (isFieldEnabled(FieldName.createUserID) && createUserID != null)
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
		checkProductID(filter, getArticleContainerArticlesMemberName());
		checkProductTypeID(filter, getArticleContainerArticlesMemberName());

		// append filter for the additional fields of the implementing class.
		checkAdditionalFields(filter);
		q.setFilter(filter.toString());
		q.declareVariables(getVars());

		if (logger.isDebugEnabled())
			logger.debug("query = "+filter);
	}

	protected void checkProductTypeID(StringBuilder filter, String member)
	{
		if (isFieldEnabled(FieldName.productTypeID) && productTypeID != null) {
			String varName = member+"Var";
			addVariable(Article.class, varName);
			filter.append(" && \n (" +
					"  this."+member+".contains("+varName+")" +
					"  && JDOHelper.getObjectId("+varName+".productType) == :productTypeID" +
					" )");
		}
	}

	/**
	 * Here you should check the fields of the subclass of ArticleContainer.
	 * @param filter the already constructed filter consisting of the vendor, customer and article id
	 * 	check.
	 */
	protected abstract void checkAdditionalFields(StringBuilder filter);

	protected void checkVendorName(StringBuilder filter)
	{
		if (isFieldEnabled(FieldName.vendorName) &&	getVendorName() != null) {
			vendorNameExpr = isVendorNameRegex() ? vendorName : ".*" + vendorName + ".*";
			filter.append("\n && (this.vendor.person.displayName.toLowerCase().matches(:vendorNameExpr.toLowerCase()))");
		}
	}

	protected void checkCustomerName(StringBuilder filter)
	{
		if (isFieldEnabled(FieldName.customerName) && getCustomerName() != null) {
			customerNameExpr = isCustomerNameRegex() ? customerName : ".*" + customerName + ".*";
			filter.append("\n && (this.customer.person.displayName.toLowerCase().matches(:customerNameExpr.toLowerCase()))");
		}
	}

	protected void checkCreatorName(StringBuilder filter)
	{
		if (isFieldEnabled(FieldName.creatorName) && creatorName != null) {
			creatorNameExpr = isCreatorNameRegex() ? creatorName : ".*" + creatorName + ".*";
			filter.append("\n && (this.customer.person.displayName.toLowerCase().matches(:creatorNameExpr.toLowerCase()))");
		}
	}

	protected void checkArticleContainerID(StringBuilder filter)
	{
		if (isFieldEnabled(FieldName.articleContainerID) &&
				getArticleContainerID() != null && !getArticleContainerID().trim().equals(""))
//			filter.append("\n && (this."+getArticleContainerIDMemberName()+" == \""+ObjectIDUtil.parseLongObjectIDField(articleContainerID)+"\"");
			filter.append("\n && (this."+getArticleContainerIDMemberName()+" == "+ObjectIDUtil.parseLongObjectIDField(articleContainerID)+")");
	}

	/**
	 * Crops all elements from given vendor anchor.
	 * @param filter the filter to write the query into.
	 */
	protected void checkVendor(StringBuilder filter) {
		if (isFieldEnabled(FieldName.vendorID) && vendorID != null)
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
	protected void checkCustomer(StringBuilder filter) {
		if (isFieldEnabled(FieldName.customerID) &&	getCustomerID() != null)
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

	protected void checkProductID(StringBuilder filter, String member)
	{
		if (isFieldEnabled(FieldName.productID) && productID != null) {
			String varName = member+"Var";
			addVariable(Article.class, varName);
			filter.append(" && \n (" +
					"  this."+member+".contains("+varName+")" +
					"  && JDOHelper.getObjectId("+varName+".product) == :productID" +
					" )");
		}
	}

	private boolean creatorNameRegex = false;
	@SuppressWarnings("unused")
	private transient String creatorNameExpr;
	private String creatorName = null;
	/**
	 * @return the creatorName
	 */
	public String getCreatorName()
	{
		return creatorName;
	}

	/**
	 * Sets the string to search in the display name of the creators person.
	 * @param creatorName the creatorName to set
	 */
	public void setCreatorName(String creatorName)
	{
		String oldCreatorName = this.creatorName;
		this.creatorName = creatorName;
		notifyListeners(FieldName.creatorName, oldCreatorName, creatorName);
	}

	/**
	 * @return Whether the value set with {@link #setCreatorName(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setCreatorName(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isCreatorNameRegex() {
		return creatorNameRegex;
	}

	/**
	 * Sets whether the value set with {@link #setCreatorName(String)} represents a
	 * regular expression.
	 *
	 * @param creatorNameRegex The creatorNameRegex to search.
	 */
	public void setCreatorNameRegex(boolean creatorNameRegex) {
		final boolean oldCreatorNameRegex = this.creatorNameRegex;
		this.creatorNameRegex = creatorNameRegex;
		notifyListeners(FieldName.creatorNameRegex, oldCreatorNameRegex, creatorNameRegex);
	}


	private boolean customerNameRegex = false;
	@SuppressWarnings("unused")
	private transient String customerNameExpr;
	private String customerName;
	/**
	 * returns the customerName
	 * @return the customerName
	 */
	public String getCustomerName() {
		return customerName;
	}

	/**
	 * Sets the string to search in the display name of the customers person.
	 * @param customerName the customerName to set
	 */
	public void setCustomerName(String customerName)
	{
		final String oldCustomerName = this.customerName;
		this.customerName = customerName;
		notifyListeners(FieldName.customerName, oldCustomerName, customerName);
	}

	/**
	 * @return Whether the value set with {@link #setCustomerName(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setCustomerName(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isCustomerNameRegex() {
		return customerNameRegex;
	}

	/**
	 * Sets whether the value set with {@link #setCustomerName(String)} represents a
	 * regular expression.
	 *
	 * @param customerNameRegex The customerNameRegex to search.
	 */
	public void setCustomerNameRegex(boolean customerNameRegex) {
		final boolean oldCustomerNameRegex = this.customerNameRegex;
		this.customerNameRegex = customerNameRegex;
		notifyListeners(FieldName.customerNameRegex, oldCustomerNameRegex, customerNameRegex);
	}


	private boolean vendorNameRegex = false;
	@SuppressWarnings("unused")
	private transient String vendorNameExpr;
	private String vendorName;
	/**
	 * returns the vendorName
	 * @return the vendorName
	 */
	public String getVendorName() {
		return vendorName;
	}

	/**
	 * Sets the string to search in the display name of the vendors person.
	 * @param vendorName the vendorName to set
	 */
	public void setVendorName(String vendorName)
	{
		final String oldVendorName = this.vendorName;
		this.vendorName = vendorName;
		notifyListeners(FieldName.vendorName, oldVendorName, vendorName);
	}

	/**
	 * @return Whether the value set with {@link #setVendorName(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setVendorName(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isVendorNameRegex() {
		return customerNameRegex;
	}

	/**
	 * Sets whether the value set with {@link #setVendorName(String)} represents a
	 * regular expression.
	 *
	 * @param vendorNameRegex The vendorNameRegex to search.
	 */
	public void setVendorNameRegex(boolean vendorNameRegex) {
		final boolean oldVendorNameRegex = this.vendorNameRegex;
		this.vendorNameRegex = vendorNameRegex;
		notifyListeners(FieldName.vendorNameRegex, oldVendorNameRegex, vendorNameRegex);
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
		notifyListeners(FieldName.articleContainerID, oldID, articleContainerID);
	}

	private int articleCountMin = -1;
	public int getArticleCountMin() {
		return articleCountMin;
	}
	public void setArticleCountMin(int articleCountMin)
	{
		int oldArticleCountMin = this.articleCountMin;
		this.articleCountMin = articleCountMin;
		notifyListeners(FieldName.articleCountMin, oldArticleCountMin, articleCountMin);
	}

	private int articleCountMax = -1;
	public int getArticleCountMax() {
		return articleCountMax;
	}
	public void setArticleCountMax(int articleCountMax)
	{
		int oldCountMax = this.articleCountMax;
		this.articleCountMax = articleCountMax;
		notifyListeners(FieldName.articleCountMax, oldCountMax, articleCountMax);
	}

	private Date createDTMin = null;
	public Date getCreateDTMin() {
		return createDTMin;
	}
	public void setCreateDTMin(Date createDTMin)
	{
		final Date oldCreateDTMin = this.createDTMin;
		this.createDTMin = createDTMin;
		notifyListeners(FieldName.createDTMin, oldCreateDTMin, createDTMin);
	}

	private Date createDTMax = null;
	public Date getCreateDTMax() {
		return createDTMax;
	}
	public void setCreateDTMax(Date createDTMax)
	{
		final Date oldCreateDTMax = this.createDTMax;
		this.createDTMax = createDTMax;
		notifyListeners(FieldName.createDTMax, oldCreateDTMax, createDTMax);
	}

	private UserID createUserID = null;
	public UserID getCreateUserID() {
		return createUserID;
	}
	public void setCreateUserID(UserID createUserID)
	{
		final UserID oldCreateUserID = this.createUserID;
		this.createUserID = createUserID;
		notifyListeners(FieldName.createUserID, oldCreateUserID, createUserID);
	}

	private AnchorID vendorID = null;
	public AnchorID getVendorID() {
		return vendorID;
	}
	public void setVendorID(AnchorID vendorID)
	{
		final AnchorID oldVendorID = this.vendorID;
		this.vendorID = vendorID;
		notifyListeners(FieldName.vendorID, oldVendorID, vendorID);
	}

	private AnchorID customerID = null;
	public AnchorID getCustomerID() {
		return customerID;
	}
	public void setCustomerID(AnchorID customerID)
	{
		final AnchorID oldCustomerID = this.customerID;
		this.customerID = customerID;
		notifyListeners(FieldName.customerID, oldCustomerID, customerID);
	}

	private ProductID productID = null;
	/**
	 * Returns the productID.
	 * @return the productID
	 */
	public ProductID getProductID() {
		return productID;
	}

	/**
	 * Sets the productID.
	 * @param productID the productID to set
	 */
	public void setProductID(ProductID productID) {
		this.productID = productID;
	}

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

	/**
	 * Returns the default member name of the Collection of Articles
	 * returned by {@link ArticleContainer#getArticles()} of the
	 * ArticleContainer implementation.
	 *
	 * @return the default member name of the Collection of Articles
	 * returned by {@link ArticleContainer#getArticles()} of the
	 * ArticleContainer implementation
	 */
	public String getArticleContainerArticlesMemberName() {
		return "articles";
	}

	private ProductTypeID productTypeID;
	/**
	 * @return the productTypeID
	 */
	public ProductTypeID getProductTypeID() {
		return productTypeID;
	}

	/**
	 * @param productTypeID the productTypeID to set
	 */
	public void setProductTypeID(ProductTypeID productTypeID) {
		this.productTypeID = productTypeID;
	}

}
