package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 *  An extended class of {@link I18nText} that represents the {@link IssueSeverityType}'s name. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueSeverityTypeNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueSeverityTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueSeverityTypeID"
 *
 * @jdo.fetch-group name="IssueSeverityType.name" fetch-groups="default" fields="issueSeverityType, names"
 */ 
public class IssueSeverityTypeName 
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
	 *		table="JFireIssueTracking_IssueSeverityTypeName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueSeverityTypeName()
	{
	}

	public IssueSeverityTypeName(IssueSeverityType issueSeverityType)
	{
		this.issueSeverityType = issueSeverityType;
		organisationID = issueSeverityType.getOrganisationID();
		issueSeverityTypeID = issueSeverityType.getIssueSeverityTypeID();
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
	
	public IssueSeverityType getIssueSeverityType() {
		return issueSeverityType;
	}
	
	public String getIssueSeverityTypeID() {
		return issueSeverityTypeID;
	}
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return issueSeverityTypeID == null ? languageID : issueSeverityTypeID;
	}
}