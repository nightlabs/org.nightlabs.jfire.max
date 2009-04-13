package org.nightlabs.jfire.department.prop;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;

/**
 * @author Chairat Kongarayawetchakun chairat[at]nightlabs[dot]de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class DepartmentStructField extends StructField<DepartmentDataField>
{
	private static final long serialVersionUID = 1L;

	protected DepartmentStructField() { }

	public DepartmentStructField(StructBlock structBlock) {
		super(structBlock);
	}

	@Override
	protected DepartmentDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new DepartmentDataField(dataBlock, this);
	}

	@Override
	public Class<DepartmentDataField> getDataFieldClass() {
		return DepartmentDataField.class;
	}

}
