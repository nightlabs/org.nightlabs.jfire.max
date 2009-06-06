package org.nightlabs.jfire.reporting.layout.render;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ReportLayoutEngineException extends Exception {

	/**
	 *
	 */
	public ReportLayoutEngineException() {
	}

	/**
	 * @param message
	 */
	public ReportLayoutEngineException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ReportLayoutEngineException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ReportLayoutEngineException(String message, Throwable cause) {
		super(message, cause);
	}

}
