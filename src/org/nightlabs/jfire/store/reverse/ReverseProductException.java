package org.nightlabs.jfire.store.reverse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.store.id.ProductID;

/**
 * Contains information if the reversing of an product is successful and
 * if not containing {@link IReverseProductError} which have the necessary 
 * information why not. 
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ReverseProductException
extends ModuleException
{
	private static final long serialVersionUID = 1L;
	
	private ProductID productID;
	private Set<IReverseProductError> errors = new HashSet<IReverseProductError>();
//	private boolean productReversable;
//	private OfferID reversingOfferID;
//	private Set<ArticleID> articleIDs = new HashSet<ArticleID>();
	
	public ReverseProductException(ProductID productID) {
		this.productID = productID;
	}

	/**
	 * Returns the productID.
	 * @return the productID
	 */
	public ProductID getProductID() {
		return productID;
	}

//	/**
//	 * Returns the productReversable.
//	 * @return the productReversable
//	 */
//	public boolean isProductReversable() {
//		return productReversable;
//	}
//
//	/**
//	 * Sets the productReversable.
//	 * @param productReversable the productReversable to set
//	 */
//	public void setProductReversable(boolean productReversable) {
//		this.productReversable = productReversable;
//	}
//
//	/**
//	 * Returns the reversingOfferID.
//	 * @return the reversingOfferID
//	 */
//	public OfferID getReversingOfferID() {
//		return reversingOfferID;
//	}
//
//	/**
//	 * Sets the reversingOfferID.
//	 * @param reversingOfferID the reversingOfferID to set
//	 */
//	public void setReversingOfferID(OfferID reversingOfferID) {
//		this.reversingOfferID = reversingOfferID;
//	}

	/**
	 * Returns the description.
	 * @return the description
	 */
	public String getDescription() 
	{
		StringBuffer sb = new StringBuffer();
		for (IReverseProductError error : errors) {
			sb.append(error.getDescription());
			sb.append("\n");
		}
		return sb.toString();
	}

//	/**
//	 * Returns the articleIDs.
//	 * @return the articleIDs
//	 */
//	public Set<ArticleID> getArticleIDs() {
//		return articleIDs;
//	}
//
//	/**
//	 * Sets the articleIDs.
//	 * @param articleIDs the articleIDs to set
//	 */
//	public void setArticleIDs(Set<ArticleID> articleIDs) {
//		this.articleIDs = articleIDs;
//	}
	
	/**
	 * Returns the {@link IReverseProductError}s.
	 * @return the {@link IReverseProductError}s
	 */
	public Set<IReverseProductError> getReverseProductResultErrors() {
		return Collections.unmodifiableSet(errors);
	}
	
	/**
	 * Adds an IReverseProductError.
	 * @param error the IReverseProductError to add
	 */
	public void addReverseProductResultError(IReverseProductError error) {
		errors.add(error);
	}
}
