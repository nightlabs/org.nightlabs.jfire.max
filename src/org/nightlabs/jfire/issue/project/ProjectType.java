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
	private long projectTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="project"
	 */
	private ProjectTypeName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProjectType parentProjectType;
	
	/**
	 * Instances of IssueLink that are representations of {@link ObjectID}s.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="ProjectType"
	 *		dependent-element="true"
	 *		mapped-by="parentProjectType"
	 */
	private Collection<ProjectType> subProjectTypes;
	
	/**
	 * @deprecated Constructor exists only for JDO! 
	 */
	@Deprecated
	protected ProjectType() { }

	public ProjectType(String organisationID, long projectTypeID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.projectTypeID = projectTypeID;

		this.name = new ProjectTypeName(this);
		
		subProjectTypes = new HashSet<ProjectType>();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() 
	{
		return organisationID;
	}

	public long getProjectTypeID() 
	{
		return projectTypeID;
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
	
	public void setParentProjectType(ProjectType project) {
		this.parentProjectType = project;
	}
	
	public ProjectType getParentProjectType() {
		return parentProjectType;
	}
	
	public Collection<ProjectType> getSubProjectTypes() 
	{
		return Collections.unmodifiableCollection(subProjectTypes);
	}
	
	public void addSubProjectType(ProjectType project) 
	{
		subProjectTypes.add(project);
	}
	
	public void removeSubProjectType(ProjectType project) 
	{
		if (project == null)
			throw new IllegalArgumentException("project must not be null!");
		subProjectTypes.remove(project);
	}
	
	public static Collection<ProjectType> getRootProjectTypes(PersistenceManager pm, String organisationID)
	{
		Query q = pm.newNamedQuery(ProjectType.class, "getRootProjectTypes");
		Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("organisationID", organisationID);
		return (Collection<ProjectType>) q.executeWithMap(params);
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