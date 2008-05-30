package org.nightlabs.jfire.prop.html;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *                          detachable="true" table="JFireBase_Prop_HTMLStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default"
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class HTMLStructField extends StructField<HTMLDataField>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new HTMLStructField instance.
	 */
	public HTMLStructField()
	{
		super();
	}

	/**
	 * Create a new HTMLStructField instance.
	 */
	public HTMLStructField(StructBlock block, String fieldOrganisationID, String fieldID)
	{
		super(block, fieldOrganisationID, fieldID);
	}

	/**
	 * Create a new HTMLStructField instance.
	 */
	public HTMLStructField(StructBlock block, StructFieldID structFieldID)
	{
		super(block, structFieldID);
	}

	/**
	 * Create a new HTMLStructField instance.
	 */
	public HTMLStructField(StructBlock block)
	{
		super(block);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#createDataFieldInstanceInternal(org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	protected HTMLDataField createDataFieldInstanceInternal(DataBlock dataBlock)
	{
		return new HTMLDataField(dataBlock, this);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#getDataFieldClass()
	 */
	@Override
	public Class<HTMLDataField> getDataFieldClass()
	{
		return HTMLDataField.class;
	}
}
