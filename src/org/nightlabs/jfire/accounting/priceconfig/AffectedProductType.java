package org.nightlabs.jfire.accounting.priceconfig;

import java.io.Serializable;

import org.nightlabs.jfire.store.id.ProductTypeID;

public class AffectedProductType
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private ProductTypeID productTypeID;
	private int nestingLevel;

	public AffectedProductType(ProductTypeID productTypeID, int nestingLevel)
	{
		this.productTypeID = productTypeID;
		this.nestingLevel = nestingLevel;
	}

	public ProductTypeID getProductTypeID()
	{
		return productTypeID;
	}
	public int getNestingLevel()
	{
		return nestingLevel;
	}
}
