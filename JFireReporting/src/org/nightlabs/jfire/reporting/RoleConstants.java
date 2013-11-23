package org.nightlabs.jfire.reporting;

import org.nightlabs.jfire.security.id.RoleID;

public class RoleConstants {
	public static final String editReport_roleID = "org.nightlabs.jfire.reporting.editReport";
	public static final String renderReport_roleID = "org.nightlabs.jfire.reporting.renderReport";
	
	public static final RoleID editReport = RoleID.create(editReport_roleID);
	public static final RoleID renderReport = RoleID.create(renderReport_roleID);
}
