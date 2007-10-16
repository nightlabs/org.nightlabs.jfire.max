package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairatk at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueDescriptionID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueID"
 *
 */ 
public class IssueDescription extends I18nText 
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueID;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireIssueTracking_IssueDescription_descriptions"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map descriptions = new HashMap();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueDescription()
	{
	}

	public IssueDescription(Issue issue)
	{
		this.organisationID = issue.getOrganisationID();
		this.issueID = issue.getIssueID();
		this.issue = issue;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return descriptions;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return Issue.getPrimaryKey(organisationID, issueID);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getIssueID()
	{
		return issueID;
	}

	public Issue getIssue()
	{
		return issue;
	}
}