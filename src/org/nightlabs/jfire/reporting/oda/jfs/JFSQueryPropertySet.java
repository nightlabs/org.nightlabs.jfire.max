/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * This property set is mainly used to pass options to 
 * queries that need to be available before the meta-data
 * is created. Unfortunately the actual parameters are
 * available only later (after prepare + getResultSetMetaData).
 * <p>
 * The {@link JFSQueryPropertySet} of a JFS query are created
 * by XML serialisation into and de-serialisation from the 
 * query string. 
 * </p> 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFSQueryPropertySet implements Serializable {

	private static final long serialVersionUID = 1L;

	private ScriptRegistryItemID scriptRegistryItemID;
	
	private Map<String, String> properties;
	
	/**
	 * 
	 */
	public JFSQueryPropertySet() {
		this.properties = new HashMap<String, String>();
	}

	/**
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * @return the scriptRegistryItemID
	 */
	public ScriptRegistryItemID getScriptRegistryItemID() {
		return scriptRegistryItemID;
	}

	/**
	 * @param reportRegistryItemID the reportRegistryItemID to set
	 */
	public void setScriptRegistryItemID(ScriptRegistryItemID scriptRegistryItemID) {
		this.scriptRegistryItemID = scriptRegistryItemID;
	}

}
