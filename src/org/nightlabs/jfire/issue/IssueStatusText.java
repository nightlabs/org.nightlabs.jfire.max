package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueStatusTextID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueStatusText"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.fetch-group name="IssueStatusText.text" fields="issueStatus, texts"
 */ 
public class IssueStatusText 
extends I18nText{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String issueStatusID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueStatus issueStatus;
	
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
	 *		table="JFireIssueTracking_IssueStatusText_texts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> texts = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueStatusText()
	{
	}

	public IssueStatusText(IssueStatus issueStatus)
	{
		this.issueStatus = issueStatus;
		issueStatusID = issueStatus.getIssueStatusID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map<String, String> getI18nMap()
	{
		return texts;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return issueStatusID == null ? languageID : issueStatusID;
	}
}
