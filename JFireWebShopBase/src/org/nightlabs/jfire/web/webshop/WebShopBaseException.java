package org.nightlabs.jfire.web.webshop;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class WebShopBaseException extends Exception
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public WebShopBaseException()
	{
		super();
	}

	public WebShopBaseException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WebShopBaseException(String message)
	{
		super(message);
	}

	public WebShopBaseException(Throwable cause)
	{
		super(cause);
	}
}
