/**
 * 
 */
package org.nightlabs.jfire.web.webshop;

/**
 * @author Khaled
 */
public class DuplicateIDException
// FIXME: why Throwable and not Exception ??
extends Throwable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DuplicateIDException()
	{
	}

	/**
	 * @param arg0
	 */
	public DuplicateIDException(String arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public DuplicateIDException(Throwable arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DuplicateIDException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

}
