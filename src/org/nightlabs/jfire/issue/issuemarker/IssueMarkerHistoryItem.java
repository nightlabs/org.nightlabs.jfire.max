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
public class IssueMarkerHistoryItem extends IssueHistoryItem
//implements DetachCallback
{
	private static final long serialVersionUID = 8377733072628534413L;

	// Ugghhh... still not sure how to get this one going. Requires a bit more thinking -- or simply ask someone!!
	// Should I persist this?
	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueMarker issueMarker;

	// And zees?
	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueMarkerHistoryItemAction historyActionIssueMarker;


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
	public IssueMarkerHistoryItem(User user, IssueMarker issueMarker, IssueMarkerHistoryItemAction historyActionIssueMarker, Issue oldPersistentIssue, Issue newDetachedIssue) {
		super(true, user, oldPersistentIssue, newDetachedIssue);
		this.issueMarker = issueMarker;
		this.historyActionIssueMarker = historyActionIssueMarker;

//		// Now generate the description (and icon image) specifically for this IssueHistoryItem.
//		setDescription( String.format("%s IssueMarker: %s", issueHistoryAction.toString(), issueMarker.getName().getText()) );
//		setIcon16x16Data( issueMarker.getIcon16x16Data() );
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	@Override
	public byte[] getIcon16x16Data() {
		return issueMarker.getIcon16x16Data();
	}

//	@Persistent(persistenceModifier=PersistenceModifier.NONE)
//	private String description;

	@Override
	public String getDescription() {
//		if (description != null)
//			return description;

		switch (historyActionIssueMarker) {
			case ADDED:
				return String.format("Issue marker %s was ADDED.", issueMarker.getName().getText());
			case REMOVED:
				return String.format("Issue marker %s was REMOVED.", issueMarker.getName().getText());
			default:
				throw new IllegalStateException("Unknown historyActionIssueMarker: " + historyActionIssueMarker);
		}
	}

//	@Override
//	public void jdoPostDetach(Object o) {
//		IssueMarkerHistoryItem detached = this;
//		IssueMarkerHistoryItem attached = (IssueMarkerHistoryItem) o;
//
//		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
//		if (pm.getFetchPlan().getGroups().contains(FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST))
//			detached.description = attached.getDescription();
//	}
//
//	@Override
//	public void jdoPreDetach() {
//		// TODO Auto-generated method stub
//
//	}
}
