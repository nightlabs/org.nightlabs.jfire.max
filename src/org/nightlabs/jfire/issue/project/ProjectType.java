package org.nightlabs.jfire.issue.project;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.issue.project.id.ProjectTypeID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.project.id.ProjectTypeID"
 *		detachable="true"
 *		table="JFireIssueTracking_ProjectType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, projectTypeID"
 *
 * @jdo.fetch-group name="ProjectType.name" fields="name"
 *
 **/
public class ProjectType
implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ProjectType.class);

	public static final String FETCH_GROUP_NAME = "ProjectType.name";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private String projectTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" fetch-groups="default" dependent="true" mapped-by="projectType"
	 */
	private ProjectTypeName name;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProjectTypeDescription description;

	public static final ProjectTypeID PROJECT_TYPE_ID_DEFAULT = ProjectTypeID.create(Organisation.DEV_ORGANISATION_ID, "default");
	
	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected ProjectType() { }

	public ProjectType(ProjectTypeID projectTypeID) {
		this(projectTypeID.organisationID, projectTypeID.projectTypeID);
	}
	
	public ProjectType(String organisationID, String projectTypeID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.projectTypeID = projectTypeID;

		this.name = new ProjectTypeName(this);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() 
	{
		return organisationID;
	}

	public String getProjectTypeID() 
	{
		return projectTypeID;
	}

	
	public ProjectTypeDescription getDescription() {
		return description;
	}

	public void setDescription(ProjectTypeDescription description) {
		this.description = description;
	}

	public ProjectTypeName getName()
	{
		return name;
	}
	
	/**
	 * Get the JDO object id.
	 * @return the JDO object id.
	 */
	public ProjectTypeID getObjectId()
	{
		return (ProjectTypeID)JDOHelper.getObjectId(this);
	}
	
	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object. 
	 */
	protected PersistenceManager getPersistenceManager() 
	{
		PersistenceManager projectTypePM = JDOHelper.getPersistenceManager(this);
		if (projectTypePM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return projectTypePM;
	}	

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ProjectType)) return false;
		ProjectType o = (ProjectType) obj;
		return
		Util.equals(this.organisationID, o.organisationID) && 
		Util.equals(this.projectTypeID, o.projectTypeID);
	}

	@Override
	public int hashCode()
	{
		return 
		Util.hashCode(organisationID) ^
		Util.hashCode(projectTypeID);
	}
}