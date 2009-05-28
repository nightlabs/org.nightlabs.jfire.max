package org.nightlabs.jfire.issue.history;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueResolution;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueResolution}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueResolutionHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="oldIssueResolution"), @Persistent(name="newIssueResolution")}
	)
})
public class IssueResolutionHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueResolution oldIssueResolution;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueResolution newIssueResolution;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueResolutionHistoryItem() {}

	/**
	 * Creates a new instance of an IssuePriorityHistoryItem.
	 */
	public IssueResolutionHistoryItem(User user, Issue issue, IssueResolution oldIssueResolution, IssueResolution newIssueResolution) {
		super(true, user, issue);
		this.oldIssueResolution = oldIssueResolution;
		this.newIssueResolution = newIssueResolution;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return String.format("MODIFIED: Resolution from \"%s\" to \"%s\".", oldIssueResolution.getName().getText(), newIssueResolution.getName().getText());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}
}
