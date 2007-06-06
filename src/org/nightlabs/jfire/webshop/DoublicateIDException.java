/**
 * 
 */
package org.nightlabs.jfire.webshop;

/**
 * @author Khaled
 *
 */
public class DoublicateIDException 
extends Throwable
{
	/**
	 * 
	 */
	public DoublicateIDException()
	{
	}

	/**
	 * @param arg0
	 */
	public DoublicateIDException(String arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public DoublicateIDException(Throwable arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DoublicateIDException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

}
