package org.nightlabs.jfire.issue.project;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import org.nightlabs.jfire.issue.project.id.ProjectDescriptionID;
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
 * An extended class of {@link I18nText} that represents the description created in an {@link Project}. 
 * <p>
 * </p>
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.project.id.ProjectDescriptionID"
 *		detachable="true"
 *		table="JFireIssueTracking_ProjectDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, projectID"
 * 
 * @jdo.fetch-group name="Project.description" fields="project, descriptions"
 */ @PersistenceCapable(
	objectIdClass=ProjectDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTracking_ProjectDescription")
@FetchGroups(
	@FetchGroup(
		name="Project.description",
		members={@Persistent(name="project"), @Persistent(name="descriptions")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class ProjectDescription 
	extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the project description belongs. Within one organisation,
	 * all the project descriptions have their organisation's ID stored here, thus it's the same
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
	 */
	@PrimaryKey
	private long projectID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Project project;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		table="JFireProjectTracking_ProjectDescription_descriptions"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 * @jdo.value-column sql-type="CLOB"
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireProjectTracking_ProjectDescription_descriptions",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProjectDescription()
	{
	}

	public ProjectDescription(Project project)
	{
		this.project = project;
		this.organisationID = project.getOrganisationID();
		this.projectID = project.getProjectID();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	public long getProjectID() {
		return projectID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return descriptions;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return project == null ? languageID : "";
	}
}