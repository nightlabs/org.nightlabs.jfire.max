package org.nightlabs.jfire.store.deliver;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID;

public class ModeOfDeliveryConst
{
	/**
	 * This is the ID of one of the default <tt>ModeOfDelivery</tt>s. However, you
	 * <b>must not</b> rely on
	 * the existence of this ModeOfDelivery as the user might choose to delete the
	 * default configuration.
	 * <p>
	 * It specifies the manual delivery outside of the control of the system (directly
	 * by the personal contact between vendor and customer).
	 */
	public static final ModeOfDeliveryID MODE_OF_DELIVERY_ID_MANUAL = ModeOfDeliveryID.create(Organisation.DEVIL_ORGANISATION_ID, "manual");

	/**
	 * This is the ID of one of the default <tt>ModeOfDelivery</tt>s. However, you
	 * <b>must not</b> rely on
	 * the existence of this ModeOfDelivery as the user might choose to delete the
	 * default configuration.
	 * <p>
	 * It specifies the non-delivery, means the delivery is simply postponed without
	 * any further action.
	 */
	public static final ModeOfDeliveryID MODE_OF_DELIVERY_ID_NON_DELIVERY = ModeOfDeliveryID.create(Organisation.DEVIL_ORGANISATION_ID, "nonDelivery");

	/**
	 * This is the ID of one of the default <tt>ModeOfDelivery</tt>s. However, you
	 * <b>must not</b> rely on
	 * the existence of this ModeOfDelivery as the user might choose to delete the
	 * default configuration.
	 * <p>
	 * It specifies the external delivery via a delivery agency like "Deutsche Post",
	 * "UPS" or similar.
	 */
	public static final ModeOfDeliveryID MODE_OF_DELIVERY_ID_MAILING_PHYSICAL = ModeOfDeliveryID.create(Organisation.DEVIL_ORGANISATION_ID, "mailing.physical");

	/**
	 * This is the ID of one of the default <tt>ModeOfDelivery</tt>s. However, you
	 * <b>must not</b> rely on
	 * the existence of this ModeOfDelivery as the user might choose to delete the
	 * default configuration.
	 * <p>
	 * It specifies the system controlled virtual delivery via online channels like
	 * eMail or similar. Usually the sensitive data would not be sent by email, but
	 * rather stored somewhere on the server where the customer can fetch it by a
	 * browser. Between organisations, a virtual delivery can be done directly within
	 * jfire.
	 */
	public static final ModeOfDeliveryID MODE_OF_DELIVERY_ID_MAILING_VIRTUAL = ModeOfDeliveryID.create(Organisation.DEVIL_ORGANISATION_ID, "mailing.virtual");

	public static final ModeOfDeliveryID MODE_OF_DELIVERY_ID_JFIRE = ModeOfDeliveryID.create(Organisation.DEVIL_ORGANISATION_ID, "jfire");

	public static final ModeOfDeliveryFlavourID MODE_OF_DELIVERY_FLAVOUR_ID_JFIRE = ModeOfDeliveryFlavourID.create(Organisation.DEVIL_ORGANISATION_ID, "jfire");
}
