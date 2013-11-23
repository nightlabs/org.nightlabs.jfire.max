package org.nightlabs.jfire.issue.issuemarker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.history.IssueHistoryItem;
import org.nightlabs.jfire.issue.history.IssueHistoryItemAction;
import org.nightlabs.jfire.issue.history.IssueHistoryItemFactory;
import org.nightlabs.jfire.issue.history.IssuePriorityHistoryItem;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssuePriorityHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueMarkerHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Check and see what new IssueMarker has been added, and what current IssueMarkers have been removed.
		// Note: There are cases of no effects; eg. a new IssueMarker was added, and then removed before the Issue was saved (and vice-versa).
		Set<IssueMarker> oldIssueMarkers = oldPersistentIssue.getIssueMarkers();
		Set<IssueMarker> newIssueMarkers = newDetachedIssue.getIssueMarkers();


		// (i) Check for newly ADDED IssueMarkers.
		Collection<IssueMarker> addedIssueMarkers = new ArrayList<IssueMarker>();
		if (oldIssueMarkers.isEmpty())	addedIssueMarkers.addAll( newIssueMarkers );
		else {
			for (IssueMarker issueMarker : newIssueMarkers) {
				if ( !oldIssueMarkers.contains(issueMarker) ) addedIssueMarkers.add(issueMarker);
			}
		}

		// (ii) Check for IssueMarkers that have been REMOVED.
		Collection<IssueMarker> removedIssueMarkers = new ArrayList<IssueMarker>();
		if (newIssueMarkers.isEmpty())	removedIssueMarkers.addAll( oldIssueMarkers );
		else {
			for (IssueMarker issueMarker : oldIssueMarkers) {
				if ( !newIssueMarkers.contains(issueMarker) ) removedIssueMarkers.add(issueMarker);
			}
		}

		// (iii) Collate them all, and generate the necessary IssueHistoryItem.
		Collection<IssueHistoryItem> issueMarkerHistoryItems = new ArrayList<IssueHistoryItem>();
		for(IssueMarker issueMarker : addedIssueMarkers)
			issueMarkerHistoryItems.add( new IssueMarkerHistoryItem(user, oldPersistentIssue, issueMarker, IssueHistoryItemAction.ADDED) );

		for(IssueMarker issueMarker : removedIssueMarkers)
			issueMarkerHistoryItems.add( new IssueMarkerHistoryItem(user, oldPersistentIssue, issueMarker, IssueHistoryItemAction.REMOVED) );


		return issueMarkerHistoryItems;
	}

}
