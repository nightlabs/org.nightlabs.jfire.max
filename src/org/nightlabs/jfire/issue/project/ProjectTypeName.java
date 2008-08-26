package org.nightlabs.jfire.issue.project;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.projectType.id.ProjectTypeNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_ProjectTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, projectTypeID"
 * 
 * @jdo.fetch-group name="ProjectTypeName.name" fetch-groups="default" fields="projectType, names"
 */ 
public class ProjectTypeName 
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
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		value-type="java.lang.String"
	 *		table="JFireIssueTracking_ProjectTypeName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProjectTypeName()
	{
	}

	public ProjectTypeName(ProjectType projectType)
	{
		this.projectType = projectType;
		this.organisationID = projectType.getOrganisationID();
		projectTypeID = projectType.getProjectTypeID();
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
	
	public ProjectType getProjectType() {
		return projectType;
	}
	
	public long getProjectID() {
		return projectTypeID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return projectTypeID == 0 ? languageID : Long.toString(projectTypeID);
	}
}