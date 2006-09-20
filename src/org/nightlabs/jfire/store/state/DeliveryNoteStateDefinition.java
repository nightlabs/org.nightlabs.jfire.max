package org.nightlabs.jfire.store.state;

import java.io.Serializable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.DeliveryNoteLocal;
import org.nightlabs.jfire.store.state.id.DeliveryNoteStateDefinitionID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.state.id.DeliveryNoteStateDefinitionID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteStateDefinition"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryNoteStateDefinitionID"
 *
 * @jdo.fetch-group name="DeliveryNoteStateDefinition.name" fields="name"
 * @jdo.fetch-group name="DeliveryNoteStateDefinition.description" fields="description"
 */
public class DeliveryNoteStateDefinition
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final DeliveryNoteStateDefinitionID DELIVERY_NOTE_STATE_DEFINITION_ID_CREATED = DeliveryNoteStateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "created");
	public static final DeliveryNoteStateDefinitionID DELIVERY_NOTE_STATE_DEFINITION_ID_FINALIZED = DeliveryNoteStateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "finalized");
	public static final DeliveryNoteStateDefinitionID DELIVERY_NOTE_STATE_DEFINITION_ID_BOOKED = DeliveryNoteStateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "booked");
	public static final DeliveryNoteStateDefinitionID DELIVERY_NOTE_STATE_DEFINITION_ID_CANCELLED = DeliveryNoteStateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "cancelled");

	public static final DeliveryNoteStateDefinitionID DELIVERY_NOTE_STATE_DEFINITION_ID_DELIVERED = DeliveryNoteStateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "delivered");
//	public static final DeliveryNoteStateDefinitionID DELIVERY_NOTE_STATE_DEFINITION_ID_UNDELIVERABLE = DeliveryNoteStateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "undeliverable");

	public static final String FETCH_GROUP_NAME = "DeliveryNoteStateDefinition.name";
	public static final String FETCH_GROUP_DESCRIPTION = "DeliveryNoteStateDefinition.description";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String deliveryNoteStateDefinitionID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="deliveryNoteStateDefinition"
	 */
	private DeliveryNoteStateDefinitionName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="deliveryNoteStateDefinition"
	 */
	private DeliveryNoteStateDefinitionDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean publicState = false;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryNoteStateDefinition() { }

	public DeliveryNoteStateDefinition(DeliveryNoteStateDefinitionID deliveryNoteStateDefinitionID)
	{
		this(deliveryNoteStateDefinitionID.organisationID, deliveryNoteStateDefinitionID.deliveryNoteStateDefinitionID);
	}

	public DeliveryNoteStateDefinition(String organisationID, String deliveryNoteStateDefinitionID)
	{
		this.organisationID = organisationID;
		this.deliveryNoteStateDefinitionID = deliveryNoteStateDefinitionID;
		this.name = new DeliveryNoteStateDefinitionName(this);
		this.description = new DeliveryNoteStateDefinitionDescription(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getDeliveryNoteStateDefinitionID()
	{
		return deliveryNoteStateDefinitionID;
	}

	public static String getPrimaryKey(String organisationID, String deliveryNoteStateDefinitionID)
	{
		return organisationID + '/' + deliveryNoteStateDefinitionID;
	}

	public DeliveryNoteStateDefinitionName getName()
	{
		return name;
	}

	public DeliveryNoteStateDefinitionDescription getDescription()
	{
		return description;
	}

	/**
	 * If a state definition is marked as <code>publicState</code>, it will be exposed to other organisations
	 * by storing it in both the {@link DeliveryNoteLocal} and the {@link DeliveryNote} instance. If it is not public,
	 * it is only stored in the {@link DeliveryNoteLocal}.
	 *
	 * @return true, if it shall be registered in the non-local instance and therefore published to business partners.
	 */
	public boolean isPublicState()
	{
		return publicState;
	}

	/**
	 * This method creates a new {@link DeliveryNoteState} and registers it in the {@link DeliveryNote} and {@link DeliveryNoteLocal}.
	 * Note, that it won't be added to the {@link DeliveryNote} (but only to the {@link DeliveryNoteLocal}), if {@link #isPublicState()}
	 * returns false.
	 * <p>
	 * This method calls {@link #_createDeliveryNoteState(User, DeliveryNote)} in order to obtain the new instance. Override that method
	 * if you feel the need for subclassing {@link DeliveryNoteState}.
	 * </p>
	 * 
	 * @param user The user who is responsible for the action.
	 * @param deliveryNote The deliveryNote that is transitioned to the new state.
	 * @return Returns the new newly created DeliveryNoteState instance.
	 */
	public DeliveryNoteState createDeliveryNoteState(User user, DeliveryNote deliveryNote)
	{
		DeliveryNoteState deliveryNoteState = _createDeliveryNoteState(user, deliveryNote);

		deliveryNote.getDeliveryNoteLocal().setDeliveryNoteState(deliveryNoteState);

		if (isPublicState())
			deliveryNote.setDeliveryNoteState(deliveryNoteState);

		return deliveryNoteState;
	}

	/**
	 * This method creates an instance of {@link DeliveryNoteState}. It is called by {@link #createDeliveryNoteState(User, DeliveryNote)}.
	 * This method does NOT register anything. You should override this method if you want to subclass {@link DeliveryNoteState}.
	 */
	protected DeliveryNoteState _createDeliveryNoteState(User user, DeliveryNote deliveryNote)
	{
		return new DeliveryNoteState(user.getOrganisationID(), IDGenerator.nextID(DeliveryNoteState.class), user, deliveryNote, this);
	}
}
