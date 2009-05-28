package org.nightlabs.jfire.issue.issuemarker;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
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
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="issueMarker")}
	)
})
public class IssueMarkerHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueMarker issueMarker;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueMarkerHistoryItemAction historyActionIssueMarker;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueMarkerHistoryItem() {}

	/**
	 * Creates a new instance of an IssuePriorityHistoryItem.
	 */
	public IssueMarkerHistoryItem(User user, IssueMarker issueMarker, IssueMarkerHistoryItemAction historyActionIssueMarker, Issue issue) {
		super(true, user, issue);
		this.issueMarker = issueMarker;
		this.historyActionIssueMarker = historyActionIssueMarker;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		// TODO Consider incorporating a '+' (or '-') onto the display icon.
		return issueMarker.getIcon16x16Data();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		switch (historyActionIssueMarker) {
			case ADDED:
				return String.format("Issue marker %s was ADDED.", issueMarker.getName().getText());
			case REMOVED:
				return String.format("Issue marker %s was REMOVED.", issueMarker.getName().getText());
			default:
				throw new IllegalStateException("Unknown historyActionIssueMarker: " + historyActionIssueMarker);
		}
	}

}
