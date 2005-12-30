/*
 * Created on May 31, 2005
 */
package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryID"
 *		detachable="true"
 *		table="JFireTrade_ModeOfDelivery"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ModeOfDelivery.name" fields="name"
 * @jdo.fetch-group name="ModeOfDelivery.flavours" fields="flavours"
 * @jdo.fetch-group name="ModeOfDelivery.this" fetch-groups="default" fields="flavours, name"
 */
public class ModeOfDelivery
implements Serializable
{
	public static final String FETCH_GROUP_NAME = "ModeOfDelivery.name";
	public static final String FETCH_GROUP_FLAVOURS = "ModeOfDelivery.flavours";
	public static final String FETCH_GROUP_THIS_MODE_OF_DELIVERY = "ModeOfDelivery.this";

	/**
	 * This is the ID (together with
	 * {@link org.nightlabs.jfire.organisation.Organisation#ROOT_ORGANISATIONID})
	 * of one of the default <tt>ModeOfDelivery</tt>s. You <b>must not</b> rely on
	 * the existence of this ModeOfDelivery as the user might choose to delete the
	 * default configuration.
	 * <p>
	 * It specifies the manual delivery outside of the control of the system (directly
	 * by the personal contact between vendor and customer).
	 */
	public static final String MODE_OF_DELIVERY_ID_MANUAL = "manual";

	/**
	 * This is the ID (together with
	 * {@link org.nightlabs.jfire.organisation.Organisation#ROOT_ORGANISATIONID})
	 * of one of the default <tt>ModeOfDelivery</tt>s. You <b>must not</b> rely on
	 * the existence of this ModeOfDelivery as the user might choose to delete the
	 * default configuration.
	 * <p>
	 * It specifies the non-delivery, means the delivery is simply postponed without
	 * any further action.
	 */
	public static final String MODE_OF_DELIVERY_ID_NON_DELIVERY = "nonDelivery";

	/**
	 * This is the ID (together with
	 * {@link org.nightlabs.jfire.organisation.Organisation#ROOT_ORGANISATIONID})
	 * of one of the default <tt>ModeOfDelivery</tt>s. You <b>must not</b> rely on
	 * the existence of this ModeOfDelivery as the user might choose to delete the
	 * default configuration.
	 * <p>
	 * It specifies the external delivery via a delivery agency like "Deutsche Post",
	 * "UPS" or similar.
	 */
	public static final String MODE_OF_DELIVERY_ID_MAILING_PHYSICAL = "mailing.physical";

	/**
	 * This is the ID (together with
	 * {@link org.nightlabs.jfire.organisation.Organisation#ROOT_ORGANISATIONID})
	 * of one of the default <tt>ModeOfDelivery</tt>s. You <b>must not</b> rely on
	 * the existence of this ModeOfDelivery as the user might choose to delete the
	 * default configuration.
	 * <p>
	 * It specifies the system controlled virtual delivery via online channels like
	 * eMail or similar. Usually the sensitive data would not be sent by email, but
	 * rather stored somewhere on the server where the customer can fetch it by a
	 * browser. Between organisations, a virtual delivery can be done directly within
	 * jfire.
	 */
	public static final String MODE_OF_DELIVERY_ID_MAILING_VIRTUAL = "mailing.virtual";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String modeOfDeliveryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="modeOfDelivery"
	 */
	private ModeOfDeliveryName name;

	/**
	 * key: String modeOfDeliveryFlavourPK (see {@link ModeOfDeliveryFlavour#getPrimaryKey()})<br/>
	 * value: ModeOfDeliveryFlavour modeOfDeliveryFlavour
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfDeliveryFlavour"
	 *		dependent="true"
	 *		mapped-by="modeOfDelivery"
	 *
	 * @jdo.key mapped-by="primaryKey"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
	 */
	private Map flavours = new HashMap();

	/**
	 * @deprecated Only for JDO!
	 */
	protected ModeOfDelivery() { }

	public ModeOfDelivery(String organisationID, String modeOfDeliveryID)
	{
		this.organisationID = organisationID;
		this.modeOfDeliveryID = modeOfDeliveryID;
		this.primaryKey = getPrimaryKey(organisationID, modeOfDeliveryID);
		this.name = new ModeOfDeliveryName(this);
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the modeOfDeliveryID.
	 */
	public String getModeOfDeliveryID()
	{
		return modeOfDeliveryID;
	}
	public static String getPrimaryKey(String organisationID, String modeOfDeliveryID)
	{
		return organisationID + '/' + modeOfDeliveryID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	/**
	 * @return Returns the name.
	 */
	public ModeOfDeliveryName getName()
	{
		return name;
	}

	/**
	 * @return Returns the flavours.
	 */
	public Collection getFlavours()
	{
		return flavours.values();
	}

	public ModeOfDeliveryFlavour getFlavour(String organisationID, String modeOfDeliveryFlavourID, boolean throwExceptionIfNotExistent)
	{
		return getFlavour(ModeOfDeliveryFlavour.getPrimaryKey(organisationID, modeOfDeliveryFlavourID), throwExceptionIfNotExistent);
	}
	public ModeOfDeliveryFlavour getFlavour(String modeOfDeliveryFlavourPK, boolean throwExceptionIfNotExistent)
	{
		ModeOfDeliveryFlavour res = (ModeOfDeliveryFlavour) flavours.get(modeOfDeliveryFlavourPK);
		if (throwExceptionIfNotExistent && res == null)
			throw new IllegalArgumentException("No ModeOfDeliveryFlavour with modeOfDeliveryFlavourPK=\"" + modeOfDeliveryFlavourPK + "\" in the ModeOfDelivery \"" + getPrimaryKey() + "\" existing!");
		return res;
	}

	/**
	 * Creates a new <tt>ModeOfDeliveryFlavour</tt> or returns a previously created one.
	 * 
	 * @param flavourID The local id (within this <tt>ModeOfDelivery</tt>) for the new flavour.
	 *
	 * @return The newly created <tt>ModeOfDeliveryFlavour</tt> (or an old instance, if already existent before).
	 */
	public ModeOfDeliveryFlavour createFlavour(String organisationID, String modeOfDeliveryFlavourID)
	{
		String modeOfDeliveryFlavourPK = ModeOfDeliveryFlavour.getPrimaryKey(organisationID, modeOfDeliveryFlavourID);
		ModeOfDeliveryFlavour res = (ModeOfDeliveryFlavour) flavours.get(modeOfDeliveryFlavourPK);
		if (res == null) {
			res = new ModeOfDeliveryFlavour(organisationID, modeOfDeliveryFlavourID, this);
			flavours.put(modeOfDeliveryFlavourPK, res);
		}
		return res;
	}
}
