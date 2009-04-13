package org.nightlabs.jfire.department.prop;

import org.nightlabs.jfire.department.Department;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;

/**
 * {@link DataField} that stores a department. 
 * 
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 * 
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		detachable="true"
 *		table="JFireDepartment_Prop_DepartmentDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fields="department"
 */
public class DepartmentDataField extends DataField
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	private Department department;

	/**
	 * For JDO only.
	 */
	protected DepartmentDataField() { }

	public DepartmentDataField(DataBlock block, StructField<? extends DataField> field) {
		super(block, field);
	}

	protected DepartmentDataField(String organisationID, long propertySetID, DepartmentDataField cloneField) {
		super(organisationID, propertySetID, cloneField);
		this.department = cloneField.department;
	}

	@Override
	public DataField cloneDataField(PropertySet propertySet) {
		return new DepartmentDataField(propertySet.getOrganisationID(), propertySet.getPropertySetID(), this);
	}

	@Override
	public boolean isEmpty() {
		return department == null;
	}

	@Override
	public Object getData() {
		return getDepartment();
	}

	@Override
	public void setData(Object data) {
		setDepartment((Department) data);
	}
	
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}

	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return Department.class.isAssignableFrom(inputType); // TODO is this correct? Is this even used?
	}

}
