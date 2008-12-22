/**
 *
 */
package org.nightlabs.jfire.store;

import org.nightlabs.jfire.store.id.ProductTypeID;

/**
 * This exception is thrown when an attempt to modify an externally/remotely managed {@link ProductType} was detected.
 * Such a ProductType is tagged with a non-<code>null</code> managed-by property of the corresponding {@link ProductTypeLocal}.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ManagedProductTypeModficationException extends RuntimeException {

	private static final long serialVersionUID = 20081218L;

	private ProductTypeID productTypeID;
	private String managedBy;

	/**
	 * Constructs a {@link ManagedProductTypeModficationException} for the given
	 * {@link ProductTypeID} and managed-by tag.
	 * @param productTypeID The {@link ProductTypeID} of the {@link ProductType} for which a modification attempt was detected.
	 * @param managedBy The managed-by tag of the {@link ProductType} for which a modification attempt was detected.
	 */
	public ManagedProductTypeModficationException(ProductTypeID productTypeID, String managedBy) {
		super("Attempt to store the externally/remotely managed ProductType " + productTypeID + " which is managed by " + managedBy);
		this.productTypeID = productTypeID;
		this.managedBy = managedBy;
	}

	/**
	 * @return The {@link ProductTypeID} of the {@link ProductType} for which a modification attempt was detected.
	 */
	public ProductTypeID getProductTypeID() {
		return productTypeID;
	}

	/**
	 * @return The managed-by tag of the {@link ProductType} for which a modification attempt was detected.
	 */
	public String getManagedBy() {
		return managedBy;
	}
}
