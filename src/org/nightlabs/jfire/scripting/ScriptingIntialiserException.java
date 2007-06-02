/**
 * 
 */
package org.nightlabs.jfire.scripting;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ScriptingIntialiserException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ScriptingIntialiserException() {
	}

	/**
	 * @param message
	 */
	public ScriptingIntialiserException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ScriptingIntialiserException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ScriptingIntialiserException(String message, Throwable cause) {
		super(message, cause);
	}
}
