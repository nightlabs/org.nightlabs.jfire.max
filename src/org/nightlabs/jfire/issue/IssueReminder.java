package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.nightlabs.jfire.security.User;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import org.nightlabs.jfire.issue.id.IssueReminderID;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * The {@link IssueReminder} class represents a reminder.
 * <p>
 * 
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueReminderID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueReminder"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, issueReminderID"
 *
 * @jdo.fetch-group name="IssueReminder.users" fetch-groups="default" fields="users"
 * @jdo.fetch-group name="IssueReminder.this" fields="users"
 *
 */
@PersistenceCapable(
	objectIdClass=IssueReminderID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueReminder")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IssueReminder.users",
		members=@Persistent(name="users")),
	@FetchGroup(
		name="IssueReminder.this",
		members=@Persistent(name="users"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class IssueReminder 
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue reminder belongs. Within one organisation,
	 * all the issue reminders have their organisation's ID stored here, thus it's the same
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
	private long issueReminderID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createTimestamp;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="User"
	 *		table="JFireIssueTracking_IssueReminder_users"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireIssueTracking_IssueReminder_users",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<User> users;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	public IssueReminder() {
	}
}
