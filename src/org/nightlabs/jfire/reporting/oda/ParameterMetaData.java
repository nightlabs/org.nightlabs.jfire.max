/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * A generic class for metadata for oda DataSets.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ParameterMetaData implements IParameterMetaData, Serializable {

	private static final long serialVersionUID = 1L;	

	/**
	 * Holds information about parameters according to datatools oda. 
	 */
	public static class ParameterDescriptor implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private int mode;
		private int dataType;
		private String dataTypeName;
		private int precision;
		private int scale;
		private int nullable;
		
		/**
		 * @return the dataType
		 */
		public int getDataType() {
			return dataType;
		}
		/**
		 * @param dataType the dataType to set
		 */
		public void setDataType(int dataType) {
			this.dataType = dataType;
		}
		/**
		 * @return the mode
		 */
		public int getMode() {
			return mode;
		}
		/**
		 * @param mode the mode to set
		 */
		public void setMode(int mode) {
			this.mode = mode;
		}
		/**
		 * @return the dataTypeName
		 */
		public String getDataTypeName() {
			return dataTypeName;
		}
		/**
		 * @param name the name to set
		 */
		public void setDataTypeName(String dataTypeName) {
			this.dataTypeName = dataTypeName;
		}
		/**
		 * @return the precision
		 */
		public int getPrecision() {
			return precision;
		}
		/**
		 * @param precision the precision to set
		 */
		public void setPrecision(int precision) {
			this.precision = precision;
		}
		/**
		 * @return the scale
		 */
		public int getScale() {
			return scale;
		}
		/**
		 * @param scale the scale to set
		 */
		public void setScale(int scale) {
			this.scale = scale;
		}
		/**
		 * @return the nullable
		 */
		public int isNullable() {
			return nullable;
		}
		/**
		 * @param nullable the nullable to set
		 */
		public void setNullable(int nullable) {
			this.nullable = nullable;
		}
		
	}
	
	private List<ParameterDescriptor> parameters = new ArrayList<ParameterDescriptor>();
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterCount()
	 */
	public int getParameterCount() throws OdaException {
		return parameters.size();
	}

	private ParameterDescriptor getDescriptor(int pPosition) {
		int pIdx = pPosition -1;
		if (pIdx >= parameters.size() || pIdx < 0)
			throw new IllegalArgumentException("No parameter with at index "+pPosition+" can be found in this ParameterMetaData set.");
		return parameters.get(pIdx);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterMode(int)
	 */
	public int getParameterMode(int pPosition) throws OdaException {
		return getDescriptor(pPosition).getMode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterType(int)
	 */
	public int getParameterType(int pPosition) throws OdaException {
		return getDescriptor(pPosition).getDataType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterTypeName(int)
	 */
	public String getParameterTypeName(int pPosition) throws OdaException {
		return getDescriptor(pPosition).getDataTypeName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getPrecision(int)
	 */
	public int getPrecision(int pPosition) throws OdaException {
		return getDescriptor(pPosition).getPrecision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getScale(int)
	 */
	public int getScale(int pPosition) throws OdaException {
		return getDescriptor(pPosition).getScale();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#isNullable(int)
	 */
	public int isNullable(int pPosition) throws OdaException {
		return getDescriptor(pPosition).isNullable();
	}
	
	public boolean hasParamDescriptor(int pPosition) {
		int pIdx = pPosition -1;
		return (pIdx < parameters.size() || pIdx >= 0);
	}
	
	public void addParameterDescriptor(ParameterDescriptor descriptor) {
		parameters.add(descriptor);
	}

}
