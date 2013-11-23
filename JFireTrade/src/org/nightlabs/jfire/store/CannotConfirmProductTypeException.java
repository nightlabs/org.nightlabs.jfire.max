/**
 * 
 */
package org.nightlabs.jfire.store;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class CannotConfirmProductTypeException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String resonKey;
	
	/**
	 * @param resonKey The key for the reason why the ProductType can't be confirmed.
	 */
	public CannotConfirmProductTypeException(String resonKey) {
		super();
		this.resonKey = resonKey;
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be confirmed.
	 * @param message The expeption message
	 */
	public CannotConfirmProductTypeException(String resonKey, String message) {
		super(message);
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be confirmed.
	 * @param cause The exception cause
	 */
	public CannotConfirmProductTypeException(String resonKey, Throwable cause) {
		super(cause);
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be confirmed.
	 * @param message The exception message.
	 * @param cause The exception cause.
	 */
	public CannotConfirmProductTypeException(String resonKey, String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Get the the the key for the reason why the ProductType can't be confirmed.
	 * @return The key for the reason why the ProductType can't be confirmed.
	 */
	public String getResonKey() {
		return resonKey;
	}
	
}

