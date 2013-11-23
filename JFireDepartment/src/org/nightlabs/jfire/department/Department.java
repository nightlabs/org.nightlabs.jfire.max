package org.nightlabs.jfire.department;

import java.io.Serializable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.department.id.DepartmentID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * The {@link Department} class represents a department. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun chairat[at]nightlabs[dot]de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.department.id.DepartmentID"
 *		detachable = "true"
 *		table="JFireDepartment_Department"
 *
 * @jdo.create-objectid-class field-order="organisationID, departmentID"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="Department.description" fields="description"
 * @jdo.fetch-group name="Department.name" fetch-groups="default" fields="name"
 * 
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fields="name"
 */
@PersistenceCapable(
	objectIdClass=DepartmentID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDepartment_Department")
@FetchGroups({
	@FetchGroup(
		name=Department.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),
	@FetchGroup(
		fetchGroups={"default"},
		name=Department.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name="FetchGroupsProp.fullData",
		members=@Persistent(name="name"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Department 
implements Serializable, Comparable<Department>
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_DESCRIPTION = "Department.description";
	public static final String FETCH_GROUP_NAME = "Department.name";

	/**
	 * This is the organisationID to which the department belongs. Within one organisation,
	 * all the departments have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
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
	private long departmentID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="department"
	 */
	@Persistent(
		dependent="true",
		mappedBy="department",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private DepartmentName name;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		dependent="true"
	 * 		mapped-by="department"
	 */
	@Persistent(
		dependent="true",
		mappedBy="department",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private DepartmentDescription description;
	
	/**
	 * @deprecated Only for JDO!
	 */
	protected Department(){}

	/**
	 * Constructs a new department.
	 * @param organisationID the first part of the composite primary key - referencing the organisation which owns this <code>Department</code>.
	 * @param departmentID the second part of the composite primary key. Use {@link IDGenerator#nextID(Class)} with <code>Department.class</code> to create an id.
	 */
	public Department(String organisationID, long departmentID){
		this.organisationID = organisationID;
		this.departmentID = departmentID;
		this.name = new DepartmentName(this);
		this.description = new DepartmentDescription(organisationID, departmentID, this);
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
	public void setDepartmentID(long departmentID) {
		this.departmentID = departmentID;
	}

	/**
	 * @return Returns the departmentID.
	 */
	public long getDepartmentID()
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

	/**
	 * Returns the department description.
	 * @return the description of the department
	 */
	public DepartmentDescription getDescription() {
		return description;
	}

	/**
	 * Sets the {@link DepartmentDescription}.
	 * @param description the description to set
	 */
	public void setDescription(DepartmentDescription description) {
		this.description = description;
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

	@Override
	public int compareTo(Department o) {
		return this.name.getText().compareTo(o.getName().getText());
	}
}
