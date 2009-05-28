package org.nightlabs.jfire.issue.history;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueSubject;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueSubject}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireIssueTracking_IssueSubjectHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueSubjectHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = -8339791095548654133L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String langID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String oldSubjectTxt;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String newSubjectTxt;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueSubjectHistoryItem() {}

	/**
	 * Creates a new instance of an IssueSubjectHistoryItem.
	 */
	public IssueSubjectHistoryItem(User user, Issue issue, String langID, String oldSubjectTxt, String newSubjectTxt) {
		super(true, user, issue);
		this.langID = langID;
		this.oldSubjectTxt = oldSubjectTxt;
		this.newSubjectTxt = newSubjectTxt;
	}



	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		if (oldSubjectTxt.isEmpty()) return String.format("ADDED: Issue subject (%s) \"%s\".", langID, newSubjectTxt);
		if (newSubjectTxt.isEmpty()) return String.format("REMOVED: Issue subject (%s) \"%s\".", langID, oldSubjectTxt);
		return String.format("MODIFIED: Issue description (%s) from \"%s\" to \"%s\".", langID, oldSubjectTxt, newSubjectTxt);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}

}
