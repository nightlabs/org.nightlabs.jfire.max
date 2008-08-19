package org.nightlabs.jfire.issue.project;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.issue.projectItem.id.ProjectItemID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.projectItem.id.ProjectItemID"
 *		detachable="true"
 *		table="JFireIssueTracking_ProjectItem"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, projectItemID"
 *
 * @jdo.fetch-group name="ProjectItem.name" fields="name"
 * @jdo.fetch-group name="ProjectItem.project" fields="project"
 *
 **/
public class ProjectItem
implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ProjectItem.class);

	public static final String FETCH_GROUP_NAME = "ProjectItem.name";
	public static final String FETCH_GROUP_PROJECT = "ProjectItem.project";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long projectItemID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="projectItem"
	 */
	private ProjectItemName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Project project;
	
	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected ProjectItem() { }

	public ProjectItem(String organisationID, long projectItemID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.projectItemID = projectItemID;

		this.name = new ProjectItemName(this);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() 
	{
		return organisationID;
	}

	public long getProjectItemID() 
	{
		return projectItemID;
	}

	public ProjectItemName getName()
	{
		return name;
	}
	
	/**
	 * Get the JDO object id.
	 * @return the JDO object id.
	 */
	public ProjectItemID getObjectId()
	{
		return (ProjectItemID)JDOHelper.getObjectId(this);
	}
	
	public Project getProject() {
		return project;
	}
	
	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object. 
	 */
	protected PersistenceManager getPersistenceManager() 
	{
		PersistenceManager projectItemPM = JDOHelper.getPersistenceManager(this);
		if (projectItemPM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return projectItemPM;
	}	

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ProjectItem)) return false;
		ProjectItem o = (ProjectItem) obj;
		return
		Util.equals(this.organisationID, o.organisationID) && 
		Util.equals(this.projectItemID, o.projectItemID);
	}

	@Override
	public int hashCode()
	{
		return 
		Util.hashCode(organisationID) ^
		Util.hashCode(projectItemID);
	}
}