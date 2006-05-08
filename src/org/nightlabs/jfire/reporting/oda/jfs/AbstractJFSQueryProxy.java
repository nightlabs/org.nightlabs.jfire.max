/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.reporting.oda.Query;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public abstract class AbstractJFSQueryProxy extends Query implements IJFSQueryProxy {

	private ScriptRegistryItemID scriptRegistryItemID;
	
	/**
	 * 
	 */
	public AbstractJFSQueryProxy() {
		super();
	}

	/**
	 * Checks if the query is set to an JDO-ObjectID String and
	 * creates a ScriptRegistryID if possible.
	 * 
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#prepare(java.lang.String)
	 */
	public void prepare(String query) throws OdaException {
		if (!query.startsWith("jdo/"))
			throw new IllegalArgumentException("Queries for JFS DataSets have to refer to a JFire ScriptRegistryItemID, but instead "+query+" was passed.");
		Object idObject = ObjectIDUtil.createObjectID(query);
		if (!(idObject instanceof ScriptRegistryItemID))
			throw new IllegalArgumentException("The query string of this JFS DataSet does refer to a JDO object but not to an ScriptRegistryItem. The query was "+query);
		scriptRegistryItemID = (ScriptRegistryItemID)idObject;
	}
	
	/**
	 * Returns the scriptRegistryItemID that is set on {@link #prepare(String)}
	 * @param checkPrepare If true an IllegalStateException will be thrown if the id is null (= prepare was not called)
	 */
	public ScriptRegistryItemID getScriptRegistryItemID(boolean checkPrepare) {
		if (checkPrepare) {
			if (scriptRegistryItemID == null)
				throw new IllegalStateException("The scriptRegistryItemID is null, this indicates that prepare(String) was not called prior to this method.");
		}
		return scriptRegistryItemID;
	}
	
	/**
	 * Returns the scriptRegistryItemID that is set on {@link #prepare(String)}
	 * and checks for prepare.
	 * 
	 * @see #getScriptRegistryItemID(boolean)
	 */
	public ScriptRegistryItemID getScriptRegistryItemID() {
		return getScriptRegistryItemID(true);
	}

}
