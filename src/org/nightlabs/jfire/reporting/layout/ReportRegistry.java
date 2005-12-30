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

/**
 * Singleton to hold the id of next new {@link org.nightlabs.jfire.reporting.layout.ReportRegistryItem}.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="datastore"
 *		detachable="true"
 *		table="JFireReporting_ReportRegistry"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ReportRegistry {

	private long newReportCategoryID = 0;
	
	/**
	 * 
	 */
	public ReportRegistry() {
		super();
	}
	
	public long getNewReportCategoryID() {
		return newReportCategoryID;
	}

	/**
	 * Returns the current newReportCategoryID
	 * before incrementing it by one. Equivalent to 
	 * <code>newReportCategoryID++</code>
	 */
	public long createNewReportCategoryID() {
		long result = getNewReportCategoryID();
		newReportCategoryID = result + 1;
		return result;
	}
	
	public static ReportRegistry getReportCategoryRegistry(PersistenceManager pm) {
		Iterator it = pm.getExtent(ReportRegistry.class).iterator();
		if (it.hasNext()) {
			return (ReportRegistry)it.next();
		}
		else {
			ReportRegistry reportCategoryRegistry = new ReportRegistry();
			pm.makePersistent(reportCategoryRegistry);
			return reportCategoryRegistry;
		}
		
	}

}
