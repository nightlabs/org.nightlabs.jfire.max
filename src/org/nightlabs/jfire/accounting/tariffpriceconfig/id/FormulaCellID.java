/*
 * Created on Jan 12, 2005
 */
package org.nightlabs.jfire.accounting.tariffpriceconfig.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class FormulaCellID extends BaseObjectID
{

	public String organisationID;
	public long priceConfigID;
	public long formulaID;

	public FormulaCellID() { }

	public FormulaCellID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static FormulaCellID create(String organisationID, long priceConfigID, long formulaID)
	{
		FormulaCellID n = new FormulaCellID();
		n.organisationID = organisationID;
		n.priceConfigID = priceConfigID;
		n.formulaID = formulaID;
		return n;
	}

}
