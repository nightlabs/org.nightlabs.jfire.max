package org.nightlabs.jfire.issue;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.nightlabs.jfire.security.User;

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
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long issueReminderID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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
	private Set<User> users;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	@Deprecated
	public IssueReminder() {
	}
}
