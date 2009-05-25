package org.nightlabs.jfire.issue.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.security.User;

public abstract class IssueHistoryItemFactory {

	public static Collection<IssueHistory> createIssueHistoryItems(PersistenceManager pm, User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	{
		Collection<IssueHistory> result = new ArrayList<IssueHistory>();

		for (Iterator<IssueHistoryItemFactory> it = pm.getExtent(IssueHistoryItemFactory.class).iterator(); it.hasNext(); ) {
			IssueHistoryItemFactory factory = it.next();

			Collection<IssueHistory> issueHistoryItems;
			try {
				issueHistoryItems = factory.createIssueHistoryItems(user, oldPersistentIssue, newDetachedIssue);
			} catch (JDODetachedFieldAccessException x) {
				// In case the factory touches a non-detached field, we silently ignore this (a non-detached field could not be modified anyway).
				issueHistoryItems = null;
			}
			if (issueHistoryItems != null)
				result.addAll(issueHistoryItems);
		}

		return result;
	}

	public IssueHistoryItemFactory() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create {@link IssueHistory}s for every detected modification. Be aware of non-detached fields! If your implementation of
	 * {@link IssueHistoryItemFactory} is responsible for multiple fields, you <b>must</b> catch {@link JDODetachedFieldAccessException}s yourself.
	 * If you access only one field, you can safely throw the {@link JDODetachedFieldAccessException} - the framework will consider this as
	 * no changes detected by your implementation.
	 *
	 * @param user the user responsible for the change.
	 * @param oldPersistentIssue the old issue, which is currently persistent (i.e. connected to the datastore).
	 * @param newDetachedIssue the new issue as modified by the client - not yet persisted.
	 * @return <code>null</code> or a {@link Collection} of {@link IssueHistory}s representing all modifications.
	 * @throws JDODetachedFieldAccessException in case an implementation of this method is only taking <b>one</b> field of the <code>newDetachedIssue</code> into account, it does not need to catch these exceptions itself.
	 */
	public abstract Collection<IssueHistory> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException;
}
