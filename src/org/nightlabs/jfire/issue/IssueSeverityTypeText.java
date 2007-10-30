package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueSeverityTypeTextID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueSeverityTypeText"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.fetch-group name="IssueSeverityTypeText.this" fetch-groups="default" fields="issueSeverityType, texts"
 */ 
public class IssueSeverityTypeText 
extends I18nText{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueSeverityTypeID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueSeverityType issueSeverityType;

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
	 *		table="JFireIssueTracking_IssueSeverityTypeText_texts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> texts = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueSeverityTypeText()
	{
	}

	public IssueSeverityTypeText(IssueSeverityType issueSeverityType)
	{
		this.issueSeverityType = issueSeverityType;
		issueSeverityTypeID = issueSeverityType.getIssueSeverityTypeID();
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
		return issueSeverityTypeID == null ? languageID : issueSeverityTypeID;
	}
}