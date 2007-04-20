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

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.reporting.layout.render.RenderManager;

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
	
		public PersistenceManager getPersistenceManager() {
			return pm;
		}
		
		public void setPersistenceManager(PersistenceManager pm) {
			this.pm = pm;
		}
		
		public Map<String, Object> getVars() {
			return vars;
		}

		public void open(PersistenceManager pm, boolean closePM) {
			JFireReportingHelper.logger.debug("Opening (JFireReporting)Helper with pm = "+pm+" and closePM="+closePM);
			this.closePM = closePM;
			setPersistenceManager(pm);
			getVars().clear();
		}
		
		public void close() {
			JFireReportingHelper.logger.debug("Closing (JFireReporting)Helper with pm = "+pm+" and closePM="+closePM);
			if (pm != null && closePM) {
				JFireReportingHelper.logger.debug("Closing PersisteneManager");
				pm.close();
			}
			getVars().clear();
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
	 * @param closePM Whether to close the pm after using the Helper.
	 */
	public static void open(PersistenceManager pm, boolean closePM) {
		helpers.get().open(pm, closePM);
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
	
}
