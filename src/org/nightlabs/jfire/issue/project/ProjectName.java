package org.nightlabs.jfire.issue.project;

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
import org.nightlabs.jfire.issue.project.id.ProjectNameID;

/**
 *  An extended class of {@link I18nText} that represents the {@link Project}'s name.
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 */
@PersistenceCapable(
	objectIdClass=ProjectNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_ProjectName")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="Project.name",
		members={@Persistent(name="project"), @Persistent(name="names")}
	),
	@FetchGroup(
		name=FetchGroupsIssueHistoryItem.FETCH_GROUP_LIST,
		members={@Persistent(name="project"), @Persistent(name="names")}
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProjectName
extends I18nText{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the project's name belongs. Within one organisation,
	 * all the project's names have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long projectID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Project project;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 */
	@Persistent(nullValue=NullValue.EXCEPTION, table="JFireIssueTracking_ProjectName_names", defaultFetchGroup="true", persistenceModifier=PersistenceModifier.PERSISTENT)
	@Join
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProjectName()
	{
	}

	public ProjectName(Project project)
	{
		this.project = project;
		this.organisationID = project.getOrganisationID();
		projectID = project.getProjectID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public Project getProject() {
		return project;
	}

	public long getProjectID() {
		return projectID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return projectID == 0 ? languageID : Long.toString(projectID);
	}
}