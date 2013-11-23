package org.nightlabs.jfire.issue.history;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueFileAttachment;
import org.nightlabs.jfire.security.User;

/**
 * This is an {@link IssueHistoryItem} specifically tailored to handle the historical tracking of
 * {@link IssueFileAttachment}s. An {@link IssueFileAttachment} is either ADDED or REMOVED in a history action.
 *
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueFileAttachmentHistoryItem")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueFileAttachmentHistoryItem extends IssueHistoryItem {
	private static final long serialVersionUID = 8377733072628534413L;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private IssueHistoryItemAction issueHistoryItemAction;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String attachmentFileName;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private long fileSize;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueFileAttachmentHistoryItem() {}

	/**
	 * Creates a new instance of an IssueFileAttachmentHistoryItem.
	 * @param attachmentFileName the file name of the {@link IssueFileAttachment} that was either ADDED or REMOVED.
	 * @param fileSize the size of the file in bytes.
	 * @param issueHistoryItemAction the corresponding {@link IssueHistoryItemAction} for the issueFileAttachment.
	 */
	public IssueFileAttachmentHistoryItem(User user, Issue issue, String attachmentFileName, long fileSize, IssueHistoryItemAction issueHistoryItemAction) {
		super(true, user, issue);
		this.attachmentFileName = attachmentFileName;
		this.fileSize = fileSize;
		this.issueHistoryItemAction = issueHistoryItemAction;
	}


	// ---[ Abstract methods defined ]----------------------------------------------------------------------|
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getDescription()
	 */
	@Override
	public String getDescription() {
		switch (issueHistoryItemAction) {
			case ADDED:	  return String.format("ADDED file attachment: \"%s\" (%s).", attachmentFileName, fileSize + " bytes");
			case REMOVED: return String.format("REMOVED file attachment: \"%s\" (%s).", attachmentFileName, fileSize + " bytes");
			default: throw new IllegalStateException("Unknown issueHistoryItemAction: " + issueHistoryItemAction);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.history.IssueHistoryItem#getIcon16x16Data()
	 */
	@Override
	public byte[] getIcon16x16Data() {
		return null;
	}

}
