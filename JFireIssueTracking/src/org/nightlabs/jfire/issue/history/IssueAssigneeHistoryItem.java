package org.nightlabs.jfire.issue.history;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * an {@link Issue}'s assignee.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireIssueTracking_IssueAssigneeHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueAssigneeHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = -8339791095548654133L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String oldAssigneeTxt;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String newAssigneeTxt;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueAssigneeHistoryItem() {}

	/**
	 * Creates a new instance of an IssueAssigneeHistoryItem.
	 */
	public IssueAssigneeHistoryItem(User user, Issue issue, String oldAssigneeTxt, String newAssigneeTxt) {
		super(true, user, issue);
		this.oldAssigneeTxt = oldAssigneeTxt;
		this.newAssigneeTxt = newAssigneeTxt;
	}



	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		// Three unique cases exist here:
		//  1. ASSIGNED: Old Issue has no Assignee, and in the new Issue it has one assigned.
		//  2. UN-ASSIGNED: Old Issue has an Assignee, and in the new Issue, none exists.
		//  3. RE-ASSIGNED: Old and new Issue's Assignees are different.
		if (oldAssigneeTxt.isEmpty()) return String.format("ASSIGNED: %s.", newAssigneeTxt);
		if (newAssigneeTxt.isEmpty()) return String.format("UN-ASSIGNED: %s.", oldAssigneeTxt);
		return String.format("RE-ASSIGNED: %s to %s.", oldAssigneeTxt, newAssigneeTxt);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}

}
