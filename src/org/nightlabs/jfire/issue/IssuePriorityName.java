package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * An extended class of {@link I18nText} that represents the {@link IssuePriority}'s name. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssuePriorityNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssuePriorityName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, issuePriorityID"
 * 
 * @jdo.fetch-group name="IssuePriorityName.name" fetch-groups="default" fields="issuePriority, names"
 */ 
public class IssuePriorityName 
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
	 *		table="JFireIssueTracking_IssuePriorityName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssuePriorityName()
	{
	}

	public IssuePriorityName(IssuePriority issuePriority)
	{
		this.issuePriority = issuePriority;
		this.organisationID = issuePriority.getOrganisationID();
		issuePriorityID = issuePriority.getIssuePriorityID();
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
	
	public IssuePriority getIssuePriority() {
		return issuePriority;
	}
	
	public String getIssuePriorityID() {
		return issuePriorityID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return issuePriorityID == null ? languageID : issuePriorityID;
	}
}
