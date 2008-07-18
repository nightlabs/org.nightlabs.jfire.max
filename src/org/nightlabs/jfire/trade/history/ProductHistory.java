package org.nightlabs.jfire.trade.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.id.ProductID;

/**
 * An {@link ProductHistory} object has a {@link Set} of {@link ProductHistoryItem}s
 * which contain Trade specific information for an specific the product.
 *  
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ProductHistory
implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private ProductID productID; 
//	private SortedSet<ProductHistoryItem> productHistoryItems = new TreeSet<ProductHistoryItem>(
//			new ProductHistoryItemComparator());
	private List<ProductHistoryItem> productHistoryItems = new ArrayList<ProductHistoryItem>();
	
	/**
	 * Creates an {@link ProductHistory} for the {@link Product} with the given {@link ProductID}. 
	 * @param productID the {@link ProductID} of the {@link Product} of this {@link ProductHistory} object. 
	 */
	public ProductHistory(ProductID productID) {
		this.productID = productID;	
	}

	/**
	 * Returns the productID.
	 * @return the productID
	 */
	public ProductID getProductID() {
		return productID;
	}
	
	/**
	 * Returns the productHistoryItems.
	 * @return the productHistoryItems
	 */
	public List<ProductHistoryItem> getProductHistoryItems() {
		return Collections.unmodifiableList(productHistoryItems);
	}

//	/**
//	 * Returns the productHistoryItems.
//	 * @return the productHistoryItems
//	 */
//	public SortedSet<ProductHistoryItem> getProductHistoryItems() {
//		return Collections.unmodifiableSortedSet(productHistoryItems);
//	}
	
	/**
	 * Adds an ProductHistoryItem.
	 * @param productHistoryItem the ProductHistoryItem to add
	 */
	public void addProductHistoryItem(ProductHistoryItem productHistoryItem) {
		productHistoryItems.add(productHistoryItem);
	}
	
	/**
	 * Detaches this {@link ProductHistory} object including all
	 * {@link ProductHistoryItem}s. 
	 * @param pm the PersistenceManager used for detaching
	 */
	public void detachCopy(PersistenceManager pm) 
	{
		for (ProductHistoryItem item : productHistoryItems) {
			item.detachCopy(pm);
		}
	}
}
