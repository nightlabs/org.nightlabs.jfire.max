/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * @author Alexander Bieber
 * @version $Revision$, $Date$
 */
public class ByteArrayBlob implements IBlob {

	private byte[] bytes;
	
	/**
	 * 
	 */
	public ByteArrayBlob(byte[] bytes) {
		this.bytes = bytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IBlob#getBinaryStream()
	 */
	@Override
	public InputStream getBinaryStream() throws OdaException {
		return new ByteArrayInputStream(bytes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IBlob#getBytes(long, int)
	 */
	@Override
	public byte[] getBytes(long arg0, int arg1) throws OdaException {
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IBlob#length()
	 */
	@Override
	public long length() throws OdaException {
		return bytes.length;
	}

}
