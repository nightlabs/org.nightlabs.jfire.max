/**
 * 
 */
package org.nightlabs.jfire.dynamictrade.accounting.priceconfig;

import org.nightlabs.jfire.accounting.TariffMapper;
import org.nightlabs.jfire.accounting.gridpriceconfig.IResultPriceConfig;
import org.nightlabs.jfire.accounting.gridpriceconfig.TransientStablePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.CustomerGroupMapper;

/**
 * @author Alexander Bieber
 * @version $Revision$, $Date$
 */
public class PriceCalculator extends
		org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator {

	/**
	 * @param packageProductType
	 * @param customerGroupMapper
	 * @param tariffMapper
	 */
	public PriceCalculator(ProductType packageProductType,
			CustomerGroupMapper customerGroupMapper, TariffMapper tariffMapper) {
		super(packageProductType, customerGroupMapper, tariffMapper);
	}
	
	protected IResultPriceConfig createResultPriceConfig(IPriceConfig innerPriceConfig)
	{
		return new TransientStablePriceConfig(innerPriceConfig);
	}	

}
