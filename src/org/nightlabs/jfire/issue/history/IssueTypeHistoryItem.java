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
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueType}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueTypeHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="oldIssueType"), @Persistent(name="newIssueType")}
	)
})
public class IssueTypeHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueType oldIssueType;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueType newIssueType;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueTypeHistoryItem() {}

	/**
	 * Creates a new instance of an IssueTypeHistoryItem.
	 */
	public IssueTypeHistoryItem(User user, Issue issue, IssueType oldIssueType, IssueType newIssueType) {
		super(true, user, issue);
		this.oldIssueType = oldIssueType;
		this.newIssueType = newIssueType;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return String.format("MODIFIED: Issue type from \"%s\" to \"%s\".", oldIssueType.getName().getText(), newIssueType.getName().getText());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}
}
