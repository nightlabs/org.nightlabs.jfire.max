package org.nightlabs.jfire.issue.project;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.projectType.id.ProjectTypeDescriptionID"
 *		detachable="true"
 *		table="JFireProjectTypeTracking_ProjectTypeDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, projectTypeID"
 * 
 * @jdo.fetch-group name="ProjectType.description" fields="projectType, descriptions"
 */ 
public class ProjectTypeDescription 
	extends I18nText
{
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
	 */
	private long projectTypeID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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

	public long getProjectTypeID() {
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