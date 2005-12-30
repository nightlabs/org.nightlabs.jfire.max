package org.nightlabs.jfire.reporting.layout;

import java.util.Set;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface NestableReportRegistryItem {
	
	public ReportRegistryItem getParentItem();
	public Set<ReportRegistryItem> getChildItems();
	
}
