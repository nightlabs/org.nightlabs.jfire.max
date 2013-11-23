package org.nightlabs.jfire.issue.history;

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssuePriorityHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssuePriorityHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Priority is modified from a drop-down combo, and will always be assigned a value.
		// Also: (i) No one can should be able to modify the 'name' field of an IssuePriority.
		//       (ii) There exists only a finite number of IssuePriorities.
		IssuePriority oldIssuePriority = oldPersistentIssue.getIssuePriority();
		IssuePriority newIssuePriority = newDetachedIssue.getIssuePriority();

		// So, we only to check whether the IssuePriorities have changed or not.
		if ( !Util.equals(oldIssuePriority, newIssuePriority) )
			return IssueHistoryItemFactory.makeItemIntoCollection( new IssuePriorityHistoryItem(user, oldPersistentIssue, oldIssuePriority, newIssuePriority) );

		return null;
	}

}
