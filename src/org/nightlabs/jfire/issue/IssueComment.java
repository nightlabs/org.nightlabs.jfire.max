package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.listener.AttachCallback;

import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueCommentID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueComment"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, issueID, commentID"
 * 
 * @jdo.fetch-group name="IssueComment.user" fetch-groups="default" fields="user"
 * @jdo.fetch-group name="IssueComment.this" fields="issue, text, createTimestamp, user"
 * 
 */ 
public class IssueComment
implements Serializable, AttachCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_THIS = "IssueComment.this";
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String commentID;
	
	/**
	 * @jdo.column length="100"
	 */
	private String text;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;
	
	/**
	 * @jdo.field persistence-modifier="persistent" load-fetch-group="all"
	 */
	private User user;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssueComment()
	{
	}
	
	public IssueComment(String organisationID, long issueID, String commentID, String text, User user){
		if (commentID == null)
			throw new IllegalArgumentException("commentID must not be null!");

		this.organisationID = organisationID;
		this.issueID = issueID;
		this.commentID = commentID;
		this.text = text;
		this.user = user;
		
		createTimestamp = new Date();
	}
	
	
	public String getOrganisationID() {
		return organisationID;
	}

	public long getIssueID() {
		return issueID;
	}

	public String getCommentID() {
		return commentID;
	}
	
	public String getText() {
		return text;
	}

	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	public User getUser() {
		return user;
	}
	
	@Override
	public void jdoPostAttach(Object arg0) {
		
	}

	@Override
	public void jdoPreAttach() {
		
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueComment)) return false;
		IssueComment o = (IssueComment) obj;
		return Util.equals(o.commentID, this.commentID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(commentID);
	}
}
