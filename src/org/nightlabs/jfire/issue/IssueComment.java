package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.issue.id.IssueCommentID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

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
@PersistenceCapable(
	objectIdClass=IssueCommentID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueComment")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=IssueComment.FETCH_GROUP_USER,
		members=@Persistent(name="user")),
	@FetchGroup(
		name=IssueComment.FETCH_GROUP_TEXT,
		members=@Persistent(name="text")),
	@FetchGroup(
		name=IssueComment.FETCH_GROUP_TIMESTAMP,
		members=@Persistent(name="createTimestamp")),			
	@FetchGroup(
		name=IssueComment.FETCH_GROUP_THIS_COMMENT,
		members={@Persistent(name="text"), @Persistent(name="createTimestamp"), @Persistent(name="user")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
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
	public static final String FETCH_GROUP_TEXT = "IssueComment.text";
	public static final String FETCH_GROUP_TIMESTAMP = "IssueComment.createTimestamp";

	
	
	/**
	 * This is the organisationID to which the issue comment belongs. Within one organisation,
	 * all the issue comments have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long commentID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="clob"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="clob")
	private String text;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent" load-fetch-group="all"
	 */
	@Persistent(
		loadFetchGroup="all",
		persistenceModifier=PersistenceModifier.PERSISTENT)
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
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssueComment</code>.
	 * @param commentID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssueComment.class</code> to create an id.
	 * @param issue the issue that this issue comment is made in 
	 * @param text the text of the comment
	 * @param user the user that created this comment
	 */
	public IssueComment(String organisationID, long commentID, Issue issue, String text, User user){
		this.organisationID = organisationID;
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
