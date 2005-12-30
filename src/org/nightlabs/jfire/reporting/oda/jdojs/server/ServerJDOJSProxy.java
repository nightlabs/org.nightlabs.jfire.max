/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jdojs.server;

import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.reporting.oda.DataType;
import org.nightlabs.jfire.reporting.oda.ResultSet;
import org.nightlabs.jfire.reporting.oda.ResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jdojs.AbstractJDOJSProxy;
import org.nightlabs.jfire.reporting.oda.jdojs.JDOJSResultSet;
import org.nightlabs.jfire.reporting.oda.jdojs.JDOJSResultSetMetaData;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector.UserDescriptor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerJDOJSProxy extends AbstractJDOJSProxy {
	
//	private String organisationID;
//	
//	public ServerJDOJSProxy(String organisationID) {
//		this.organisationID = organisationID;
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
					(JDOJSResultSetMetaData)getMetaData(), 
					getFetchScript(),
					getParameterMetaData(),
					getParameters()
					
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
			System.out.println("Trying to execute prepare JavaScript:\n"+prepareScript);
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
	
	public static JDOJSResultSet fetchJDOJSResultSet(
			JDOJSResultSetMetaData metaData, 
			String fetchScript,
			IParameterMetaData parameterMetaData,
			Map parameters
			
		)
	throws ModuleException
	{
		try {
			SecurityReflector securityReflector = SecurityReflector.lookupSecurityReflector(new InitialContext());
			UserDescriptor userDescriptor = securityReflector.whoAmI();

			Context context = Context.enter();
			Lookup lookup = null;
			lookup = new Lookup(userDescriptor.getOrganisationID());
			PersistenceManager pm = null;
			try {
				pm = lookup.getPersistenceManager();
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
					
					System.out.println("*****************************************");
					System.out.println("*****************************************");
					System.out.println("*****************************************");
					System.out.println("*********   JDOJSDriver Params: ");
					
					for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
						Map.Entry entry = (Map.Entry) iter.next();
						System.out.println("*********   "+entry.getKey()+": "+entry.getValue());
						int paramId = ((Integer)entry.getKey()).intValue();
						
						String paramName = (parameterMetaData == null) ? null : parameterMetaData.getParameterTypeName(paramId);
						if (paramName == null)
							paramName = "p_"+paramId;
						else
							paramName = "p_"+paramName;
						Object js_param = Context.javaToJS(entry.getValue(), scope);
						ScriptableObject.putProperty(scope, paramName, js_param);
						
					}
					System.out.println("*****************************************");
					System.out.println("*****************************************");
					System.out.println("*****************************************");
					
					System.out.println("Trying to execute prepare JavaScript:\n"+fetchScript);
					context.evaluateString(scope, fetchScript, "JDOJS fetch script", 1, null);
					resultSet.init();
					return resultSet;
				} finally {
					Context.exit();
				}
			} finally {
				pm.close();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (ModuleException e) {
			throw e;
		} catch (Exception e) {
			throw new ModuleException(e);
		}
	}
}
