package org.nightlabs.jfire.store;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

public class ProductTypeActionHandlerCache
{
	private Map<Class, ProductTypeActionHandler> class2ProductTypeActionHandler = new HashMap<Class, ProductTypeActionHandler>();

	private PersistenceManager pm;

	public ProductTypeActionHandlerCache(PersistenceManager pm)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		this.pm = pm;
	}

	public ProductTypeActionHandler getProductTypeActionHandler(Product product)
	{
		return getProductTypeActionHandler(product.getProductType());
	}

	public ProductTypeActionHandler getProductTypeActionHandler(ProductType productType)
	{
		return getProductTypeActionHandler(productType.getClass());
	}

	public ProductTypeActionHandler getProductTypeActionHandler(Class productTypeClass)
	{
		ProductTypeActionHandler ptah = class2ProductTypeActionHandler.get(productTypeClass);
		if (ptah == null) {
			ptah = ProductTypeActionHandler.getProductTypeActionHandler(pm, productTypeClass);
			class2ProductTypeActionHandler.put(productTypeClass, ptah);
		}
		return ptah;
	}
}
