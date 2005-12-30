/*
 * Created on 30.09.2004
 *
 */
package org.nightlabs.ipanema.trade;

import org.nightlabs.ModuleException;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 *
 */
public class TradeException extends ModuleException
{
	public TradeException() 
	{
		super();
	}
	
	public TradeException(String message) 
	{
		super(message);
	}
	
	public TradeException(String message, Throwable cause) 
	{
		super(message, cause);
	}
	
	public TradeException(Throwable cause) 
	{
		super(cause);
	}

}
