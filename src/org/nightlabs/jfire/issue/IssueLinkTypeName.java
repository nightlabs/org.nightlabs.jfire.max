package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * An extended class of {@link I18nText} that represents the {@link IssueLinkType}'s name. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueLinkTypeNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueLinkTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.fetch-group name="IssueLinkType.name" fetch-groups="default" fields="issueLinkType, names"
 */ 
public class IssueLinkTypeName 
extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue link type's name belongs. Within one organisation,
	 * all the issue link type's names have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issueLinkTypeID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private IssueLinkType issueLinkType;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		value-type="java.lang.String"
	 *		table="JFireIssueTracking_IssueLinkTypeName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueLinkTypeName() { }

	public IssueLinkTypeName(IssueLinkType issueLinkType)
	{
		this.issueLinkType = issueLinkType;
		this.organisationID = issueLinkType.getOrganisationID();
		issueLinkTypeID = issueLinkType.getIssueLinkTypeID();
	}

	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public IssueLinkType getIssueLinkType() {
		return issueLinkType;
	}
	
	public String getIssueLinkTypeID() {
		return issueLinkTypeID;
	}

	@Override
	protected String getFallBackValue(String languageID)
	{
		return issueLinkTypeID;
	}
}
