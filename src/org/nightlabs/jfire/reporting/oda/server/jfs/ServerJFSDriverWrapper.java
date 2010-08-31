package org.nightlabs.jfire.reporting.oda.server.jfs;

import org.nightlabs.jfire.reporting.oda.server.jfs.ServerJFSDriver;

import org.nightlabs.jfire.reporting.oda.wrapper.DriverWrapper;

/**
 * @author Alexander Bieber
 *
 */
public class ServerJFSDriverWrapper extends DriverWrapper {

	/**
	 * 
	 */
	public ServerJFSDriverWrapper() {
		super(new ServerJFSDriver());
	}
}
