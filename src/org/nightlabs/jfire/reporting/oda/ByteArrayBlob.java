/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.datatools.connectivity.oda.jfire.IBlob;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

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
	public InputStream getBinaryStream() throws JFireOdaException {
		return new ByteArrayInputStream(bytes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IBlob#getBytes(long, int)
	 */
	@Override
	public byte[] getBytes(long arg0, int arg1) throws JFireOdaException {
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IBlob#length()
	 */
	@Override
	public long length() throws JFireOdaException {
		return bytes.length;
	}

}
