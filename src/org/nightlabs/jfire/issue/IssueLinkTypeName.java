package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import org.nightlabs.jfire.issue.id.IssueLinkTypeNameID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

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
@PersistenceCapable(
	objectIdClass=IssueLinkTypeNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueLinkTypeName")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IssueLinkType.name",
		members={@Persistent(name="issueLinkType"), @Persistent(name="names")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

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
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String issueLinkTypeID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireIssueTracking_IssueLinkTypeName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueLinkTypeName() { }

	/**
	 * 
	 * @param issueLinkType
	 */
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
	
	/**
	 * 
	 * @return
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * 
	 * @return
	 */
	public IssueLinkType getIssueLinkType() {
		return issueLinkType;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getIssueLinkTypeID() {
		return issueLinkTypeID;
	}

	@Override
	/**
	 * 
	 */
	protected String getFallBackValue(String languageID)
	{
		return issueLinkTypeID;
	}
}
