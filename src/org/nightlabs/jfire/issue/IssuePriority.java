package org.nightlabs.jfire.issue;

import java.io.Serializable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;

/**
 * The {@link IssuePriority} class represents a priority of each {@link Issue}s. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.id.IssuePriorityID"
 *		detachable = "true"
 *		table="JFireIssueTracking_IssuePriority"
 *
 * @jdo.create-objectid-class field-order="organisationID, issuePriorityID"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="IssuePriority.name" fetch-groups="default" fields="name"
 */
public class IssuePriority
implements Serializable{

	public static final String FETCH_GROUP_NAME = "IssuePriority.name";

	private static final long serialVersionUID = 1L;
	
	/**
	 * This is the organisationID to which the issue priority belongs. Within one organisation,
	 * all the issue priorities have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String issuePriorityID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="issuePriority"
	 */
	private IssuePriorityName name;

	/**
	 * @deprecated Only for JDO!!!!
	 */
	protected IssuePriority()
	{
	}

	/**
	 * Constructs a new issue priority.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>IssuePriority</code>.
	 * @param issuePriorityID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>IssuePriority.class</code> to create an id.
	 */
	public IssuePriority(String organisationID, String issuePriorityID){
		if (issuePriorityID == null)
			throw new IllegalArgumentException("issuePriorityID must not be null!");

		this.organisationID = organisationID;
		this.issuePriorityID = issuePriorityID;
		this.name = new IssuePriorityName(this);
	}

	/**
	 * 
	 * @return
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return Returns the issuePriorityID
	 */
	public String getIssuePriorityID()
	{
		return issuePriorityID;
	}

	/**
	 * 
	 * @return
	 */
	public IssuePriorityName getIssuePriorityText()
	{
		return name;
	}

	@Override
	/*
	 * 
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IssuePriority)) return false;
		IssuePriority o = (IssuePriority) obj;
		return 
		Util.equals(this.organisationID, o.organisationID) &&
		Util.equals(o.issuePriorityID, this.issuePriorityID);
	}

	@Override
	/*
	 * 
	 */
	public int hashCode()
	{
		return 
		(31 * Util.hashCode(organisationID)) + 
		Util.hashCode(issuePriorityID);
	}
}