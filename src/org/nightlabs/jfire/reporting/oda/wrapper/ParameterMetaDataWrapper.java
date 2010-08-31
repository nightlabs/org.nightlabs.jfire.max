/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.wrapper;

import java.io.Serializable;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

/**
 * A generic class for metadata for ODA DataSets.
 * <p>
 * Note that as defined by ODA the position-parameters used
 * for most of the methods are 1-based.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ParameterMetaDataWrapper implements IParameterMetaData, Serializable {

	private static final long serialVersionUID = 1L;
	
	private org.eclipse.datatools.connectivity.oda.jfire.IParameterMetaData delegate;

	public ParameterMetaDataWrapper(org.eclipse.datatools.connectivity.oda.jfire.IParameterMetaData delegate) {
		super();
		this.delegate = delegate;
	}

	public int getParameterCount() throws OdaException {
		try {
			return delegate.getParameterCount();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getParameterMode(int i) throws OdaException {
		try {
			return delegate.getParameterMode(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getParameterType(int i) throws OdaException {
		try {
			return delegate.getParameterType(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public String getParameterTypeName(int i) throws OdaException {
		try {
			return delegate.getParameterTypeName(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getPrecision(int i) throws OdaException {
		try {
			return delegate.getPrecision(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getScale(int i) throws OdaException {
		try {
			return delegate.getScale(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int isNullable(int i) throws OdaException {
		try {
			return delegate.isNullable(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	@Override
	public String getParameterName(int param) throws OdaException {
		try {
			return delegate.getParameterName(param);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}
	
	
	

}
