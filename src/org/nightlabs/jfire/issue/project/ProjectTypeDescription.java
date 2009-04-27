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
import org.nightlabs.jfire.issue.project.id.ProjectTypeDescriptionID;

/**
 *  An extended class of {@link I18nText} that represents the description created in an {@link ProjectType}.
 * <p>
 * </p>
 *
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.project.id.ProjectTypeDescriptionID"
 *		detachable="true"
 *		table="JFireProjectTypeTracking_ProjectTypeDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, projectTypeID"
 *
 * @jdo.fetch-group name="ProjectType.description" fields="projectType, descriptions"
 */ @PersistenceCapable(
	objectIdClass=ProjectTypeDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireProjectTypeTracking_ProjectTypeDescription")
@FetchGroups(
	@FetchGroup(
		name="ProjectType.description",
		members={@Persistent(name="projectType"), @Persistent(name="descriptions")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class ProjectTypeDescription
	extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the project type description belongs. Within one organisation,
	 * all the project type descriptions have their organisation's ID stored here, thus it's the same
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
	private String projectTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProjectType projectType;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		table="JFireProjectTypeTracking_ProjectTypeDescription_descriptions"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 * @jdo.value-column sql-type="CLOB"
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireProjectTypeTracking_ProjectTypeDescription_descriptions",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProjectTypeDescription() {}

	public ProjectTypeDescription(ProjectType projectType)
	{
		this.projectType = projectType;
		this.organisationID = projectType.getOrganisationID();
		this.projectTypeID = projectType.getProjectTypeID();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	public String getProjectTypeID() {
		return projectTypeID;
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
		return projectType == null ? languageID : "";
	}
}