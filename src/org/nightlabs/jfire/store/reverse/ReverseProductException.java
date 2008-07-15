package org.nightlabs.jfire.store.reverse;

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
//	private Set<IReverseProductError> errors = new HashSet<IReverseProductError>();
	private IReverseProductError reverseProductError = null;
	
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

	public String getDescription() {
		if (reverseProductError != null) {
			return reverseProductError.getDescription();
		}
		return "";
	}
	
	/**
	 * Returns the reverseProductError.
	 * @return the reverseProductError
	 */
	public IReverseProductError getReverseProductError() {
		return reverseProductError;
	}

	/**
	 * Sets the reverseProductError.
	 * @param reverseProductError the reverseProductError to set
	 */
	public void setReverseProductError(IReverseProductError reverseProductError) {
		this.reverseProductError = reverseProductError;
	}
	
//	/**
//	 * Returns the description.
//	 * @return the description
//	 */
//	public String getDescription() 
//	{
//		StringBuffer sb = new StringBuffer();
//		for (IReverseProductError error : errors) {
//			sb.append(error.getDescription());
//			sb.append("\n");
//		}
//		return sb.toString();
//	}
//	
//	/**
//	 * Returns the {@link IReverseProductError}s.
//	 * @return the {@link IReverseProductError}s
//	 */
//	public Set<IReverseProductError> getReverseProductResultErrors() {
//		return Collections.unmodifiableSet(errors);
//	}
//	
//	/**
//	 * Adds an IReverseProductError.
//	 * @param error the IReverseProductError to add
//	 */
//	public void addReverseProductResultError(IReverseProductError error) {
//		errors.add(error);
//	}
	
}
