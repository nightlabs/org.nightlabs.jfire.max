/**
 * 
 */
package org.nightlabs.jfire.store;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class CannotPublishProductTypeException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String resonKey;
	
	/**
	 * @param resonKey The key for the reason why the ProductType can't be published.
	 */
	public CannotPublishProductTypeException(String resonKey) {
		super();
		this.resonKey = resonKey;
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be published.
	 * @param message The expeption message
	 */
	public CannotPublishProductTypeException(String resonKey, String message) {
		super(message);
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be published.
	 * @param cause The exception cause
	 */
	public CannotPublishProductTypeException(String resonKey, Throwable cause) {
		super(cause);
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be published.
	 * @param message The exception message.
	 * @param cause The exception cause.
	 */
	public CannotPublishProductTypeException(String resonKey, String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Get the the the key for the reason why the ProductType can't be published.
	 * @return The key for the reason why the ProductType can't be published.
	 */
	public String getResonKey() {
		return resonKey;
	}
	
}

