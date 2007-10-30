package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.issue.id.IssuePriorityID;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssuePriorityTextID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssuePriorityText"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.fetch-group name="IssuePriorityText.text" fetch-groups="default" fields="issue, texts"
 */ 
public class IssuePriorityText 
extends I18nText{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issuePriorityID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssuePriority issuePriority;

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
	 *		table="JFireIssueTracking_IssuePriorityText_texts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> texts = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssuePriorityText()
	{
	}

	public IssuePriorityText(IssuePriority issuePriority)
	{
		this.issuePriority = issuePriority;
		issuePriorityID = issuePriority.getIssuePriorityID();
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
		return issuePriorityID == null ? languageID : issuePriorityID;
	}
}
