/**
 * 
 */
package org.nightlabs.jfire.accounting.gridpriceconfig;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.store.id.ProductTypeID;

public class AssignInnerPriceConfigCommand
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private ProductTypeID productTypeID;
	private PriceConfigID innerPriceConfigID;
	private boolean innerPriceConfigInherited;
	public AssignInnerPriceConfigCommand(ProductTypeID productTypeID, PriceConfigID innerPriceConfigID, boolean innerPriceConfigInherited)
	{
		assert productTypeID != null;

		this.productTypeID = productTypeID;
		this.innerPriceConfigID = innerPriceConfigID;
		this.innerPriceConfigInherited = innerPriceConfigInherited;
	}
	public ProductTypeID getProductTypeID()
	{
		return productTypeID;
	}
	public PriceConfigID getInnerPriceConfigID()
	{
		return innerPriceConfigID;
	}
	public boolean isInnerPriceConfigInherited()
	{
		return innerPriceConfigInherited;
	}
}