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
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssuePriority}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssuePriorityHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="oldIssuePriority"), @Persistent(name="newIssuePriority")}
	)
})
public class IssuePriorityHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssuePriority oldIssuePriority;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssuePriority newIssuePriority;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssuePriorityHistoryItem() {}

	/**
	 * Creates a new instance of an IssuePriorityHistoryItem.
	 */
	public IssuePriorityHistoryItem(User user, Issue issue, IssuePriority oldIssuePriority, IssuePriority newIssuePriority) {
		super(true, user, issue);
		this.oldIssuePriority = oldIssuePriority;
		this.newIssuePriority = newIssuePriority;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return String.format("MODIFIED: Priority from \"%s\" to \"%s\".", oldIssuePriority.getIssuePriorityText().getText(), newIssuePriority.getIssuePriorityText().getText());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}
}
