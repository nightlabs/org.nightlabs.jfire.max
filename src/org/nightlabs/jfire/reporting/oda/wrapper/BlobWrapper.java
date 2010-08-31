/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.wrapper;

import java.io.InputStream;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

/**
 * @author Alexander Bieber
 * @version $Revision$, $Date$
 */
public class BlobWrapper implements IBlob {

	private org.eclipse.datatools.connectivity.oda.jfire.IBlob delegate;
	
	/**
	 * 
	 */
	public BlobWrapper(org.eclipse.datatools.connectivity.oda.jfire.IBlob delegate) {
		this.delegate = delegate;
	}

	public InputStream getBinaryStream() throws OdaException {
		try {
			if (delegate == null)
				return null;
			
			return delegate.getBinaryStream();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public byte[] getBytes(long arg0, int arg1) throws OdaException {
		try {
			if (delegate == null)
				return null;

			return delegate.getBytes(arg0, arg1);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public long length() throws OdaException {
		try {
			if (delegate == null)
				return 0;

			return delegate.length();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}
	
	
	
}
