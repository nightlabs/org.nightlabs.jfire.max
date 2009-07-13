package org.nightlabs.jfire.chezfrancois;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.security.User;

/**
 * Demo {@link DataCreator} with respect to the IssueTracking project.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class DataCreatorIssueTracking extends DataCreator {
	/**
	 * Creates a new instance of a DataCreatorIssueTracking.
	 */
	public DataCreatorIssueTracking(PersistenceManager pm, User user) {
		super(pm, user);
	}

	/**
	 * Creates a new demo {@link Issue}.
	 */
	public Issue createDemoIssue() {
		return null;
	}
}
