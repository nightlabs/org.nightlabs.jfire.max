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
import org.nightlabs.jfire.issue.id.IssueResolutionNameID;

/**
 * An extended class of {@link I18nText} that represents the {@link IssueResolution}'s name.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
	objectIdClass=IssueResolutionNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_IssueResolutionName")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IssueResolutionName.name",
		members={@Persistent(name="issueResolution"), @Persistent(name="names")}
	),
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="issueResolution"), @Persistent(name="names")}
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class IssueResolutionName
extends I18nText{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the issue resolution's name belongs. Within one organisation,
	 * all the issue resolution's names have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String issueResolutionID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IssueResolution issueResolution;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireIssueTracking_IssueResolutionName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected IssueResolutionName()
	{
	}

	/**
	 *
	 * @param issueResolution
	 */
	public IssueResolutionName(IssueResolution issueResolution)
	{
		this.issueResolution = issueResolution;
		organisationID = issueResolution.getOrganisationID();
		issueResolutionID = issueResolution.getIssueResolutionID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
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
	public IssueResolution getIssueResolution() {
		return issueResolution;
	}

	/**
	 *
	 * @return
	 */
	public String getIssueResolutionID() {
		return issueResolutionID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return issueResolutionID == null ? languageID : issueResolutionID;
	}
}
