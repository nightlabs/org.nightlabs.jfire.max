package org.nightlabs.jfire.issue.project;

import java.io.Serializable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;

/**
 * The {@link Department} class represents a department of each {@link Project}s. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun chairat[at]nightlabs[dot]de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.issue.project.id.DepartmentID"
 *		detachable = "true"
 *		table="JFireIssueTracking_Department"
 *
 * @jdo.create-objectid-class field-order="organisationID, departmentID"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="Department.name" fetch-groups="default" fields="name"
 */
public class Department 
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "Department.name";

	/**
	 * This is the organisationID to which the department belongs. Within one organisation,
	 * all the departments have their organisation's ID stored here, thus it's the same
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
	private String departmentID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="department"
	 */
	private DepartmentName name;

	/**
	 * @deprecated Only for JDO!
	 */
	protected Department(){}

	/**
	 * Constructs a new department.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>Department</code>.
	 * @param departmentID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>Department.class</code> to create an id.
	 */
	public Department(String organisationID, String departmentID){
		if (departmentID == null)
			throw new IllegalArgumentException("departmentID must not be null!");
		this.organisationID = organisationID;
		this.departmentID = departmentID;
		this.name = new DepartmentName(this);
	}

	/**
	 * 
	 * @return
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * 
	 * @param organisationID
	 */
	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	/**
	 * 
	 * @param departmentID
	 */
	public void setDepartmentID(String departmentID) {
		this.departmentID = departmentID;
	}

	/**
	 * @return Returns the departmentID.
	 */
	public String getDepartmentID()
	{
		return departmentID;
	}

	/**
	 * @return Returns the departmentName.
	 */
	public DepartmentName getName()
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
		if (!(obj instanceof Department)) return false;
		Department o = (Department) obj;
		return 
		Util.equals(o.organisationID, this.organisationID) &&
		Util.equals(o.departmentID, this.departmentID);
	}

	@Override
	/*
	 * 
	 */
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(departmentID);
	}
}
