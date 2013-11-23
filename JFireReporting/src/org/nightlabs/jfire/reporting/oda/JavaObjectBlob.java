/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import java.io.InputStream;
import java.io.Serializable;

import org.eclipse.datatools.connectivity.oda.jfire.IBlob;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JavaObjectBlob implements IBlob, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Object object;
	
	/**
	 * 
	 */
	public JavaObjectBlob(Object object) {
		this.object = object;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IBlob#getBinaryStream()
	 */
	public InputStream getBinaryStream() throws JFireOdaException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IBlob#getBytes(long, int)
	 */
	public byte[] getBytes(long arg0, int arg1) throws JFireOdaException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IBlob#length()
	 */
	public long length() throws JFireOdaException {
		return 0;
	}

	public Object getObject() {
		return object;
	}
}
