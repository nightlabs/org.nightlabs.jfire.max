package org.nightlabs.jfire.store.state;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.state.id.DeliveryNoteStateID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteState"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryNoteStateID"
 *
 * @jdo.fetch-group name="DeliveryNoteState.user" fields="user"
 * @jdo.fetch-group name="DeliveryNoteState.deliveryNote" fields="deliveryNote"
 * @jdo.fetch-group name="DeliveryNoteState.deliveryNoteStateDefinition" fields="deliveryNoteStateDefinition"
 */
public class DeliveryNoteState
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long deliveryNoteStateID;


	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private User user;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private DeliveryNote deliveryNote;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private DeliveryNoteStateDefinition deliveryNoteStateDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Date createDT;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryNoteState() { }

	public DeliveryNoteState(String organisationID, long deliveryNoteStateID, User user, DeliveryNote deliveryNote, DeliveryNoteStateDefinition deliveryNoteStateDefinition)
	{
		this.organisationID = organisationID;
		this.deliveryNoteStateID = deliveryNoteStateID;
		this.user = user;
		this.deliveryNote = deliveryNote;
		this.deliveryNoteStateDefinition = deliveryNoteStateDefinition;
		this.createDT = new Date();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getDeliveryNoteStateID()
	{
		return deliveryNoteStateID;
	}
	public User getUser()
	{
		return user;
	}
	public DeliveryNote getDeliveryNote()
	{
		return deliveryNote;
	}
	public DeliveryNoteStateDefinition getDeliveryNoteStateDefinition()
	{
		return deliveryNoteStateDefinition;
	}
	public Date getCreateDT()
	{
		return createDT;
	}

}
