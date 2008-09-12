package org.nightlabs.jfire.issue.project;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.project.id.ProjectID"
 *		detachable="true"
 *		table="JFireIssueTracking_Project"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, projectID"
 *
 * @jdo.query
 *		name="getRootProjects"
 *		query="SELECT
 *			WHERE 
 *				this.organisationID == :organisationID &&
 *				this.parentProject == null"
 *
 * @jdo.query
 *		name="getProjectsByParentProjectID"
 *		query="SELECT
 *			WHERE 
 *				this.organisationID == :organisationID &&
 *				this.parentProject.projectID == :parentProjectID"
 *
 * @jdo.query
 *		name="getProjectsByProjectTypeID"
 *		query="SELECT
 *			WHERE 
 *				this.projectType.organisationID == :organisationID &&
 *				this.projectType.projectTypeID == :projectTypeID"
 *
 * @jdo.fetch-group name="Project.name" fields="name"
 * @jdo.fetch-group name="Project.description" fields="description"
 * @jdo.fetch-group name="Project.parentProject" fields="parentProject"
 * @jdo.fetch-group name="Project.subProjects" fields="subProjects"
 * @jdo.fetch-group name="Project.projectType" fields="projectType"
 * @jdo.fetch-group name="Issue.project" fields="name"
 * 
 **/
public class Project
implements Serializable, Comparable<Project> 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Project.class);

	public static final String FETCH_GROUP_DESCRIPTION = "Project.description";
	public static final String FETCH_GROUP_NAME = "Project.name";
	public static final String FETCH_GROUP_PARENT_PROJECT = "Project.parentProject";
	public static final String FETCH_GROUP_SUBPROJECTS = "Project.subProjects";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long projectID;

	/**
	 * @jdo.field persistence-modifier="persistent" fetch-groups="default"  dependent="true" mapped-by="project"
	 */
	private ProjectName name;
	
	/**
	 * @jdo.field 
	 * 		persistence-modifier="persistent" 
	 * 		dependent="true" 
	 * 		mapped-by="project"
	 */
	private ProjectDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Project parentProject;
	
	/**
	 * Instances of IssueLink that are representations of {@link ObjectID}s.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Project"
	 *		dependent-element="true"
	 *		mapped-by="parentProject"
	 */
	private Collection<Project> subProjects;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProjectType projectType;
	
	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected Project() { }

	public Project(String organisationID, long projectID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.projectID = projectID;

		this.name = new ProjectName(this);
		this.description = new ProjectDescription(this);
		
		subProjects = new HashSet<Project>();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() 
	{
		return organisationID;
	}

	public long getProjectID() 
	{
		return projectID;
	}

	public ProjectName getName()
	{
		return name;
	}
	
	/**
	 * Get the JDO object id.
	 * @return the JDO object id.
	 */
	public ProjectID getObjectId()
	{
		return (ProjectID)JDOHelper.getObjectId(this);
	}
	
	public void setParentProject(Project project) {
		this.parentProject = project;
	}
	
	public Project getParentProject() {
		return parentProject;
	}
	
	public ProjectType getProjectType() {
		return projectType;
	}

	public void setProjectType(ProjectType projectType) {
		this.projectType = projectType;
	}

	public Collection<Project> getSubProjects() 
	{
		return Collections.unmodifiableCollection(subProjects);
	}
	
	public void addSubProject(Project project) 
	{
		subProjects.add(project);
	}
	
	public void removeSubProject(Project project) 
	{
		if (project == null)
			throw new IllegalArgumentException("project must not be null!");
		subProjects.remove(project);
	}
	
	/**
	 * @return Returns the description.
	 */
	public ProjectDescription getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(ProjectDescription description) {
		this.description = description;
	}

	/**
	 * Internal method.
	 * @return The PersistenceManager associated with this object. 
	 */
	protected PersistenceManager getPersistenceManager() 
	{
		PersistenceManager projectPM = JDOHelper.getPersistenceManager(this);
		if (projectPM == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not persistent, can not get a PersistenceManager!");

		return projectPM;
	}	

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof Project)) return false;
		Project o = (Project) obj;
		return
		Util.equals(this.organisationID, o.organisationID) && 
		Util.equals(this.projectID, o.projectID);
	}

	@Override
	public int hashCode()
	{
		return 
		Util.hashCode(organisationID) ^
		Util.hashCode(projectID);
	}

	@Override
	public int compareTo(Project o) {
		return this.name.getText().compareTo(o.getName().getText());
	}
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private int level;
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
}