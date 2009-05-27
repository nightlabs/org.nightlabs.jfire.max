package org.nightlabs.jfire.issue.issuemarker;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.history.IssueHistoryItem;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueMarker}s.
 *
 * An {@link IssueMarker} is either ADDED or REMOVED in a history action.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueMarkerHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueMarkerHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	// May consider a 'general history action' for all IssueHistoyItems? Mebbe not... dunno yet. Let's see.
	public static enum HistoryActionIssueMarker { ADDED, REMOVED }

	private IssueMarker issueMarker;
	private HistoryActionIssueMarker historyActionIssueMarker;


//	// !!!!!! !!!!!! Marked for REVISION !!!! See notes 25 May 2009 !!!!!! !!!!!!
//	public IssueMarkerHistoryItem(String organisationID, User user, Issue oldIssue, Issue newIssue, long issueHistoryItemID) { // <-- FIXME Requires revision. Kai.
//		super(organisationID, user, oldIssue, newIssue, issueHistoryItemID);
//		// TODO Auto-generated constructor stub
//	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueMarkerHistoryItem() {}

	/**
	 * Creates a new instance of an IssueMarkerHistoryItem.
	 */
	public IssueMarkerHistoryItem(User user, IssueMarker issueMarker, HistoryActionIssueMarker historyActionIssueMarker, Issue oldPersistentIssue, Issue newDetachedIssue) {
		super(true, user, oldPersistentIssue, newDetachedIssue);
		this.issueMarker = issueMarker;
		this.historyActionIssueMarker = historyActionIssueMarker;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	@Override
	public byte[] getImage16x16Data() {
		return issueMarker.getIcon16x16Data();
	}

	@Override
	public String getDescription() {
		return String.format("%s IssueMarker: %s", historyActionIssueMarker.toString(), issueMarker.getName().getText());
	}
}
