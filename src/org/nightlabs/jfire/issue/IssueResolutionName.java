package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueResolutionNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueResolutionName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.fetch-group name="IssueResolutionName.name" fetch-groups="default" fields="issueResolution, names"
 */ 
public class IssueResolutionName 
extends I18nText{
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
	 * @jdo.column length="100"
	 */
	private String issueResolutionID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueResolution issueResolution;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		value-type="java.lang.String"
	 *		table="JFireIssueTracking_IssueResolutionName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueResolutionName()
	{
	}

	public IssueResolutionName(IssueResolution issueResolution)
	{
		this.issueResolution = issueResolution;
		organisationID = issueResolution.getOrganisationID();
		issueResolutionID = issueResolution.getIssueResolutionID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	public String getOrganisationID() {
		return organisationID;
	}
	
	public IssueResolution getIssueResolution() {
		return issueResolution;
	}
	
	public String getIssueResolutionID() {
		return issueResolutionID;
	}
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return issueResolutionID == null ? languageID : issueResolutionID;
	}
}
