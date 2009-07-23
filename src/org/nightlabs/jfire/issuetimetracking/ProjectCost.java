package org.nightlabs.jfire.issuetimetracking;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.issuetimetracking.id.ProjectCostID;
import org.nightlabs.util.Util;

/**
 * The {@link ProjectCost} class represents an cost information of each {@link Project}s. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issuetimetracking.id.ProjectCostID"
 *		detachable="true"
 *		table="JFireIssueTimeTracking_ProjectCost"
 *
 * @jdo.create-objectid-class field-order="organisationID, projectCostID"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.query
 *		name="getProjectCostsByProjectID"
 *		query="SELECT
 *			WHERE
 *				this.project.organisationID == :organisationID &&
 *				this.project.projectID == :projectID"
 *
 * @jdo.fetch-group name="ProjectCost.project" fields="project"
 * @jdo.fetch-group name="ProjectCost.currency" fields="currency"
 * @jdo.fetch-group name="ProjectCost.defaultCost" fields="defaultCost"
 * @jdo.fetch-group name="ProjectCost.defaultRevenue" fields="defaultRevenue"
 * @jdo.fetch-group name="ProjectCost.projectCostValue" fields="projectCostValue"
 */
@PersistenceCapable(
	objectIdClass=ProjectCostID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireIssueTimeTracking_ProjectCost")
@FetchGroups({
	@FetchGroup(
		name=ProjectCost.FETCH_GROUP_PROJECT,
		members=@Persistent(name="project")),
	@FetchGroup(
		name=ProjectCost.FETCH_GROUP_CURRENCY,
		members=@Persistent(name="currency")),
	@FetchGroup(
		name=ProjectCost.FETCH_GROUP_DEFAULT_COST,
		members=@Persistent(name="defaultCost")),
	@FetchGroup(
		name=ProjectCost.FETCH_GROUP_DEFAULT_REVENUE,
		members=@Persistent(name="defaultRevenue")),
	@FetchGroup(
		name=ProjectCost.fETCH_GROUP_PROJECT_COST_VALUES,
		members=@Persistent(name="projectCostValues"))
})
@Queries(
	@Query(
		name="getProjectCostsByProjectID",
		value="SELECT WHERE this.project.organisationID == :organisationID && this.project.projectID == :projectID")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProjectCost 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_PROJECT = "ProjectCost.project";
	public static final String FETCH_GROUP_CURRENCY = "ProjectCost.currency";
	public static final String FETCH_GROUP_DEFAULT_COST = "ProjectCost.defaultCost";
	public static final String FETCH_GROUP_DEFAULT_REVENUE = "ProjectCost.defaultRevenue";
	public static final String fETCH_GROUP_PROJECT_COST_VALUES = "ProjectCost.projectCostValues";
	
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
	private long projectCostID;
	
	/**
	 * @jdo.field persistence-modifier="persistent" unique="true"
	 */
	@Element(unique="true")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Project project;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency;
	
//	/**
//	 * This field defines the project cost per user. The default value is the entry {@value User#USER_ID_OTHER}.
//	 * 
//	 * key: String {@link UserID#userID}<br/>
//	 * value: {@link ProjectCostValue}
//	 * 
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		default-fetch-group="true"
//	 *		table="JFireIssueTimeTracking_ProjectCost_user2ProjectCostMap"
//	 *		null-value="exception"
//	 *		dependent-value="true"
//	 * 
//	 * @jdo.join
//	 */
//	@Join
//	@Persistent(
//		nullValue=NullValue.EXCEPTION,
//		table="JFireIssueTimeTracking_ProjectCost_user2ProjectCostMap",
//		defaultFetchGroup="true",
//		persistenceModifier=PersistenceModifier.PERSISTENT)
//	@Value(dependent="true")
//	private Map<String, ProjectCostValue> user2ProjectCostMap;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="ProjectCostValue"
	 *		dependent-element="true"
	 *		mapped-by="projectCost"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="projectCost",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<ProjectCostValue> projectCostValues;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price defaultCost;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price defaultRevenue;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected ProjectCost()	{}

	/**
	 * Constructs a new project cost.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssuePriority</code>.
	 * @param projectCostID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>Project.class</code> to create an id.
	 */
	public ProjectCost(Project project, Currency currency)
	{
		this.organisationID = IDGenerator.getOrganisationID();
		this.projectCostID = IDGenerator.nextID(ProjectCost.class);
		
		this.project = project;
		this.currency = currency;
		
//		this.user2ProjectCostMap = new HashMap<String, ProjectCostValue>();
		
//		ProjectCostValue projectCostValue = new ProjectCostValue(this, IDGenerator.nextID(ProjectCostValue.class));
//		user2ProjectCostMap.put(User.USER_ID_OTHER, projectCostValue);
		
		this.defaultCost = new Price(project.getOrganisationID(), IDGenerator.nextID(Price.class), currency);
		this.defaultRevenue = new Price(project.getOrganisationID(), IDGenerator.nextID(Price.class), currency);
	}
	
	/**
	 * Returns the String.
	 * @return the String
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	public Project getProject() {
		return project;
	}

	public Currency getCurrency() {
		return currency;
	}
	
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public void addProjectCostValue(ProjectCostValue projectCostValue) {
		projectCostValues.add(projectCostValue);
	}
	
	public ProjectCostValue getProjectCostValue(String userID) {
		for (ProjectCostValue projectCostValue : projectCostValues) {
			if (projectCostValue.getUser() != null && projectCostValue.getUser().getUserID().equals(userID)) {
				return projectCostValue;
			}
		}
		return null;
	}
	
	public long getTotalCost() {
		long result = 0;
		for (ProjectCostValue projectCostValue : projectCostValues) {
			result += projectCostValue.getCost().getAmount();
		}
		return result;
	}
	
	public long getTotalRevenue() {
		long result = 0;
		for (ProjectCostValue projectCostValue : projectCostValues) {
			result += projectCostValue.getRevenue().getAmount();
		}
		return result;
	}
	
	public Price getDefaultCost() {
		return defaultCost;
	}
	
	public Price getDefaultRevenue() {
		return defaultRevenue;
	}
	
	public Collection<ProjectCostValue> getProjectCostValues() {
		return Collections.unmodifiableCollection(projectCostValues);
	}
	
	@Override
	/*
	 *
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ProjectCost)) return false;
		ProjectCost o = (ProjectCost) obj;
		return
		Util.equals(o.organisationID, this.organisationID) &&
		Util.equals(o.projectCostID, this.projectCostID);
	}

	@Override
	/*
	 *
	 */
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(projectCostID);
	}
}