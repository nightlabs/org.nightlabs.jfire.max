package org.nightlabs.jfire.issuetimetracking;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

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
 * @jdo.create-objectid-class field-order="organisationID, projectID"
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
 */
public class ProjectCost 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_PROJECT = "ProjectCost.project";
	public static final String FETCH_GROUP_CURRENCY = "ProjectCost.currency";
	public static final String FETCH_GROUP_DEFAULT_COST = "ProjectCost.defaultCost";
	public static final String FETCH_GROUP_DEFAULT_REVENUE = "ProjectCost.defaultRevenue";
	
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
	 * @jdo.field persistence-modifier="persistent" unique="true"
	 */
	private Project project;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;
	
	/**
	 * This field defines the project cost per user. The default value is the entry {@value User#USER_ID_OTHER}.
	 * 
	 * key: String {@link UserID#userID}<br/>
	 * value: {@link ProjectCostValue}
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		default-fetch-group="true"
	 *		table="JFireIssueTimeTracking_ProjectCost_user2ProjectCostMap"
	 *		null-value="exception"
	 *		dependent-value="true"
	 * 
	 * @jdo.join
	 */
	private Map<String, ProjectCostValue> user2ProjectCostMap;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price defaultCost;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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
	public ProjectCost(Project project, Currency currency){
		this.organisationID = project.getOrganisationID();
		this.projectID = project.getProjectID();
		this.project = project;

		this.currency = currency;
		
		this.user2ProjectCostMap = new HashMap<String, ProjectCostValue>();
		ProjectCostValue projectCostValue = new ProjectCostValue(this, IDGenerator.nextID(ProjectCostValue.class));
		user2ProjectCostMap.put(User.USER_ID_OTHER, projectCostValue);
		
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
	
	public long getProjectID() {
		return projectID;
	}
	
	public Project getProject() {
		return project;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void addProjectCostValue(String userID, ProjectCostValue projectCostValue) {
		user2ProjectCostMap.put(userID, projectCostValue);
	}
	
	public ProjectCostValue getProjectCostValue(String userID) {
		ProjectCostValue projectCostValue = user2ProjectCostMap.get(userID);
		return projectCostValue;
	}
	
	public long getTotalCost() {
		long result = 0;
		for (String userID : user2ProjectCostMap.keySet()) {
			ProjectCostValue projectCostValue = user2ProjectCostMap.get(userID);
			result += projectCostValue.getCost().getAmount();
		}
		return result;
	}
	
	public long getTotalRevenue() {
		long result = 0;
		for (String userID : user2ProjectCostMap.keySet()) {
			ProjectCostValue projectCostValue = user2ProjectCostMap.get(userID);
			result += projectCostValue.getRevenue().getAmount();
		}
		return result;
	}
	
	public Set<String> getUserIDs() {
		return user2ProjectCostMap.keySet();
	}
	
	public Price getDefaultCost() {
		return defaultCost;
	}
	
	public Price getDefaultRevenue() {
		return defaultRevenue;
	}
	
	public Collection<ProjectCostValue> getProjectCostValues() {
		return Collections.unmodifiableCollection(user2ProjectCostMap.values());
	}
}