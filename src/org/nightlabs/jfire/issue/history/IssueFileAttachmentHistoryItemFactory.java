package org.nightlabs.jfire.issue.history;

import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueFileAttachment;
import org.nightlabs.jfire.security.User;

/**
 * This is the {@link IssueHistoryItemFactory} that generates {@link IssueFileAttachmentHistoryItem}s, based on information
 * between an old {@link Issue} and a newly saved {@link Issue}.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
public class IssueFileAttachmentHistoryItemFactory extends IssueHistoryItemFactory {
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItemFactory#createIssueHistoryItems(org.nightlabs.jfire.security.User, org.nightlabs.jfire.issue.Issue, org.nightlabs.jfire.issue.Issue)
	 */
	@Override
	public Collection<IssueHistoryItem> createIssueHistoryItems(User user, Issue oldPersistentIssue, Issue newDetachedIssue)
	throws JDODetachedFieldAccessException {
		// Check and see what new FileAttachments have been added, and what current IssueMarkers have been removed.
		// Note: There are cases of no effects; eg. a new FileAttachment was added, and then removed before the Issue was saved (and vice-versa).
		Collection<IssueFileAttachment> oldFAs = oldPersistentIssue.getIssueFileAttachments();
		Collection<IssueFileAttachment> newFAs = newDetachedIssue.getIssueFileAttachments();

		// (i) Check for newly ADDED IssueFileAttachments.
		Collection<IssueFileAttachment> addedFAs = new ArrayList<IssueFileAttachment>();
		if (oldFAs.isEmpty()) addedFAs.addAll( newFAs );
		else {
			for (IssueFileAttachment iFA : newFAs)
				if ( !oldFAs.contains(iFA) ) addedFAs.add( iFA );
		}

		// (ii) Check for IssueFileAttachments that have been REMOVED.
		Collection<IssueFileAttachment> removedFAs = new ArrayList<IssueFileAttachment>();
		if (newFAs.isEmpty()) removedFAs.addAll( oldFAs );
		else {
			for (IssueFileAttachment iFA : oldFAs)
				if ( !newFAs.contains(iFA) ) removedFAs.add( iFA );
		}


		// (iii) Collate them all, and generate the necessary IssueFileAttachmentHistoryItem
		Collection<IssueHistoryItem> issueFileAttachmentHistoryItems = new ArrayList<IssueHistoryItem>();
		for(IssueFileAttachment iFA : removedFAs)
			issueFileAttachmentHistoryItems.add( new IssueFileAttachmentHistoryItem(user, oldPersistentIssue, iFA.getFileName(), iFA.getFileSize(), IssueHistoryItemAction.REMOVED) );

		for(IssueFileAttachment iFA : addedFAs)
			issueFileAttachmentHistoryItems.add( new IssueFileAttachmentHistoryItem(user, oldPersistentIssue, iFA.getFileName(), iFA.getFileSize(), IssueHistoryItemAction.ADDED) );


		return issueFileAttachmentHistoryItems;
	}

}
