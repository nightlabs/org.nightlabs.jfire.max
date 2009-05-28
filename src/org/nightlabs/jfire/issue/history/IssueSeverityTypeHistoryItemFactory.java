package org.nightlabs.jfire.issue.history;

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueSeverityTypeHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueSeverityTypeHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Severity is modified from a drop-down combo, and will always be assigned a value.
		// Also: (i) No one can should be able to modify the 'name' field of an IssueSeverityType.
		//       (ii) There exists only a finite number of IssueSeverityTypes.
		IssueSeverityType oldSeverityType = oldPersistentIssue.getIssueSeverityType();
		IssueSeverityType newSeverityType = newDetachedIssue.getIssueSeverityType();

		// So, we only to check whether the IssueSeverityTypes have changed or not.
		if ( !oldSeverityType.equals(newSeverityType) )
			return IssueHistoryItemFactory.makeItemIntoCollection( new IssueSeverityTypeHistoryItem(user, oldPersistentIssue, oldSeverityType, newSeverityType) );

		return null;
	}

}
