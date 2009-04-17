package org.nightlabs.jfire.issuetimetracking;

import java.io.Serializable;
import java.util.Map;

import javax.security.auth.spi.LoginModule;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

import com.sun.org.apache.bcel.internal.generic.IFNULL;

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
 * @jdo.fetch-group name="ProjectCost.cost" fields="cost"
 * @jdo.fetch-group name="ProjectCost.revenue" fields="revenue"
 * @jdo.fetch-group name="ProjectCost.project" fields="project"
 */
public class ProjectCost 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_COST = "ProjectCost.cost";
	public static final String FETCH_GROUP_REVENUE = "ProjectCost.revenue";
	public static final String FETCH_GROUP_PROJECT = "ProjectCost.project";
	
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
	 * @deprecated Only for JDO!!!!
	 */
	protected ProjectCost()	{}

	/**
	 * Constructs a new project cost.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssuePriority</code>.
	 * @param projectCostID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>ProjectCost.class</code> to create an id.
	 */
	public ProjectCost(Project project, Currency currency){
		this.project = project;
		
		this.organisationID = project.getOrganisationID();
		this.projectID = project.getProjectID();
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
	
	public double getTotalCostDoubleVale() {
		double result = 0;
		for (String userID : user2ProjectCostMap.keySet()) {
			ProjectCostValue projectCostValue = user2ProjectCostMap.get(userID);
			result += projectCostValue.getCost().getAmountAsDouble();
		}
		return result;
	}
	
	public double getTotalRevenueDoubleVale() {
		double result = 0;
		for (String userID : user2ProjectCostMap.keySet()) {
			ProjectCostValue projectCostValue = user2ProjectCostMap.get(userID);
			result += projectCostValue.getRevenue().getAmountAsDouble();
		}
		return result;
	}
}