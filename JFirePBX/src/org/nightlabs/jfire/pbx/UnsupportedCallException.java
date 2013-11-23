package org.nightlabs.jfire.pbx;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class UnsupportedCallException extends PhoneSystemException
{
	private static final long serialVersionUID = 1L;
	private Call call;

	public UnsupportedCallException(Call call) {
		this.call = call;
	}

	public UnsupportedCallException(Call call, String message, Throwable cause) {
		super(message, cause);
		this.call = call;
	}

	public UnsupportedCallException(Call call, String message) {
		super(message);
		this.call = call;
	}

	public UnsupportedCallException(Call call, Throwable cause) {
		super(cause);
		this.call = call;
	}

	public Call getCall() {
		return call;
	}

}
