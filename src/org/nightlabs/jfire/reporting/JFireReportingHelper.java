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

package org.nightlabs.jfire.reporting;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.reporting.layout.render.RenderManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * Helper to be used static by the ODA runtime driver implementations
 * as well as from within the BIRT layouts.
 * <p>
 * It provides access to an {@link PersistenceManager} which is set by
 * the {@link RenderManager} prior to the rendering of reports.
 * <p>
 * Additionally it serves as a storage for global report values
 * that are also accessible for the ODA drivers. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireReportingHelper {

	/**
	 * The Logger used by this class.
	 */
	private static Logger logger = Logger.getLogger(JFireReportingHelper.class);
	
	/**
	 * Private class that keeps the helper per thread. 
	 */
	private static class Helper {
		private boolean closePM;
		private PersistenceManager pm;		
		private Map<String, Object> vars = new HashMap<String, Object>();
		private Map<String, Object> parameters = new HashMap<String, Object>();
		private Locale locale;
	
		public PersistenceManager getPersistenceManager() {
			return pm;
		}
		
		public void setPersistenceManager(PersistenceManager pm) {
			this.pm = pm;
		}
		
		public Map<String, Object> getVars() {
			return vars;
		}

		public Map<String, Object> getParameters() {
			return parameters;
		}
		
		public Locale getLocale() {
			return locale;
		}
		
		public void open(PersistenceManager pm, Map<String, Object> params, Locale locale, boolean closePM) {
			JFireReportingHelper.logger.debug("Opening (JFireReporting)Helper with pm = "+pm+" and closePM="+closePM);
			this.closePM = closePM;
			this.locale = locale;
			setPersistenceManager(pm);
			getVars().clear();
			getParameters().clear();
			getParameters().putAll(params);
		}
		
		public void close() {
			JFireReportingHelper.logger.debug("Closing (JFireReporting)Helper with pm = "+pm+" and closePM="+closePM);
			if (pm != null && closePM) {
				JFireReportingHelper.logger.debug("Closing PersisteneManager");
				pm.close();
			}
			getVars().clear();
			getParameters().clear();
		}
	}
	
	private static ThreadLocal<Helper> helpers = new ThreadLocal<Helper>() {
		@Override
		protected Helper initialValue() {
			return new Helper();
		}
	};
	
	
	/**
	 * Protected constructor. Class is for static use. 
	 */
	protected JFireReportingHelper() {
	}

	/**
	 * Open (re-initialize) the helper associated to the current
	 * thread (currently rendered report). 
	 * <p>
	 * Do not call this method direcly, it is called by the {@link RenderManager}.+
	 * 
	 * @param pm The PersistenceManager to use
	 * @param params The report params set for the next execution.
	 * @param locale The Locale used by this helper for the next run.
	 * @param closePM Whether to close the pm after using the Helper.
	 */
	public static void open(PersistenceManager pm, Map<String, Object> params, Locale locale, boolean closePM) {
		helpers.get().open(pm, params, locale, closePM);
	}
	
	/**
	 * Close the Helper after the report has been rendered.
	 * <p>
	 * Do not call this method direcly, it is called by the {@link RenderManager}.+
	 */
	public static void close() {
		helpers.get().close();
	}

	/**
	 * Get the {@link PersistenceManager} associated to the current thread
	 * (execution of the current report).
	 * 
	 * @return The {@link PersistenceManager} associated to the current thread
	 * (execution of the current report).
	 */
	public static PersistenceManager getPersistenceManager() {
		return helpers.get().getPersistenceManager();
	}
	
	/**
	 * Get the value of a named variable associated to the current thread
	 * (execution of the current report).
	 * 
	 * @param varName The variable name.
	 * @return The value of a named variable associated to the current thread
	 * (execution of the current report).
	 */
	public static Object getVar(String varName) {
		return helpers.get().getVars().get(varName);
	}
	
	/**
	 * Get the map of all named varialbes associated to the current thread
	 * (execution of the current report). 
	 * 
	 * @return The map of all named varialbes associated to the current thread
	 * (execution of the current report).
	 */
	public static Map<String, Object> getVars() {
		return helpers.get().getVars();
	}
	
	/**
	 * Get the map of all parameters of the currently running report.
	 * This will be set by {@link RenderManager} before starting rendering the report.
	 * 
	 * @return The map named parameters for the currently running report.
	 */
	public static Map<String, Object> getParameters() {
		return helpers.get().getParameters();
	}
	
	/**
	 * Get the parameter with the given name for the currently running report.
	 * 
	 * @return The parameter with the given name for the currently running report.
	 * @see #getParameters()
	 */
	public static Object getParameter(String name) {
		return helpers.get().getParameters().get(name);
	}
	
	/**
	 * Returns the locale the report for this helper currently runs with.
	 * @return The locale the report for this helper currently runs with.
	 */
	public static Locale getLocale() {
		return helpers.get().getLocale();
	}
	
	/**
	 * Returns the JDO object with the given jdo id. Assumes that the given
	 * {@link String} parameter is the string representation of an {@link ObjectID}
	 * and tries to get this Object from the datastore.
	 *  
	 * @param jdoIDString The {@link String} representation of the {@link ObjectID} of the object to retrieve.	 * 
	 * @return The persistent object of with the given id or null if this can not be found. 
	 */
	public static Object getJDOObject(String jdoIDString) {
		ObjectID id = ObjectIDUtil.createObjectID(jdoIDString);
		Object result = null;
		try {
			result = helpers.get().getPersistenceManager().getObjectById(id);
		} catch (JDOObjectNotFoundException e) {
			result = null;
		}
		return result;
	}
	
	private static final Pattern dataSetParamPattern = Pattern.compile("^<dataSetParameter");
	private static final String dataSetparameterOpenTag = "<dataSetParameter>";
	private static final String dataSetparameterCloseTag = "</dataSetParameter>";
	
	/**
	 * Creates a String representation of the given object that
	 * can be used as parameter for JFS data sets.  
	 * <p>
	 * Currently, the XStream serializer is used for this purpose.
	 * </p>
	 * @param obj The object to serialize and 
	 * @return A String serialization of the given object.
	 */
	public static String createDataSetParam(Object obj) {
		XStream xStream = new XStream(new XppDriver());
		StringWriter writer = new StringWriter();
		writer.append(dataSetparameterOpenTag);
		xStream.toXML(obj, writer);
		writer.append(dataSetparameterCloseTag);
		return writer.toString();
	}

	/**
	 * This method expects to receive a data set parameter as it is passed
	 * by the ODA driver to the {@link IQuery} implementation.
	 * <p>
	 * This method then checks whether the parameter was serialized using the
	 * {@link #createDataSetParam(Object)} method and returns the appropriate
	 * de-serialized object than. Otherwise it will return the object as
	 * it is passed (i.e. passing <code>null</code> will return <code>null</code> as well). 
	 * </p>
	 * @param obj The data set parameter.
	 * @return The data set parameter either de-serialized to the object it originally was or as passed to this method.
	 */
	public static Object getDataSetParamObject(Object obj) {
		if (obj == null)
			return null;
		if (!(obj instanceof String))
			return obj;
		String paramStr = (String) obj;
		if (dataSetParamPattern.matcher(paramStr).find()) {
			String stripped = paramStr.replace(dataSetparameterOpenTag, "");
			stripped = stripped.replace(dataSetparameterCloseTag, "");
			XStream xStream = new XStream(new XppDriver());
			return xStream.fromXML(stripped);
		} else
			return obj;			
	}
	
	/**
	 * Pattern used for {@link #setBRLineBreaks(String)}
	 */
	private static final Pattern patLineBreak = Pattern.compile("\\r?\\n");
	
	/**
	 * Returns the given Sting with all occurences of the Pattern \r?\n with &lt;br/&gt;.
	 * 
	 * @param str The String to replace the linebreaks in. 
	 * @return The given Sting with all occurences of the Pattern \r?\n with &lt;br/&gt;.
	 */
	public static String setBRLineBreaks(String str) {
		if (str == null)
			return null;
		return patLineBreak.matcher(str).replaceAll("<br/>");
	}
}
