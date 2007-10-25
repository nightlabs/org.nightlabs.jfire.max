package org.nightlabs.jfire.store.deliver;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.DeliveryData"
 *		detachable="true"
 *		table="JFireTrade_DeliveryDataCrossTrade"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryDataCrossTrade
extends DeliveryData
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String clientDeliveryOrganisationID;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long clientDeliveryDeliveryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String serverDeliveryOrganisationID;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long serverDeliveryDeliveryID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DeliveryDataCrossTrade() { }

	public DeliveryDataCrossTrade(Delivery delivery, Delivery clientDelivery, Delivery serverDelivery)
	{
		super(delivery);

		clientDeliveryOrganisationID = clientDelivery.getOrganisationID();
		clientDeliveryDeliveryID = clientDelivery.getDeliveryID();

		serverDeliveryOrganisationID = serverDelivery.getOrganisationID();
		serverDeliveryDeliveryID = serverDelivery.getDeliveryID();
	}

	public String getClientDeliveryOrganisationID()
	{
		return clientDeliveryOrganisationID;
	}
	public long getClientDeliveryDeliveryID()
	{
		return clientDeliveryDeliveryID;
	}

	public String getServerDeliveryOrganisationID()
	{
		return serverDeliveryOrganisationID;
	}
	public long getServerDeliveryDeliveryID()
	{
		return serverDeliveryDeliveryID;
	}
}
