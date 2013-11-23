package org.nightlabs.jfire.reporting.oda.client.wrapper;

import org.nightlabs.jfire.reporting.oda.client.jfs.ClientJFSDriver;
import org.nightlabs.jfire.reporting.oda.wrapper.DriverWrapper;

/**
 * @author Daniel Mazurek
 *
 */
public class ClientJFSDriverWrapper extends DriverWrapper {

	/**
	 * @param delegate
	 */
	public ClientJFSDriverWrapper() {
		super(new ClientJFSDriver());
	}

}
