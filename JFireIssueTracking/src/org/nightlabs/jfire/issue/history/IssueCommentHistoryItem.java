package org.nightlabs.jfire.issue.history;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueComment;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueComment}s.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireIssueTracking_IssueCommentHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueCommentHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = -8339791095548654133L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String commentTxt;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueCommentHistoryItem() {}

	/**
	 * Creates a new instance of an IssueCommentHistoryItem.
	 */
	public IssueCommentHistoryItem(User user, Issue issue, IssueComment comment, boolean isNew) {
		super(true, user, issue);
		boolean needTrim = comment.getText().split("\n").length > 1;
		String commentString = needTrim ? comment.getText().substring(0, comment.getText().indexOf("\n")) + " ... ": comment.getText();
		if (isNew) 
			this.commentTxt = String.format("ADDED: Issue comment \"%s\".", comment.getText(), commentString);
		else {
			this.commentTxt = String.format("UPDATED: Issue comment(#%s) \"%s\".", comment.getCommentID(), commentString);
		}
	}



	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return commentTxt;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}

}
