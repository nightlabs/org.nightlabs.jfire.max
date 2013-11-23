package org.nightlabs.jfire.issue.history;

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueProjectHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueProjectHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Project is modified from a drop-down combo, and will always be assigned a value.
		// Also: (i) No one can should be able to modify the 'name' field of a Project.
		//       (ii) There exists only a finite number of Projects.
		Project oldProject = oldPersistentIssue.getProject();
		Project newProject = newDetachedIssue.getProject();

		// So, we only to check whether the Projects have changed or not.
		if ( !Util.equals(oldProject, newProject) )
			return IssueHistoryItemFactory.makeItemIntoCollection( new IssueProjectHistoryItem(user, oldPersistentIssue, oldProject, newProject) );

		return null;
	}

}
