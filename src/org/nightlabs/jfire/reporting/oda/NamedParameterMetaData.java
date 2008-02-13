/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;

/**
 * Interface to extend the functionality of ODA's {@link IParameterMetaData}
 * with parameter names.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface NamedParameterMetaData extends IParameterMetaData {
	/**
	 * Return the name of the given parameter.
	 * 
	 * @param pPosition The (1-based) parameter position.
	 * @return The name of the given parameter.
	 */
	public String getParameterName(int pPosition);
}
