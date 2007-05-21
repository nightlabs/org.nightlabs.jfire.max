/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.oda.Query;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * Common implementation of the JFS ODA Driver, that is used in server and client
 * and can resolve the {@link Script} the driver's data set is linked to.
 * <p>
 * The query parameter of the ODA dataset linked to this driver will be assumed
 * to be a string reference of a {@link ScriptRegistryItemID}, referencing
 * as {@link Script} not a category.
 *  
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

	@Override
	public IParameterMetaData getParameterMetaData() throws OdaException {
		// TODO: Implement getParameterMetaData for JFS Scripts (Possible with ParameterSet)
		return super.getParameterMetaData();
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
		ObjectID idObject = ObjectIDUtil.createObjectID(query);
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
	
	/**
	 * Delegates to {@link JFireReportingHelper#getDataSetParamObject(Object)}
	 * with the parameter obtained by {@link #getParameter(String)}.
	 * 
	 * @param name The parameter name.
	 * @return The parameter (possibly de-serialized) with the given name.
	 */
	protected Object getDataSetParameter(String name) {
		return JFireReportingHelper.getDataSetParamObject(getParameter(name));
	}

}
