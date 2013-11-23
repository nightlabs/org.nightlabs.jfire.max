package org.nightlabs.jfire.issue.history;

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueWorkingStatusHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueWorkingStatusHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// This handles the case when the START (or STOP) button in the 'Work Time' section is pressed.
		// Note: The UserSearchDialog is automatically invoked if no Assignee is yet assigned to the current Issue.
		//       Only when there is a valid User associated to the Issue can the START method be properly carried
		//       out; which stamps the time (and date) indicating when the Issue is being worked on. Also, upon
		//       selecting a User form the UserSearchDialog, a 'doSave()' is triggered from the main Editor page --
		//       and incidently saving all other fields that has been recently changed and were not previously saved.
		if (oldPersistentIssue.isStarted() != newDetachedIssue.isStarted())
			return IssueHistoryItemFactory.makeItemIntoCollection( new IssueWorkingStatusHistoryItem(user, oldPersistentIssue, newDetachedIssue.isStarted()) );

		return null;
	}

}
