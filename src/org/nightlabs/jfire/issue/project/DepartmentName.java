package org.nightlabs.jfire.issue.project;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 *  An extended class of {@link I18nText} that represents the {@link Department}'s name. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.issue.project.id.DepartmentNameID"
 *		detachable="true"
 *		table="JFireIssueTracking_DepartmentName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, departmentID"
 *
 * @jdo.fetch-group name="Department.name" fetch-groups="default" fields="department, names"
 */
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
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private String departmentID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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
	 *		table="JFireIssueTracking_DepartmentName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
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

	public String getDepartmentID() {
		return departmentID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return departmentID.equals("") ? languageID : departmentID;
	}
}