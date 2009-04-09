package org.nightlabs.jfire.department;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * An extended class of {@link I18nText} that represents the description created in an {@link Department}. 
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.department.id.DepartmentDescriptionID"
 *		detachable="true"
 *		table="JFireDepartment_DepartmentDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, departmentID"
 * 
 * @jdo.fetch-group name="Department.description" fields="department, descriptions"
 */ 
public class DepartmentDescription 
	extends I18nText
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the organisationID to which the department description belongs. Within one organisation,
	 * all the department descriptions have their organisation's ID stored here, thus it's the same
	 * value for all of them.
	 * 
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long departmentID;
	
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
	 *		default-fetch-group="true"
	 *		table="JFireDepartmentTracking_DepartmentDescription_descriptions"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 * @jdo.value-column sql-type="CLOB"
	 */
	protected Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DepartmentDescription()
	{
	}

	/**
	 * Constructs a new DepartmentDescription.
	 * @param department the department that this department description is made in 
	 */
	public DepartmentDescription(String organisationID, long departmentID, Department department)
	{
		this.department = department;
		this.organisationID = organisationID;
		this.departmentID = departmentID;
	}

	/**
	 * Returns the organisation id.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the department id.
	 * @return the department id
	 */
	public long getDepartmentID() {
		return departmentID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return descriptions;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return department == null ? languageID : "";
	}
}