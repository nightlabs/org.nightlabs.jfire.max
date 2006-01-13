/**
 * 
 */
package org.nightlabs.jfire.scripting;

import org.nightlabs.jfire.base.JFireException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 */
public class ScriptException
		extends JFireException
{
	private static final long serialVersionUID = 1L;


	public ScriptException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public ScriptException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ScriptException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ScriptException(Throwable cause)
	{
		super(cause);
	}

}
