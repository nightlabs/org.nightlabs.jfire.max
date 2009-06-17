package org.nightlabs.jfire.issue.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueLinkHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueLinkHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Note: Most interesting one yet! This should deal with mainly two things:
		//        (i) the Object linked to the current Issue, and
		//       (ii) the 'relationship' of the link between the Issue and the Object.
		//
		// Also, handling IssueLinks is definitely not a straight-forward task. Suppose that the
		// linkedObject of the current Issue is also an Issue, we should then also have its history
		// amended accordingly. For example:
		//   -- Suppose we created a link in [Issue A] to relate to [Issue B] as [Child of].
		//   -- Then it should also be reflected in Issue B's history that a link has been created in
		//      [Issue B] to related to [Issue A] as [Parent of].
		//
		// (This information of the links should already be available through the IssueLink, right?) -- Right. See notes 16.06.2009. Kai.
		Set<IssueLink> oldIssueLinks = oldPersistentIssue.getIssueLinks();
		Set<IssueLink> newIssueLinks = newDetachedIssue.getIssueLinks();

		// (i) Check for newly created IssueLinks.
		Collection<IssueLink> addedIssueLinks = new ArrayList<IssueLink>();
		if (oldIssueLinks.isEmpty()) addedIssueLinks.addAll(newIssueLinks);
		else {
			for (IssueLink issueLink : newIssueLinks)
				if (!oldIssueLinks.contains(issueLink)) addedIssueLinks.add(issueLink);
		}

		// (ii) Check for recently severed IssueLinks.
		Collection<IssueLink> removedIssueLinks = new ArrayList<IssueLink>();
		if (newIssueLinks.isEmpty()) removedIssueLinks.addAll(oldIssueLinks);
		else {
			for (IssueLink issueLink : oldIssueLinks)
				if (!newIssueLinks.contains(issueLink)) removedIssueLinks.add(issueLink);
		}


		// (iii) Collate them all, and generate the necessary IssueHistoryItem.
		Collection<IssueHistoryItem> issueLinkHistoryItems = new ArrayList<IssueHistoryItem>();
		for (IssueLink issueLink : addedIssueLinks) {
			// [Default behaviour]
			// Create a new IssueHistoryItem to indicate that a new IssueLink has been created.
			// Generate the IssueHistoryItem for the forward link.
			issueLinkHistoryItems.add( new IssueLinkHistoryItem(
					user, oldPersistentIssue,
					issueLink.getIssueLinkType(), 	// <-- The relationship between the Issue and the linkedObject is indicated here in the name field.
					IssueHistoryItemAction.ADDED,
					issueLink.getLinkedObjectClass().getSimpleName(),
					issueLink.getLinkedObjectID().toString()
			));

			// [Additional behaviour]
			// If the linkedObject indicated by this IssueLink is also an Issue, then we also need to add an appropriate
			// IssueHistoryItem for it -- based on the relationship. This relationship information should already be available.
			// --> This is done through the method 'postCreateIssueLink(...)' in IssueLinkTypeIssueToIssue.
		}

		// Similarly, when a link is removed, we should note the severance of the link of BOTH objects.
		for (IssueLink issueLink : removedIssueLinks) {
			// [Default behaviour]
			issueLinkHistoryItems.add( new IssueLinkHistoryItem(
					user, oldPersistentIssue,
					issueLink.getIssueLinkType(),
					IssueHistoryItemAction.REMOVED,
					issueLink.getLinkedObjectClass().getSimpleName(),
					issueLink.getLinkedObjectID().toString()
			));


			// [Additional behaviour]
			// If the linkedObject indicated by this IssueLink is also an Issue, then we also need to take care of this.
			// --> This is done through the method 'preDeleteIssueLink(...)' in IssueLinkTypeIssueToIssue.
		}


		return issueLinkHistoryItems;
	}

}
