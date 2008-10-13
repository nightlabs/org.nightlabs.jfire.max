package org.nightlabs.jfire.dynamictrade.recurring;

import org.nightlabs.jfire.trade.Article;

/**
 *
 * @author Fitas Amine <fitas@nightlabs.de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Article"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicProductTypeRecurringArticle"
 *
 * @jdo.inheritance strategy="new-table"
 *
 *
 * @jdo.fetch-group name="DynamicProductTypeRecurringArticle.dynamicProductTypeRecurringArticleName" fields="dynamicProductTypeRecurringArticleName"
 *
 */
public class DynamicProductTypeRecurringArticle extends Article {
	
	
	public static final String FETCH_GROUP_DYNAMIC_PRODUCT_TYPE_RECURRING_ARTICLE_NAME = "DynamicProductTypeRecurringArticle.dynamicProductTypeRecurringArticleName";

	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicProductTypeRecurringArticle() {}
	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long quantity;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private DynamicProductTypeRecurringArticleName 	dynamicProductTypeRecurringArticleName;
}
