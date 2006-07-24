package org.nightlabs.jfire.store;

import org.nightlabs.util.Utils;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.DeliveryNoteActionHandlerID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteActionHandler"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, deliveryNoteActionHandlerID"
 */
public class DeliveryNoteActionHandler
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String deliveryNoteActionHandlerID;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryNoteActionHandler() { }

	public DeliveryNoteActionHandler(String organisationID, String deliveryNoteActionHandlerID)
	{
		this.organisationID = organisationID;
		this.deliveryNoteActionHandlerID = deliveryNoteActionHandlerID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getDeliveryNoteActionHandlerID()
	{
		return deliveryNoteActionHandlerID;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof DeliveryNoteActionHandler))
			return false;

		DeliveryNoteActionHandler o = (DeliveryNoteActionHandler) obj;

		return
				Utils.equals(this.organisationID, o.organisationID) &&
				Utils.equals(this.deliveryNoteActionHandlerID, o.deliveryNoteActionHandlerID);
	}

	@Override
	public int hashCode()
	{
		return Utils.hashCode(organisationID) ^ Utils.hashCode(deliveryNoteActionHandlerID);
	}
}
