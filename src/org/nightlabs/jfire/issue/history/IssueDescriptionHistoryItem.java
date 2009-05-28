package org.nightlabs.jfire.issue.history;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueDescription;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueDescription}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireIssueTracking_IssueDescriptionHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueDescriptionHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = -8339791095548654133L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String langID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String oldDescriptionTxt;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String newDescriptionTxt;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueDescriptionHistoryItem() {}

	/**
	 * Creates a new instance of an IssueDescriptionHistoryItem.
	 */
	public IssueDescriptionHistoryItem(User user, Issue issue, String langID, String oldDescriptionTxt, String newDescriptionTxt) {
		super(true, user, issue);
		this.langID = langID;
		this.oldDescriptionTxt = oldDescriptionTxt;
		this.newDescriptionTxt = newDescriptionTxt;
	}



	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		if (oldDescriptionTxt.isEmpty()) return String.format("ADDED: Issue description (%s) \"%s\".", langID, newDescriptionTxt);
		if (newDescriptionTxt.isEmpty()) return String.format("REMOVED: Issue description (%s) \"%s\".", langID, oldDescriptionTxt);
		return String.format("MODIFIED: Issue description (%s) from \"%s\" to \"%s\".", langID, oldDescriptionTxt, newDescriptionTxt);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}

}
