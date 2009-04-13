package org.nightlabs.jfire.issue.project;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * The {@link Project} class represents a project.
 * <p>
 * The {@link Project} class can contain many sub-projects.
 * </p>
 *
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
 * @jdo.fetch-group name="Project.propertySet" fields="propertySet"
 * @jdo.fetch-group name="Project.projectManager" fields="projectManager"
 * @jdo.fetch-group name="Project.members" fields="members"
 *
 * @jdo.fetch-group name="Issue.project" fields="name"
 *
 **/
public class Project
implements Serializable, Comparable<Project>
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Project.class);

	public static final String FETCH_GROUP_DESCRIPTION = "Project.description";
	public static final String FETCH_GROUP_NAME = "Project.name";
	public static final String FETCH_GROUP_PARENT_PROJECT = "Project.parentProject";
	public static final String FETCH_GROUP_SUBPROJECTS = "Project.subProjects";
	public static final String FETCH_GROUP_PROPERTY_SET = "Project.propertySet";
	public static final String FETCH_GROUP_PROJECT_TYPE = "Project.projectType";
	public static final String FETCH_GROUP_PROJECT_MANAGER = "Project.projectManager";
	public static final String FETCH_GROUP_MEMBERS = "Project.members";

	public static final ProjectID PROJECT_ID_DEFAULT = ProjectID.create(Organisation.DEV_ORGANISATION_ID, -1);

	/**
	 * This is the organisationID to which the project belongs. Within one organisation,
	 * all the projects have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 *
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
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		load-fetch-group="all"
	 */
	private User projectManager;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="User"
	 *		table="JFireIssueTracking_Project_members"
	 *
	 * @jdo.join
	 */
	private Set<User> members;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Project parentProject;

	/**
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean active = true;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date updateTimestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date contractDate;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date finishDate;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private ProjectCost projectCost;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PropertySet propertySet;


	/**
	 * Returns the property set of this {@link Project}.
	 *
	 * @return The property set of this {@link Project}.
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}

	/**
	 * The scope of the StructLocal by which the propertySet is build from.
	 *
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	private String structLocalScope;

	/**
	 * Returns the scope of the StructLocal by which the propertySet is build from.
	 * @return The scope of the StructLocal by which the propertySet is build from.
	 */
	public String getStructLocalScope() {
		return structLocalScope;
	}

	/**
	 * The scope of the Struct by which the propertySet is build from.
	 *
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	private String structScope;

	/**
	 * Returns the scope of the Struct by which the propertySet is build from.
	 * @return The scope of the Struct by which the propertySet is build from.
	 */
	public String getStructScope() {
		return structScope;
	}

	/**
	 * @deprecated Constructor exists only for JDO!
	 */
	@Deprecated
	protected Project() { }

	public Project(ProjectID projectID) {
		this(projectID.organisationID, projectID.projectID);
	}

	public Project(String organisationID, long projectID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.projectID = projectID;

		this.name = new ProjectName(this);
		this.description = new ProjectDescription(this);
//		this.projectCost = new ProjectCost(organisationID, IDGenerator.nextID(ProjectCost.class));
		
		subProjects = new HashSet<Project>();
		members = new HashSet<User>();

		this.createTimestamp = new Date();

		this.structScope = Struct.DEFAULT_SCOPE;
		this.structLocalScope = StructLocal.DEFAULT_SCOPE;
		this.propertySet = new PropertySet(
				organisationID, IDGenerator.nextID(PropertySet.class),
				Organisation.DEV_ORGANISATION_ID,
				Project.class.getName(), structScope, structLocalScope);
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

	public Set<User> getMembers() {
		return Collections.unmodifiableSet(members);
	}

	public void addMember(User user) {
		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		if (!user.getOrganisationID().equals(this.getOrganisationID()))
			throw new IllegalArgumentException("this.organisationID != user.organisationID");

		members.add(user);
	}

	public void addMembers(Collection<User> users) {
		for (User user : users) {
			addMember(user);
		}
	}

	public boolean removeMember(User user) {
		return members.remove(user);
	}

	public boolean removeMembers(Collection<User> users) {
		return members.removeAll(users);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setProjectManager(User user) {
		this.projectManager = user;
	}

	public User getProjectManager() {
		return projectManager;
	}

	public void setCreateTimestamp(Date createTimestamp) {
		this.createTimestamp = createTimestamp;
	}

	/**
	 * @return Returns the create timestamp.
	 */
	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	/**
	 * @return Returns the update timestamp.
	 */
	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setUpdateTimestamp(Date timestamp) {
		this.updateTimestamp = timestamp;
	}

	/**
	 * @return Returns the contact date.
	 */
	public Date getContactDate() {
		return contractDate;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setContactDate(Date contractDate) {
		this.contractDate = contractDate;
	}

	/**
	 * @return Returns the finish date.
	 */
	public Date getFinishDate() {
		return finishDate;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
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