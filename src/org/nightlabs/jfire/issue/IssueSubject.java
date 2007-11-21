package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
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
 * @jdo.fetch-group name="Issue.subject" fetch-groups="default" fields="issue, names"
 * @jdo.fetch-group name="IssueSubject.this" fetch-groups="default" fields="issue, names"
 * @jdo.fetch-group name="IssueSubject.name" fetch-groups="default" fields="names"
 */ 
public class IssueSubject 
extends I18nText{
	
	public static final String FETCH_GROUP_THIS = "IssueSubject.this";
	
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	/**
	 * key: String languageID<br/>
	 * value: String names
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireIssueTracking_IssueSubject_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueSubject()
	{
	}

	public IssueSubject(Issue issue)
	{
		this.organisationID = issue.getOrganisationID();
		this.issue = issue;
		issueID = issue.getIssueID();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	
	/**
	 * @param organisationID The organisationID to set.
	 */
	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return issue == null ? languageID : "";
	}
}