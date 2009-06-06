package org.nightlabs.jfire.issue.history;

import org.nightlabs.jfire.issue.Issue;

/**
 * These shall indicate the possible 'actions' affected to an Issue in a save routine,
 * after comparing the old and new {@link Issue}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public enum IssueHistoryItemAction {
	// So far, this is used in
	//   (i) IssueMarkerHistoryItem, and
	//  (ii) IssueFileAttachmentHistoryItem

	ADDED, REMOVED
}
