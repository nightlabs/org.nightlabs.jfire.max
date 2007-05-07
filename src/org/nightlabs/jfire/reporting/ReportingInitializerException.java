/**
 * 
 */
package org.nightlabs.jfire.reporting;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportingInitializerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ReportingInitializerException() {
	}

	/**
	 * @param message
	 */
	public ReportingInitializerException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ReportingInitializerException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ReportingInitializerException(String message, Throwable cause) {
		super(message, cause);
	}

}
