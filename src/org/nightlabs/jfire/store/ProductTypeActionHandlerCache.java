package org.nightlabs.jfire.store;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * This is a simple cache managing a <code>Map&lt;Class&lt;? extends ProductType&gt;, ProductTypeActionHandler&gt;</code>
 * in order to prevent unnecessary lookups. It can only be used within one transaction!
 *
 * @author marco
 */
public class ProductTypeActionHandlerCache
{
	private Map<Class<? extends ProductType>, ProductTypeActionHandler> class2ProductTypeActionHandler = new HashMap<Class<? extends ProductType>, ProductTypeActionHandler>();

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

	public ProductTypeActionHandler getProductTypeActionHandler(Class<? extends ProductType> productTypeClass)
	{
		ProductTypeActionHandler ptah = class2ProductTypeActionHandler.get(productTypeClass);
		if (ptah == null) {
			ptah = ProductTypeActionHandler.getProductTypeActionHandler(pm, productTypeClass);
			class2ProductTypeActionHandler.put(productTypeClass, ptah);
		}
		return ptah;
	}
}
