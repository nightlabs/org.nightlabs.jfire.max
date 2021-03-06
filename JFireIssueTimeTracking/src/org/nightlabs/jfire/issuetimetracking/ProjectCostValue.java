package org.nightlabs.jfire.issuetimetracking;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.issuetimetracking.id.ProjectCostValueID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * The {@link ProjectCostValue} class represents an cost information of each {@link Project}s. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issuetimetracking.id.ProjectCostValueID"
 *		detachable="true"
 *		table="JFireIssueTimeTracking_ProjectCostValue"
 *
 * @jdo.create-objectid-class field-order="organisationID, projectCostValueID"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.query
 *		name="getProjectCostValuesByProjectID"
 *		query="SELECT
 *			WHERE
 *				this.projectCost.organisationID == :organisationID &&
 *				this.projectCost.project.projectID == :projectID"
 *
 * @jdo.fetch-group name="ProjectCostValue.cost" fields="cost"
 * @jdo.fetch-group name="ProjectCostValue.revenue" fields="revenue"
 * @jdo.fetch-group name="ProjectCostValue.projectCost" fields="projectCost"
 * @jdo.fetch-group name="ProjectCostValue.user" fields="user"
 */
@PersistenceCapable(
	objectIdClass=ProjectCostValueID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTimeTracking_ProjectCostValue")
@FetchGroups({
	@FetchGroup(
		name=ProjectCostValue.FETCH_GROUP_COST,
		members=@Persistent(name="cost")),
	@FetchGroup(
		name=ProjectCostValue.FETCH_GROUP_REVENUE,
		members=@Persistent(name="revenue")),
	@FetchGroup(
		name=ProjectCostValue.FETCH_GROUP_PROJECT_COST,
		members=@Persistent(name="projectCost")),
	@FetchGroup(
			name=ProjectCostValue.FETCH_GROUP_USER,
			members=@Persistent(name="user"))
})
@Queries(
	@Query(
		name="getProjectCostValuesByProjectID",
		value="SELECT WHERE this.projectCost.organisationID == :organisationID && this.projectCost.project.projectID == :projectID")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProjectCostValue
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_COST = "ProjectCostValue.cost";
	public static final String FETCH_GROUP_REVENUE = "ProjectCostValue.revenue";
	public static final String FETCH_GROUP_PROJECT_COST = "ProjectCostValue.projectCost";
	public static final String FETCH_GROUP_USER = "ProjectCostValue.user";

	/**
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
	private long projectCostValueID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProjectCost projectCost;

	/**
	 * @jdo.field persistence-modifier="persistent" unique="true"
	 */
	@Element(unique="true")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User user;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price cost;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price revenue;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected ProjectCostValue()
	{
	}

	/**
	 * Constructs a new project cost.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssuePriority</code>.
	 * @param projectCostID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>ProjectCostValue.class</code> to create an id.
	 */
	public ProjectCostValue(User user, ProjectCost projectCost, long projectCostValueID){
		this.organisationID = projectCost.getOrganisationID();
		this.projectCostValueID = projectCostValueID;
		
		this.user = user;
		this.projectCost = projectCost;
		
		this.cost = new Price(projectCost.getOrganisationID(), IDGenerator.nextID(Price.class), projectCost.getCurrency());
		this.revenue = new Price(projectCost.getOrganisationID(), IDGenerator.nextID(Price.class), projectCost.getCurrency());
	}

	/**
	 * Returns the String.
	 * @return the String
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	public long getProjectCostValueID() {
		return projectCostValueID;
	}
	
	public ProjectCost getProjectCost() {
		return projectCost;
	}
	
	/**
	 * Returns the {@link Price}.
	 * @return the {@link Price}
	 */
	public Price getCost() {
		return cost;
	}

	/**
	 * Returns the {@link Price}.
	 * @return the {@link Price}
	 */
	public Price getRevenue() {
		return revenue;
	}
	
	/**
	 * Returns the {@link Price}.
	 * @return the {@link Price}
	 */
	public User getUser() {
		return user;
	}
	
	@Override
	/*
	 *
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ProjectCostValue)) return false;
		ProjectCostValue o = (ProjectCostValue) obj;
		return
		Util.equals(o.organisationID, this.organisationID) &&
		Util.equals(o.projectCostValueID, this.projectCostValueID);
	}

	@Override
	/*
	 *
	 */
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(projectCostValueID);
	}
}