package org.nightlabs.jfire.issue.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.issuemarker.IssueMarkerHistoryItem;
import org.nightlabs.jfire.security.User;

public abstract class IssueHistoryItemFactory {
	/**
	 * Quick reference: This lists down all known {@link IssueMarkerHistoryItem}s that should be generated
	 * whenever an {@link Issue} is saved.
	 */
	private static final Class<?>[] factories = {
		IssueMarkerHistoryItem.class,

		// Retrieved the following from the previous handling of tracking historical changes in an Issue
		// from the original class IssueHistoryItem.generateHistory():
//		IssueDescriptionHistoryItemFactoryName.class,
//		IssueCommentHistoryItemFactoryName.class,
//		IssueSubjectHistoryItemFactoryName.class,
//		IssueAssigneeHistoryItemFactoryName.class,
//		IssueReporterHistoryItemFactoryName.class,
//		IssueFileAttachmentsHistoryItemFactoryName.class,
//		IssuePriorityHistoryItemFactoryName.class,
//		IssueSeverityTypeHistoryItemFactoryName.class,
//		IssueResolutionHistoryItemFactoryName.class,
//		IssueTypeHistoryItemFactoryName.class,
//		IssueStateDefinitionHistoryItemFactoryName.class,
//		IssueStatusHistoryItemFactoryName.class,

		// QN: Is every single one of the above necessary?
	};

	private static Collection<IssueHistoryItemFactory> getIssueHistoryItemFactories() {
		Collection<IssueHistoryItemFactory> result = new ArrayList<IssueHistoryItemFactory>();
		for (Class<?> clazz : factories) {
			IssueHistoryItemFactory factory;
			try {
				factory = (IssueHistoryItemFactory)clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			result.add(factory);
		}
		return result;
	}

	/**
	 * This calls upon all known {@link IssueHistoryItemFactory} instances, from which we access the method {@link IssueHistoryItemFactory.#createIssueHistoryItems(User, Issue, Issue)},
	 * which will in turn generate a unique collection of {@link IssueHistoryItem}s corresponding to the {@link IssueHistoryItemFactory}.
	 *
	 * @return a {@link Collection} of {@link IssueHistoryItem}s based on the comparisons between a given old-persistent {@link Issue}
	 * and a given newly-detached {@link Issue}.
	 */
	public static Collection<IssueHistoryItem> createIssueHistoryItems(PersistenceManager pm, User user, Issue oldPersistentIssue, Issue newDetachedIssue) {
		Collection<IssueHistoryItem> result = new ArrayList<IssueHistoryItem>();

//		for (Iterator<IssueHistoryItemFactory> it = pm.getExtent(IssueHistoryItemFactory.class).iterator(); it.hasNext(); ) {
		for (Iterator<IssueHistoryItemFactory> it = getIssueHistoryItemFactories().iterator(); it.hasNext(); ) {
			IssueHistoryItemFactory factory = it.next();

			Collection<IssueHistoryItem> issueHistoryItems;
			try {
				issueHistoryItems = factory.createIssueHistoryItems(user, oldPersistentIssue, newDetachedIssue);
			} catch (JDODetachedFieldAccessException x) {
				// In case the factory touches a non-detached field, we silently ignore this (a non-detached field could not be modified anyway).
				issueHistoryItems = null;
			}

			if (issueHistoryItems != null)
				result.addAll( issueHistoryItems );
		}

		return result;
	}




	// -------------------------------------------------------------------------------------------------------------------------|
	protected IssueHistoryItemFactory() { }

	/**
	 * Create {@link IssueHistoryItem}s for every detected modification. Be aware of non-detached fields! If your implementation of
	 * {@link IssueHistoryItemFactory} is responsible for multiple fields, you <b>must</b> catch {@link JDODetachedFieldAccessException}s yourself.
	 * If you access only one field, you can safely throw the {@link JDODetachedFieldAccessException} - the framework will consider this as
	 * no changes detected by your implementation.
	 *
	 * @param user the user responsible for the change.
	 * @param oldPersistentIssue the old issue, which is currently persistent (i.e. connected to the datastore).
	 * @param newDetachedIssue the new issue as modified by the client - not yet persisted.
	 * @return <code>null</code> or a {@link Collection} of {@link IssueHistoryItem}s representing all modifications.
	 * @throws JDODetachedFieldAccessException in case an implementation of this method is only taking <b>one</b> field of the <code>newDetachedIssue</code> into account, it does not need to catch these exceptions itself.
	 */
	public abstract Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException;
}
