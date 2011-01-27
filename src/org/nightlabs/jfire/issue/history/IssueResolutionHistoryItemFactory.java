package org.nightlabs.jfire.issue.history;

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueResolution;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;


/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueResolutionHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueResolutionHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Resolution is modified from a drop-down combo, and will always be assigned a value.
		// Also: (i) No one can should be able to modify the 'name' field of an IssueResolution.
		//       (ii) There exists only a finite number of IssueResolutions.
		IssueResolution oldIssueResolution = oldPersistentIssue.getIssueResolution();
		IssueResolution newIssueResolution = newDetachedIssue.getIssueResolution();

		// So, we only to check whether the IssueResolution have changed or not.
		if ( !Util.equals(oldIssueResolution, newIssueResolution) )
			return IssueHistoryItemFactory.makeItemIntoCollection( new IssueResolutionHistoryItem(user, oldPersistentIssue, oldIssueResolution, newIssueResolution) );

		return null;
	}

}
