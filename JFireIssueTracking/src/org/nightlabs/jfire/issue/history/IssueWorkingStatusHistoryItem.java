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
 * status of an {@link Issue}, with regards to its 'START' and 'STOP' working time (? working time ?).
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueWorkingStatusHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueWorkingStatusHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private boolean isStarted;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueWorkingStatusHistoryItem() {}

	/**
	 * Creates a new instance of an IssueWorkingStatusHistoryItem.
	 */
	public IssueWorkingStatusHistoryItem(User user, Issue issue, boolean isStarted) {
		super(true, user, issue);
		this.isStarted = isStarted;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return String.format("MODIFIED: Work time status to \"%s\".", (isStarted ? "STARTED" : "STOPPED"));
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}
}
