package org.nightlabs.jfire.issue.project;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.issue.project.id.ProjectPhaseID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * The {@link ProjectPhase} class represents a phase in a {@link Project}. One {@link Project} can have many phases. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.project.id.ProjectPhaseID"
 *		detachable="true"
 *		table="JFireIssueTracking_ProjectPhase"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, projectPhaseID"
 *
 * @jdo.fetch-group name="ProjectPhase.description" fields="description"
 * @jdo.fetch-group name="ProjectPhase.name" fields="name"
 *
 **/
public class ProjectPhase
implements Serializable 
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ProjectPhase.class);

	public static final String FETCH_GROUP_NAME = "ProjectPhase.name";
	public static final String FETCH_GROUP_DESCRIPTION = "ProjectPhase.description";

	/**
	 * This is the organisationID to which the project phase belongs. Within one organisation,
	 * all the project phases have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private String projectPhaseID;

	/**
	 * @jdo.field persistence-modifier="persistent" fetch-groups="default" dependent="true" mapped-by="projectPhase"
	 */
	private ProjectPhaseName name;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProjectPhaseDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean active = true;
	
	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected ProjectPhase() { }

	public ProjectPhase(ProjectPhaseID projectPhaseID) {
		this(projectPhaseID.organisationID, projectPhaseID.projectPhaseID);
	}
	
	public ProjectPhase(String organisationID, String projectPhaseID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.projectPhaseID = projectPhaseID;

		this.name = new ProjectPhaseName(this);
		this.description = new ProjectPhaseDescription(this);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() 
	{
		return organisationID;
	}

	public String getProjectPhaseID() 
	{
		return projectPhaseID;
	}

	
	public ProjectPhaseDescription getDescription() {
		return description;
	}

	public void setDescription(ProjectPhaseDescription description) {
		this.description = description;
	}

	public ProjectPhaseName getName()
	{
		return name;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Get the JDO object id.
	 * @return the JDO object id.
	 */
	public ProjectPhaseID getObjectId()
	{
		return (ProjectPhaseID)JDOHelper.getObjectId(this);
	}
	
	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object. 
	 */
	protected PersistenceManager getPersistenceManager() 
	{
		PersistenceManager projectPhasePM = JDOHelper.getPersistenceManager(this);
		if (projectPhasePM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return projectPhasePM;
	}	

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ProjectPhase)) return false;
		ProjectPhase o = (ProjectPhase) obj;
		return
		Util.equals(this.organisationID, o.organisationID) && 
		Util.equals(this.projectPhaseID, o.projectPhaseID);
	}

	@Override
	public int hashCode()
	{
		return 
		Util.hashCode(organisationID) ^
		Util.hashCode(projectPhaseID);
	}
}