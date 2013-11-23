/**
 *
 */
package org.nightlabs.jfire.accounting.gridpriceconfig;

import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;

/**
 * This {@link StablePriceConfig} can be used to create non-persistent
 * {@link StablePriceConfig}s that refer to an Existing {@link IPriceConfig}.
 *
 * Note, that no dimensions of values will be automatically taken from the
 * {@link #basePriceConfig} a {@link TransientStablePriceConfig} is created with.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class TransientStablePriceConfig extends StablePriceConfig {

	private static final long serialVersionUID = 20081222L;

	private IPriceConfig basePriceConfig;

	/**
	 * Constructs a new {@link TransientStablePriceConfig} that will
	 * refer to the given {@link PriceConfig}.
	 *
	 * @param basePriceConfig The {@link IPriceConfig} to refer to.
	 */
	public TransientStablePriceConfig(final IPriceConfig basePriceConfig) {
//		super(SecurityReflector.getUserDescriptor().getOrganisationID(), nextPriceConfigID());
		super(null);
		this.basePriceConfig = basePriceConfig;
	}

	/**
	 * @return The {@link IPriceConfig} this {@link TransientStablePriceConfig} refers to.
	 */
	public IPriceConfig getBasePriceConfig() {
		return basePriceConfig;
	}
	private static long nextPriceConfigID = 0;
//	private static synchronized String nextPriceConfigID() {
//		return ObjectIDUtil.longObjectIDFieldToString(nextPriceConfigID++);
//	}
}
