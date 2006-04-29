package org.nightlabs.jfire.store;

/**
 * This exception is thrown, if there is no {@link ProductTypeActionHandler} registered
 * for a certain <code>ProductType</code>-{@link Class}.
 *
 * @see ProductTypeActionHandler#getProductTypeActionHandler(javax.jdo.PersistenceManager, Class)
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ProductTypeActionHandlerNotFoundException
		extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private Class productTypeClass;

	public ProductTypeActionHandlerNotFoundException(Class productTypeClass, String message)
	{
		super(message);
		this.productTypeClass = productTypeClass;
	}

	public ProductTypeActionHandlerNotFoundException(Class productTypeClass, String message,
			Throwable cause)
	{
		super(message, cause);
		this.productTypeClass = productTypeClass;
	}

	public Class getProductTypeClass()
	{
		return productTypeClass;
	}
}
