/**
 * 
 */
package org.nightlabs.jfire.reporting.layout.render;

/**
 * Exception to wrap BIRT exceptions, so that clients
 * are not required to know BIRT when rendering reports.
 *
 * TODO: Need to wrap possible causes in simple exceptions.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class RenderReportException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public RenderReportException() {
	}

	/**
	 * @param message
	 */
	public RenderReportException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RenderReportException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RenderReportException(String message, Throwable cause) {
		super(message, cause);
	}

}
