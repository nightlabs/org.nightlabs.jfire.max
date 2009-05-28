package org.nightlabs.jfire.issue.history;

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueCommentHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueAssigneeHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Three unique cases exist here:
		//  1. ASSIGNED: Old Issue has no Assignee, and in the new Issue it has one assigned.
		//  2. UN-ASSIGNED: Old Issue has an Assignee, and in the new Issue, none exists.
		//  3. RE-ASSIGNED: Old and new Issue's Assignees are different.
		User oldAssignee = oldPersistentIssue.getAssignee();
		User newAssignee = newDetachedIssue.getAssignee();

		String oldAssigneeName = oldAssignee == null ? "" : oldAssignee.getName();
		String newAssigneeName = newAssignee == null ? "" : newAssignee.getName();

		if ( oldAssigneeName.isEmpty() && !newAssigneeName.isEmpty()     // Case 1
			 || oldAssigneeName.isEmpty() && !newAssigneeName.isEmpty()  // Case 2
			 || !oldAssigneeName.equals(newAssigneeName) )               // Case 3
		{
			return IssueHistoryItemFactory.makeItemIntoCollection( new IssueAssigneeHistoryItem(user, oldPersistentIssue, oldAssigneeName, newAssigneeName) );
		}

		return null;
	}

}
