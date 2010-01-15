/**
 *
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.Serializable;

import org.nightlabs.util.reflect.ReflectUtil;

/**
 * Exception to wrap exceptions thrown while rendering reports, 
 * so that clients are not required to know the underlying rendering engine
 * when rendering reports.
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
		super();
		initCause(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RenderReportException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}

	@Override
	public synchronized Throwable initCause(Throwable cause) {
		System.err.println("Init cause of " + RenderReportException.class.getSimpleName() + " called with " + cause);
		if (cause != null) {
			if (ReflectUtil.findContainedObjectsByClass(cause, Serializable.class, false)) {
				cause = new RenderReportException(cause.getMessage());
			}
		}
		return super.initCause(cause);
	}

}
