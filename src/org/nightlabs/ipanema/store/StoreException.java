/*
 * Created on 30.09.2004
 *
 */
package org.nightlabs.ipanema.store;

import org.nightlabs.ModuleException;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class StoreException extends ModuleException
{
	public StoreException() 
	{
		super();
	}
	
	public StoreException(String message) 
	{
		super(message);
	}
	
	public StoreException(String message, Throwable cause) 
	{
		super(message, cause);
	}
	
	public StoreException(Throwable cause) 
	{
		super(cause);
	}
}
