package org.nightlabs.jfire.issuetimetracking;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * The {@link ProjectCost} class represents an cost information of each {@link Project}s. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issuetimetracking.id.ProjectCostID"
 *		detachable = "true"
 *		table="JFireIssueTimeTracking_ProjectCost"
 *
 * @jdo.create-objectid-class field-order="organisationID, projectCostID"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 */
public class ProjectCost 
implements Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	 * This is the organisationID to which the information belongs. Within one organisation,
	 * all the project information have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long projectCostID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price cost;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price revenue;
	
	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected ProjectCost()
	{
	}

	/**
	 * Constructs a new project cost.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssuePriority</code>.
	 * @param projectCostID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>ProjectCost.class</code> to create an id.
	 */
	public ProjectCost(String organisationID, long projectCostID){
		this.organisationID = organisationID;
		this.projectCostID = projectCostID;
	}
	
	/**
	 * Returns the String.
	 * @return the String
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * Returns long.
	 * @return the long
	 */
	public long getProjectCostID() {
		return projectCostID;
	}
	
	/**
	 * Sets the {@link Price}.
	 * @param cost the cost
	 */
	public void setCost(Price cost) {
		this.cost = cost;
	}
	
	/**
	 * Returns the {@link Price}.
	 * @return the {@link Price}
	 */
	public Price getCost() {
		return cost;
	}
	
	/**
	 * Sets the revenue.
	 * @param revenue the revenue
	 */
	public void setRevenue(Price revenue) {
		this.revenue = revenue;
	}
	
	/**
	 * Returns the {@link Price}.
	 * @return the {@link Price}
	 */
	public Price getRevenue() {
		return revenue;
	}
}
