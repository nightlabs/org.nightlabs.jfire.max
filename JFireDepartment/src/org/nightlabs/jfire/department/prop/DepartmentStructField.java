package org.nightlabs.jfire.department.prop;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @author Chairat Kongarayawetchakun chairat[at]nightlabs[dot]de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class DepartmentStructField extends StructField<DepartmentDataField>
{
	private static final long serialVersionUID = 1L;

	protected DepartmentStructField() { }

	public DepartmentStructField(StructBlock structBlock, StructFieldID structFieldID) {
		super(structBlock, structFieldID);
	}

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
