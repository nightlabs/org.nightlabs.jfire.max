package org.nightlabs.jfire.issue.issuemarker;

import org.nightlabs.jfire.issue.Issue;

/**
 * These shall indicate ALL possible 'actions' affected to an Issue in a save routine,
 * after comparing the old and new {@link Issue}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public enum IssueMarkerHistoryItemAction {
	ADDED, REMOVED
}
