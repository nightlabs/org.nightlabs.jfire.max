/**
 * 
 */
package org.nightlabs.ipanema.reporting.layout;

import java.util.Iterator;

import javax.jdo.PersistenceManager;

/**
 * Singleton to hold the id of next new {@link org.nightlabs.ipanema.reporting.layout.ReportRegistryItem}.
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
