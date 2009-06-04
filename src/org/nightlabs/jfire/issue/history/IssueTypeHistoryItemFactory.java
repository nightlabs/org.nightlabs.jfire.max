package org.nightlabs.jfire.issue.history;

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueTypeHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueTypeHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Note: Not sure if we even should do this? Since the UI does not allow an Issue Type to be modified. Kai.

		// Also: (i) No one can should be able to modify the 'name' field of an IssueType.
		//       (ii) There exists only a finite number of IssueTypes.
		IssueType oldIssueType = oldPersistentIssue.getIssueType();
		IssueType newIssueType = newDetachedIssue.getIssueType();

		// So, we only to check whether the IssueTypes have changed or not.
		if ( !oldIssueType.equals(newIssueType) )
			return IssueHistoryItemFactory.makeItemIntoCollection( new IssueTypeHistoryItem(user, oldPersistentIssue, oldIssueType, newIssueType) );

		return null;
	}

}
