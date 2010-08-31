/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.wrapper;

import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.LogConfiguration;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class DriverWrapper implements IDriver {

	private org.eclipse.datatools.connectivity.oda.jfire.IDriver delegate;

	public DriverWrapper(org.eclipse.datatools.connectivity.oda.jfire.IDriver delegate) {
		super();
		this.delegate = delegate;
	}

	public org.eclipse.datatools.connectivity.oda.IConnection getConnection(String s) throws OdaException {
		try {
			return new ConnectionWrapper(delegate.getConnection(s));
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getMaxConnections() throws OdaException {
		try {
			return delegate.getMaxConnections();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setAppContext(Object obj) throws OdaException {
		try {
			delegate.setAppContext(obj);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	@Override
	public void setLogConfiguration(
			LogConfiguration arg0)
			throws OdaException {
		// FIXME: Alex: Implement
	}
}
