package org.nightlabs.jfire.accounting.gridpriceconfig;

import org.nightlabs.jfire.store.ProductType;

public interface PriceCalculatorFactory
{
	PriceCalculator createPriceCalculator(ProductType productType);
}
