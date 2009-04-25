package org.nightlabs.jfire.department;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import org.nightlabs.jfire.department.id.DepartmentNameID;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 *  An extended class of {@link I18nText} that represents the {@link Department}'s name. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.department.id.DepartmentNameID"
 *		detachable="true"
 *		table="JFireDepartment_DepartmentName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, departmentID"
 *
 * @jdo.fetch-group name="Department.name" fetch-groups="default" fields="department, names"
 */
@PersistenceCapable(
	objectIdClass=DepartmentNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDepartment_DepartmentName")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="Department.name",
		members={@Persistent(name="department"), @Persistent(name="names")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DepartmentName
extends I18nText{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the department's name belongs. Within one organisation,
	 * all the department's names have their organisation's ID stored here, thus it's the same
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Department department;

	/**
	 * key: String languageID<br/>
	 * value: String description
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		value-type="java.lang.String"
	 *		table="JFireDepartment_DepartmentName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireDepartment_DepartmentName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DepartmentName()
	{
	}

	public DepartmentName(Department department)
	{
		this.department = department;
		this.organisationID = department.getOrganisationID();
		departmentID = department.getDepartmentID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public Department getDepartment() {
		return department;
	}

	public long getDepartmentID() {
		return departmentID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return departmentID == 0 ? languageID : Long.toString(departmentID);
	}
}