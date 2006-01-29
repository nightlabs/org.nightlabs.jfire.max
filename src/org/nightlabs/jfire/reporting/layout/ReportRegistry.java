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

package org.nightlabs.jfire.reporting.layout;

import java.util.Iterator;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.reporting.layout.id.ReportRegistryID;

/**
 * Singleton to hold the id of next new {@link org.nightlabs.jfire.reporting.layout.ReportRegistryItem}.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class = "org.nightlabs.jfire.reporting.layout.id.ReportRegistryID"
 *		detachable="true"
 *		table="JFireReporting_ReportRegistry"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.inheritance strategy="new-table"
 */
public class ReportRegistry {

	
	/**
	 * @jdo.field primary-key="true"
	 */
	private int reportRegistryID = 0;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long newReportItemID = 0;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected ReportRegistry() {
		super();
	}
	
	public ReportRegistry(int reportRegistryID) {
		this.reportRegistryID = reportRegistryID;
	}
	
	public long getNewReportItemID() {
		return newReportItemID;
	}

	/**
	 * Returns the current newReportCategoryID
	 * before incrementing it by one. Equivalent to 
	 * <code>newReportCategoryID++</code>
	 */
	public long createNewReportItemID() {
		long result = getNewReportItemID();
		newReportItemID = result + 1;
		return result;
	}

	public static final int SINGLETON_REGISTRY_ID = 0;
	public static final ReportRegistryID SINGLETON_ID = ReportRegistryID.create(SINGLETON_REGISTRY_ID); 
	
	public static ReportRegistry getReportRegistry(PersistenceManager pm) {
		Iterator it = pm.getExtent(ReportRegistry.class).iterator();
		if (it.hasNext()) {
			return (ReportRegistry)it.next();
		}
		else {
			ReportRegistry reportCategoryRegistry = new ReportRegistry(SINGLETON_REGISTRY_ID);
			pm.makePersistent(reportCategoryRegistry);
			return reportCategoryRegistry;
		}
		
	}

}
