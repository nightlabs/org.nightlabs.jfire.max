package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * The {@link IssueComment} class represents a comment which is created in an {@link Issue}.
 * <p>
 * 
 * </p>
 *
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
 * 		field-order="organisationID, commentID"
 *
 * @jdo.fetch-group name="IssueComment.user" fetch-groups="default" fields="user"
 * @jdo.fetch-group name="IssueComment.this" fields="text, createTimestamp, user"
 *
 */
public class IssueComment
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_COMMENT = "IssueComment.this";

	public static final String FETCH_GROUP_USER = "IssueComment.user";
	
	/**
	 * This is the organisationID to which the issue comment belongs. Within one organisation,
	 * all the issue comments have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long commentID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="clob"
	 */
	private String text;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent" load-fetch-group="all"
	 */
	private User user;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	protected IssueComment()
	{
	}

	/**
	 * Constructs a new IssueComment.
	 * @param organisationID organisation id which this issue comment is stored in
	 * @param commentID a unique id for this issue comment
	 * @param issue the issue that this issue comment is made in 
	 * @param text the text of the comment
	 * @param user the user that created this comment
	 */
	public IssueComment(Issue issue, long commentID, String text, User user){
		this.organisationID = issue.getOrganisationID();
		this.commentID = commentID;
		this.issue = issue;
		this.text = text;
		this.user = user;

		createTimestamp = new Date();
	}

	/**
	 * Returns the organisation id.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the comment id.
	 * @return the commentID
	 */
	public long getCommentID() {
		return commentID;
	}

	/**
	 * Returns the text of the comment.
	 * @return the text of the comment
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the create time of the comment.
	 * @return the create time of the comment
	 */
	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	/**
	 * Returns the user who created the comment.
	 * @return the user who created the comment
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Returns the issue that this comment is created for.
	 * @return the issue that this comment is created for
	 */
	public Issue getIssue() {
		return issue;
	}

	@Override
	/*
	 * 
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssueComment)) return false;
		IssueComment o = (IssueComment) obj;
		return Util.equals(this.organisationID, o.organisationID) && Util.equals(o.commentID, this.commentID);
	}

	@Override
	/*
	 * 
	 */
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(commentID);
	}
}
