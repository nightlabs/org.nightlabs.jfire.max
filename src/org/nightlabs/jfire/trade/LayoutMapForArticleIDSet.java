package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.trade.id.ArticleID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class LayoutMapForArticleIDSet 
implements Serializable 
{
	private static final long serialVersionUID = 1L;

	public LayoutMapForArticleIDSet()
	{
		this.articleID2ProductIDMap = new HashMap<ArticleID, ProductID>();
		this.productID2LayoutMap = new HashMap<ProductID, ILayout>();
	}

	public LayoutMapForArticleIDSet(Map<ArticleID, ProductID> articleID2TicketIDMap, 
			Map<ProductID, ILayout> productID2LayoutMap)
	{
		if (articleID2TicketIDMap == null)
			throw new IllegalArgumentException("articleID2TicketIDMap == null");

		if (productID2LayoutMap == null)
			throw new IllegalArgumentException("productID2LayoutMap == null");

		this.articleID2ProductIDMap = articleID2TicketIDMap;
		this.productID2LayoutMap = productID2LayoutMap;
	}

	private Map<ArticleID, ProductID> articleID2ProductIDMap;
	private Map<ProductID, ILayout> productID2LayoutMap;

	public Map<ArticleID, ProductID> getArticleID2ProductIDMap()
	{
		return articleID2ProductIDMap;
	}
	
	public Map<ProductID, ILayout> getProductID2LayoutMap()
	{
		return productID2LayoutMap;
	}

	/**
	 * Adds the contents from <code>other</code> and overwrites already existing entries
	 * (which should not happen in reality).
	 * @param other Another instance containing values that shall be added to this instance.
	 */
	public void append(LayoutMapForArticleIDSet other)
	{
		this.articleID2ProductIDMap.putAll(other.articleID2ProductIDMap);
		this.productID2LayoutMap.putAll(other.productID2LayoutMap);
	}

}
