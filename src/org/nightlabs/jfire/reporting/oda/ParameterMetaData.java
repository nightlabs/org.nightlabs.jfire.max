/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.jfire.scripting.ScriptParameter;
import org.nightlabs.jfire.scripting.ScriptParameterSet;

/**
 * A generic class for metadata for oda DataSets.
 * <p>
 * Note that as defined by ODA the position-parameters used
 * for most of the methods are 1-based.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ParameterMetaData implements NamedParameterMetaData, Serializable {

	private static final long serialVersionUID = 1L;	

	private static final Logger logger = Logger.getLogger(ParameterMetaData.class);
	
	/**
	 * Holds information about parameters according to datatools oda. 
	 */
	public static class ParameterDescriptor implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private String parameterName;
		private int mode;
		private int dataType;
		private String dataTypeName;		
		private int precision;
		private int scale;
		private int nullable = IParameterMetaData.parameterNullable;
		
		private String realDataTypeName;
		
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
		/**
		 * @return the parameterName
		 */
		public String getParameterName() {
			return parameterName;
		}
		/**
		 * @param parameterName the parameterName to set
		 */
		public void setParameterName(String parameterName) {
			this.parameterName = parameterName;
		}
		
		/**
		 * The realDataTypeName can be used to store the original 
		 * type name (which might have been a class name).
		 * 
		 * @return the realDataTypeName
		 */
		public String getRealDataTypeName() {
			return realDataTypeName;
		}
		/**
		 * @param realDataTypeName the realDataTypeName to set
		 */
		public void setRealDataTypeName(String realDataTypeName) {
			this.realDataTypeName = realDataTypeName;
		}
	}
	
	private List<ParameterDescriptor> parameters = new ArrayList<ParameterDescriptor>();
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterCount()
	 */
	public int getParameterCount() throws OdaException {
		return parameters.size();
	}

	public ParameterDescriptor getDescriptor(int pPosition) {
		int pIdx = pPosition -1;
		if (pIdx >= parameters.size() || pIdx < 0)
			throw new IllegalArgumentException("No parameter at index "+pPosition+" can be found in this ParameterMetaData set.");
		return parameters.get(pIdx);
	}
	
	/**
	 * Additional method that returns a name for a given parameter.
	 * Note that this is not part of the ODA API and therefor not neccessary
	 * to exist. 
	 * 
	 * @param pPosition The parameter postion the name should be returned.
	 * @return The name of the referenced parameter if one is set, <code>null</code> otherwise.
	 */
	public String getParameterName(int pPosition) {
		return getDescriptor(pPosition).getParameterName();
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
	 * @see org.eclipse.datatools.connectivity.oda.IParametParameterDescriptorerMetaData#getPrecision(int)
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

	/**
	 * Tries to create a ODA {@link ParameterMetaData} from the given 
	 * {@link ScriptParameterSet}. The parameters will be sorted by
	 * their parameterID (their name) so this method will always return
	 * the same result for the same parameter set.
	 * 
	 * @param parameterSet The parameter set to create meta data for.
	 * @return A ParameterMetaData representing the parameter of the given script parameter set.
	 * @throws JFireReportingOdaException 
	 */
	public static ParameterMetaData createMetaDataFromParameterSet(ScriptParameterSet parameterSet) throws JFireReportingOdaException
	{
		ParameterMetaData result = new ParameterMetaData();
		for (Iterator iter = parameterSet.getSortedParameters().iterator(); iter.hasNext();) {
			ScriptParameter parameter = (ScriptParameter) iter.next();
			if (parameter == null)
				throw new IllegalStateException("Previously still registered parameter now not found ?!?");
			ParameterDescriptor descriptor = new ParameterDescriptor();
			descriptor.setMode(IParameterMetaData.parameterModeIn);
			int dataType;
			try {
				dataType = DataType.classToDataType(parameter.getScriptParameterClass());
			} catch (ClassNotFoundException e) {
				throw new JFireReportingOdaException("Could not create ParameterMetaData from ScriptParameterSet as one parameter's class could not be found or mapped to a scalar datatype. "+e.getMessage());
			}			
			descriptor.setDataType(dataType);
			descriptor.setDataTypeName(DataType.getTypeName(dataType));
			descriptor.setParameterName(parameter.getScriptParameterID());
			descriptor.setRealDataTypeName(parameter.getScriptParameterClassName());
			result.addParameterDescriptor(descriptor);
		}
		if (logger.isDebugEnabled()) {
			try {
				for (int i = 0; i < result.getParameterCount(); i++) {
					logger.debug("Parameter "+(i+1)+": "+result.getParameterName(i+1)+", "+result.getParameterTypeName(i+1));
				}
			} catch (OdaException e) {
				throw new JFireReportingOdaException(e);
			}
		}
		return result;
	}

}
