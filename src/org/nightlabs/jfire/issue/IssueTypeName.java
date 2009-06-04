/**
 *
 */
package org.nightlabs.jfire.issue;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.issue.history.FetchGroupsIssueHistoryItem;
import org.nightlabs.jfire.issue.id.IssueTypeNameID;

/**
 * An extended class of {@link I18nText} that represents the {@link IssueType}'s name.
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.id.IssueTypeNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_IssueTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, issueTypeID"
 *
 * @jdo.fetch-group name="IssueType.name" fetch-groups="default" fields="issueType, names"
 */
@PersistenceCapable(
	objectIdClass=IssueTypeNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueTypeName")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IssueType.name",
		members={@Persistent(name="issueType"), @Persistent(name="names")}
	),
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="issueType"), @Persistent(name="names")}
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class IssueTypeName
extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue type's name belongs. Within one organisation,
	 * all the issue type's names have their organisation's ID stored here, thus it's the same
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
	private String issueTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueType issueType;

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
	 *		table="JFireIssueTracking_IssueTypeName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireIssueTracking_IssueTypeName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueTypeName() {}

	/**
	 *
	 * @param issueType
	 */
	public IssueTypeName(IssueType issueType) {
		this.issueType = issueType;
		organisationID = issueType.getOrganisationID();
		issueTypeID = issueType.getIssueTypeID();
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
	public String getIssueTypeID() {
		return issueTypeID;
	}

	/**
	 *
	 * @return
	 */
	public IssueType getIssueType() {
		return issueType;
	}

	@Override
	/**
	 *
	 */
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Override
	/**
	 *
	 */
	protected String getFallBackValue(String languageID)
	{
		return organisationID + '/' + issueTypeID;
	}
}