package org.nightlabs.jfire.issue.history;

import org.nightlabs.jfire.issue.Issue;

/**
 * These shall indicate ALL possible 'actions' affected to an Issue in a save routine,
 * after comparing the old and new {@link Issue}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 * @deprecated Not to be in the abstract base, but just for those subclasses that really need it.
 */
public enum IssueHistoryAction {
	ADDED, REMOVED, UPDATED, CHANGED

	// TODO Add support for language options.
}
