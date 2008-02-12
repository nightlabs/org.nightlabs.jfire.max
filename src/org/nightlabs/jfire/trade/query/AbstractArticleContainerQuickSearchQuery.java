/**
 * 
 */
package org.nightlabs.jfire.trade.query;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.trade.ArticleContainer;

/**
 * {@link JDOQuery} implementation which can be used together with
 * {@link AbstractArticleContainerQuickSearchEntry}s
 *  
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 */
public abstract class AbstractArticleContainerQuickSearchQuery 
extends JDOQuery<ArticleContainer> 
{ 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AbstractArticleContainerQuickSearchQuery.class);
	
	@Override
	protected Query prepareQuery() 
	{
		Query q = getPersistenceManager().newQuery(getArticleContainerClass());
		StringBuffer filter = new StringBuffer();
		
		filter.append("true");	
		
		// own methods to allow override e.g. for Offer where for customerName it is different
		checkCustomerName(filter);
		checkVendorName(filter);
		checkArticleContainerID(filter);
				
		q.setFilter(filter.toString());
//		q.setRange(rangeFromIncl, rangeToExcl);
		
		if (logger.isDebugEnabled())
			logger.debug("query = "+filter);
		
		return q;
	}

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
	
	protected void checkArticleContainerID(StringBuffer filter) 
	{
		if (getArticleContainerID() != null && !getArticleContainerID().trim().equals(""))
//			filter.append("\n && (this."+getArticleContainerIDMemberName()+" == \""+ObjectIDUtil.parseLongObjectIDField(articleContainerID)+"\"");
			filter.append("\n && (this."+getArticleContainerIDMemberName()+" == "+ObjectIDUtil.parseLongObjectIDField(articleContainerID)+")");			
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
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
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
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
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
		this.articleContainerID = articleContainerID;
	}
	
	/**
	 * returns the class of the articleContainer
	 * @return the class of the articleContainer
	 */
	public abstract Class getArticleContainerClass();
	
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
