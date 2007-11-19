package org.nightlabs.jfire.issue.history;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.issue.Issue;

/**
 * @author Chairat Kongarayawetchakun - chairatk at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueHistoryTextID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueHistoryText"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueHistoryID, issueHistoryTextID"
 *
 */
public class IssueHistoryText extends I18nText{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private long issueHistoryID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueHistory issueHistory;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueHistoryTextID;

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
	 *		table="JFireIssueTracking_IssueHistoryText_texts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map texts = new HashMap();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Issue issue;

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueHistoryText()
	{
	}

	public IssueHistoryText(IssueHistory issueHistory)
	{
		this.organisationID = issueHistory.getOrganisationID();
		this.issueHistoryID = issueHistory.getIssueHistoryID();
		this.issueHistory = issueHistory;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return texts;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return Issue.getPrimaryKey(organisationID, issue.getIssueID());
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getIssueHistoryTextID()
	{
		return issueHistoryTextID;
	}

	public Issue getIssue()
	{
		return issue;
	}
}
