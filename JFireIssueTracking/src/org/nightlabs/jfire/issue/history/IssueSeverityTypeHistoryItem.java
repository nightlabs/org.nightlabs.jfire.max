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
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueSeverityType}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueSeverityTypeHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="oldIssueSeverity"), @Persistent(name="newIssueSeverity")}
	)
})
public class IssueSeverityTypeHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueSeverityType oldIssueSeverity;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueSeverityType newIssueSeverity;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueSeverityTypeHistoryItem() {}

	/**
	 * Creates a new instance of an IssueSeverityTypeHistoryItem.
	 */
	public IssueSeverityTypeHistoryItem(User user, Issue issue, IssueSeverityType oldIssueSeverity, IssueSeverityType newIssueSeverity) {
		super(true, user, issue);
		this.oldIssueSeverity = oldIssueSeverity;
		this.newIssueSeverity = newIssueSeverity;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return String.format("MODIFIED: Severity type from \"%s\" to \"%s\".", oldIssueSeverity.getIssueSeverityTypeText().getText(), newIssueSeverity.getIssueSeverityTypeText().getText());
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}
}
