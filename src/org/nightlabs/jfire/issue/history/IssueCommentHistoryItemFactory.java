package org.nightlabs.jfire.issue.history;

import java.util.ArrayList;
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
public class IssueCommentHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// An IssueComment, it seems, can neither be removed nor updated/changed/edited.
		// So, this makes it very easy, and that we only need to check if a new IssueComment has beed ADDED.
		Collection<IssueHistoryItem> issueCommentHistoryItems = new ArrayList<IssueHistoryItem>();
		if (oldPersistentIssue.getComments().size() < newDetachedIssue.getComments().size()) {
			// And, it also seems, that only ONE comment is allowed to be added per save.
			// QN: Can I assume that the List of IssueComments are already sorted according to the times they were created?
			//     Or at least the new comment(s) is(are) outside the index-range of the old comment's list.
			for (int i=oldPersistentIssue.getComments().size(); i<newDetachedIssue.getComments().size(); i++)
				issueCommentHistoryItems.add( new IssueCommentHistoryItem(user, oldPersistentIssue, newDetachedIssue.getComments().get(i).getText()) );
		}

		return issueCommentHistoryItems;
	}

}
