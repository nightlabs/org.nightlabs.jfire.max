/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireReportingOdaException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public JFireReportingOdaException() {
	}

	/**
	 * @param message
	 */
	public JFireReportingOdaException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public JFireReportingOdaException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public JFireReportingOdaException(String message, Throwable cause) {
		super(message, cause);
	}

}
