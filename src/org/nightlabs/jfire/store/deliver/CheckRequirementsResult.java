/**
 * 
 */
package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;

/**
 * Result object can be returned by {@link ServerDeliveryProcessor}s,
 * in the {@link ServerDeliveryProcessor#_checkRequirements(CheckRequirementsEnvironment)},
 * including the localized message and the machine-readable result code. 
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public class CheckRequirementsResult
implements Serializable
{
	private String localizedMessage;
	private String resultCode;
	
	/**
	 * Creates a CheckRequirementsResult.
	 * @param resultCode the machine-readable resultCode,
	 * normally the class name of the {@link ServerDeliveryProcessor} where
	 * the result is coming from + an additional identifier describing the problem
	 * @param localizedMessage the localized message
	 */
	public CheckRequirementsResult(String resultCode, String localizedMessage) {
		super();
		this.localizedMessage = localizedMessage;
		this.resultCode = resultCode;
	}

	/**
	 * Returns the localized message.
	 * @return the localized message
	 */
	public String getMessage() {
		return localizedMessage;
	}

	/**
	 * Sets the localized message.
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.localizedMessage = message;
	}

	/**
	 * Returns the resultCode.
	 * Normally the class name of the {@link ServerDeliveryProcessor} where
	 * the result is coming from + an additional identifier describing the problem,
	 * to be able to react on it in the client
	 * @return the resultCode
	 */
	public String getResultCode() {
		return resultCode;
	}

	/**
	 * Sets the resultCode.
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	
}
