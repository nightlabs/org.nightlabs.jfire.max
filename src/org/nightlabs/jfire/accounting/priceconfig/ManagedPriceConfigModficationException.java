/**
 *
 */
package org.nightlabs.jfire.accounting.priceconfig;

import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.security.Authority;

/**
 * This exception is thrown when an attempt to modify an externally/remotely managed {@link Authority} was detected.
 * Such an {@link Authority} is tagged with a non-<code>null</code> managed-by property.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ManagedPriceConfigModficationException extends RuntimeException {

	private static final long serialVersionUID = 20081222L;

	private PriceConfigID priceConfigID;
	private String managedBy;

	/**
	 * Constructs a {@link ManagedPriceConfigModficationException} for the given
	 * {@link PriceConfig} and managed-by tag.
	 * @param productTypeID The {@link PriceConfigID} of the {@link PriceConfig} for which a modification attempt was detected.
	 * @param managedBy The managed-by tag of the {@link PriceConfig} for which a modification attempt was detected.
	 */
	public ManagedPriceConfigModficationException(PriceConfigID priceConfigID, String managedBy) {
		super("Attempt to store the externally/remotely managed PriceConfig " + priceConfigID + " which is managed by " + managedBy);
		this.priceConfigID = priceConfigID;
		this.managedBy = managedBy;
	}

	/**
	 * @return The {@link PriceConfigID} of the {@link PriceConfig} for which a modification attempt was detected.
	 */
	public PriceConfigID getAuthorityID() {
		return priceConfigID;
	}

	/**
	 * @return The managed-by tag of the {@link PriceConfig} for which a modification attempt was detected.
	 */
	public String getManagedBy() {
		return managedBy;
	}
}
