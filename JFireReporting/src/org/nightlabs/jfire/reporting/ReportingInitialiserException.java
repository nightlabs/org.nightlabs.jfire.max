/**
 * 
 */
package org.nightlabs.jfire.reporting;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportingInitialiserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ReportingInitialiserException() {
	}

	/**
	 * @param message
	 */
	public ReportingInitialiserException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ReportingInitialiserException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ReportingInitialiserException(String message, Throwable cause) {
		super(message, cause);
	}

}
