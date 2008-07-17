package org.nightlabs.jfire.trade.history;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
{
	public static Comparator<ProductHistoryItem> productHistoryItemComparator = new Comparator<ProductHistoryItem>(){
		@Override
		public int compare(ProductHistoryItem phi1, ProductHistoryItem phi2) {
			if (phi1.getCreateDT() != null && phi2.getCreateDT() != null) {
				return phi1.getCreateDT().compareTo(phi2.getCreateDT());
			}
			return 0;
		}
	};
	private ProductID productID; 
	private SortedSet<ProductHistoryItem> productHistoryItems = new TreeSet<ProductHistoryItem>(productHistoryItemComparator);
	
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
	public SortedSet<ProductHistoryItem> getProductHistoryItems() {
		return Collections.unmodifiableSortedSet(productHistoryItems);
	}

	/**
	 * Adds an ProductHistoryItem.
	 * @param productHistoryItem the ProductHistoryItem to add
	 */
	public void addProductHistoryItem(ProductHistoryItem productHistoryItem) {
		productHistoryItems.add(productHistoryItem);
	}
}
