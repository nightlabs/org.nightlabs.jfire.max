package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Value;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.issue.id.IssueSubjectID;

/**
 * The {@link IssueSubject} class represents a subject of an {@link Issue}.
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueSubjectID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueSubject"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, issueID"
 *
 * @jdo.fetch-group name="IssueSubject.names" fields="names"
 * @jdo.fetch-group name="Issue.subject" fields="issue, names"
 */
@PersistenceCapable(
	objectIdClass=IssueSubjectID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueSubject")
@FetchGroups({
	@FetchGroup(
		name=IssueSubject.FETCH_GROUP_THIS_ISSUE_SUBJECT_NAMES,
		members=@Persistent(name="names")),
	@FetchGroup(
		name="Issue.subject",
		members={@Persistent(name="issue"), @Persistent(name="names")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class IssueSubject
	extends I18nText
{
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_ISSUE_SUBJECT_NAMES = "IssueSubject.names";

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue subject belongs. Within one organisation,
	 * all the issue subjects have their organisation's ID stored here, thus it's the same
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
	private long issueID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Issue issue;

	/**
	 * key: String languageID<br/>
	 * value: String names
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		table="JFireIssueTracking_IssueSubject_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 * @jdo.value-column sql-type="CLOB"
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireIssueTracking_IssueSubject_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	@Value(
			columns={@Column(sqlType="CLOB")}
	)
	private Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueSubject() { }

	/**
	 *
	 * @param issue
	 */
	public IssueSubject(Issue issue)
	{
		this.issue = issue;
		this.organisationID = issue.getOrganisationID();
		this.issueID = issue.getIssueID();
	}

	/**
	 *
	 * @return
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 *
	 * @return
	 */
	public long getIssueID() {
		return issueID;
	}

	/**
	 *
	 * @return
	 */
	public Issue getIssue() {
		return issue;
	}

	@Override
	/**
	 *
	 */
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Override
	/**
	 *
	 */
	protected String getFallBackValue(String languageID)
	{
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(issueID);
	}
}