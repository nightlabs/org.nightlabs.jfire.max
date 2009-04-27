package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.issue.id.IssueDescriptionID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * An extended class of {@link I18nText} that represents the description created in an {@link Issue}. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueDescriptionID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, issueID"
 * 
 * @jdo.fetch-group name="Issue.description" fields="issue, descriptions"
 */
@PersistenceCapable(
	objectIdClass=IssueDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueDescription")
@FetchGroups(
	@FetchGroup(
		name="Issue.description",
		members={@Persistent(name="issue"), @Persistent(name="descriptions")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class IssueDescription 
	extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue description belongs. Within one organisation,
	 * all the issue descriptions have their organisation's ID stored here, thus it's the same
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
	 * value: String description
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		table="JFireIssueTracking_IssueDescription_descriptions"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 * @jdo.value-column sql-type="CLOB"
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireIssueTracking_IssueDescription_descriptions",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueDescription()
	{
	}

	/**
	 * Constructs a new IssueDescription.
	 * @param issue the issue that this issue description is made in 
	 */
	public IssueDescription(String organisationID, long issueID, Issue issue)
	{
		this.issue = issue;
		this.organisationID = organisationID;
		this.issueID = issueID;
	}

	/**
	 * Returns the organisation id.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the issue id.
	 * @return the issue id
	 */
	public long getIssueID() {
		return issueID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return descriptions;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return issue == null ? languageID : "";
	}
}