package org.nightlabs.jfire.issue.issuemarker;

import org.nightlabs.jfire.issue.Issue;

/**
 * These shall indicate ALL possible 'actions' affected to an Issue in a save routine,
 * after comparing the old and new {@link Issue}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@Deprecated
public enum IssueMarkerHistoryItemAction {
	// Use instead the more general org.nightlabs.jfire.issue.history.IssueHistoryItemAction.
	ADDED, REMOVED
}
