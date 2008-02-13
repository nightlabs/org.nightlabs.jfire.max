/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.oda.server.jdojs;

import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.ResultSet;
import org.nightlabs.jfire.reporting.oda.ResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jdojs.AbstractJDOJSProxy;
import org.nightlabs.jfire.reporting.oda.jdojs.JDOJSResultSet;
import org.nightlabs.jfire.reporting.oda.jdojs.JDOJSResultSetMetaData;

/**
 * Server-side JDO JavaScript DataSets. Its method are also called
 * from the client-side(preview) DataSet through the ReportManager-Bean.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerJDOJSProxy extends AbstractJDOJSProxy {

	/**
	 * LOG4J logger used by this class
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
	.getLogger(ServerJDOJSProxy.class);

//	private String organisationID;

//	public ServerJDOJSProxy(String organisationID) {
//	this.organisationID = organisationID;
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#prepare(java.lang.String)
	 */
	public void prepare(String prepareScript) throws OdaException {
		setPrepareScript(prepareScript);
		getMetaData();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws OdaException {
		return prepareJDOJSQuery(getPrepareScript());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws OdaException {
		try {
			return fetchJDOJSResultSet(
					getPersistenceManager(),
					(JDOJSResultSetMetaData)getMetaData(),
					getFetchScript(),
//					getParameterMetaData(),
					getNamedParameters()

			);
		} catch (Exception e) {
			throw new OdaException("Could not execute JDOJSQuery "+e.getClass().getName()+": "+e.getMessage());
		}
	}



	public static final String IMPORT_PACKAGES_PREPARE = null; // "importPackage(Packages.org.nightlabs.jfire.reporting);";
	public static final String IMPORT_PACKAGES_FETCH = "importPackage(Packages.javax.jdo);\n";
	public static final String IMPORT_CLASSES_PREPARE =
		"importClass(Packages."+ResultSetMetaData.class.getName()+");\n"+
		"importClass(Packages."+DataType.class.getName()+");\n"
		;
	public static final String IMPORT_CLASSES_FETCH =
		"importClass(Packages."+ResultSetMetaData.class.getName()+");\n"+
		"importClass(Packages."+ResultSet.class.getName()+");\n"+
		"importClass(Packages."+DataType.class.getName()+");\n"
		;

	public static JDOJSResultSetMetaData prepareJDOJSQuery(String prepareScript) {
		Context context = Context.enter();
		try {
			// Scriptable scope = context.initStandardObjects();
			Scriptable scope = new ImporterTopLevel(context);

			String importClasses = IMPORT_CLASSES_PREPARE;
			if (importClasses != null)
				prepareScript = importClasses + "\n" + prepareScript;

			String importPackages = IMPORT_PACKAGES_PREPARE;
			if (importPackages != null)
				prepareScript = importPackages + "\n" + prepareScript;

			JDOJSResultSetMetaData metaData = new JDOJSResultSetMetaData();

			Object js_metaData = Context.javaToJS(metaData, scope);
			ScriptableObject.putProperty(scope, "metaData", js_metaData);
			if(logger.isDebugEnabled())
				logger.debug("Trying to execute prepare JavaScript:\n"+prepareScript);
			context.evaluateString(scope, prepareScript, "JDOJS prepare script", 1, null);
			try {
				if (metaData.getColumnCount() <= 0)
					throw new IllegalStateException("JDOJS prepare statement "+prepareScript+" did not add at least one ");
			} catch (OdaException e) {
				throw new RuntimeException(e);
			}
			return metaData;
		} finally {
			Context.exit();
		}
	}

	/**
	 * Fetches the ResultSet by executing the given fechtScript.
	 * Before execution the fetchScript is prefixed by the imports for
	 * fech-scripts ({@link #IMPORT_CLASSES_FETCH}, {@link #IMPORT_PACKAGES_FETCH}).
	 * <p>
	 * Additionally three special variables will be deployed into the script:
	 * <ul>
	 *   <li><b>metaData</b>: The result set metadata that was returned by the prepare script.</li>
	 *   <li><b>resultSet</b>: A freshly create result set object that can be filled by the script and should be returned.
	 *   	The object is of type {@link JDOJSResultSet}</li>
	 *   <li><b>persistenceManager</b>: A PersistenceManager the script can use to query its result set</li>
	 * </ul>
	 * <p>
	 * Besides the special variables all parameters defined for this query will be
	 * deployed into the script. Note that ODA does not operate on named parameters,
	 * the names of the parameters that are deployed (in case not defined otherwise by the ParameterMetaData)
	 * will be named like param0, param1, ...
	 * 
	 * @param metaData The meta data of the result set.
	 * @param fetchScript The script to execute.
	 * @param parameters The query's parmeters
	 * @return A resultSet that was filled by the fetchScript.
	 */
	public static JDOJSResultSet fetchJDOJSResultSet(
			PersistenceManager pm,
			JDOJSResultSetMetaData metaData,
			String fetchScript,
//			IParameterMetaData parameterMetaData,
			Map<String, Object> parameters
	)
	{
		try {
			Context context = Context.enter();
			try {
				// Scriptable scope = context.initStandardObjects();
				Scriptable scope = new ImporterTopLevel(context);

				String importClasses = IMPORT_CLASSES_FETCH;
				if (importClasses != null)
					fetchScript = importClasses + "\n" + fetchScript;

				String importPackages = IMPORT_PACKAGES_PREPARE;
				if (importPackages != null)
					fetchScript = importPackages + "\n" + fetchScript;


				Object js_metaData = Context.javaToJS(metaData, scope);
				ScriptableObject.putProperty(scope, "metaData", js_metaData);

				JDOJSResultSet resultSet = new JDOJSResultSet(metaData);

				Object js_resultSet = Context.javaToJS(resultSet, scope);
				ScriptableObject.putProperty(scope, "resultSet", js_resultSet);

				Object js_persistenceManager = Context.javaToJS(pm, scope);
				ScriptableObject.putProperty(scope, "persistenceManager", js_persistenceManager);

				if(logger.isDebugEnabled()) {
					logger.debug("*****************************************");
					logger.debug("*****************************************");
					logger.debug("*****************************************");
					logger.debug("*********   JDOJSDriver Params: ");

					for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
						Map.Entry entry = (Map.Entry) iter.next();
						logger.debug("*********   "+entry.getKey()+": "+entry.getValue());
					}

					logger.debug("*****************************************");
					logger.debug("*****************************************");
					logger.debug("*****************************************");
				}

				// deactivate part above as parameternames are now mapped by Query itself
				for (Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
					Object js_param = Context.javaToJS(paramEntry.getValue(), scope);
					ScriptableObject.putProperty(scope, paramEntry.getKey(), js_param);
				}

				if(logger.isDebugEnabled())
					logger.debug("Trying to execute prepare JavaScript:\n"+fetchScript);
				context.evaluateString(scope, fetchScript, "JDOJS fetch script", 1, null);
				resultSet.init();
				return resultSet;
			} finally {
				Context.exit();
			}
		} finally {
//			pm.close();
		}
	}
}

