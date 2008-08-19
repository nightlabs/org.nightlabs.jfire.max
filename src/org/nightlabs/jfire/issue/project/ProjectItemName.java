package org.nightlabs.jfire.issue.project;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.project.id.ProjectItemNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_ProjectItemName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, projectItemID"
 * 
 * @jdo.fetch-group name="ProjectItemName.name" fetch-groups="default" fields="projectItem, names"
 */ 
public class ProjectItemName 
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
	private long projectItemID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProjectItem projectItem;

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
	 *		table="JFireIssueTracking_IssueProjectItemName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProjectItemName()
	{
	}

	public ProjectItemName(ProjectItem projectItem)
	{
		this.projectItem = projectItem;
		this.organisationID = projectItem.getOrganisationID();
		projectItemID = projectItem.getProjectItemID();
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
	
	public ProjectItem getProjectItem() {
		return projectItem;
	}
	
	public long getProjectItemID() {
		return projectItemID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return projectItemID == 0 ? languageID : Long.toString(projectItemID);
	}
}