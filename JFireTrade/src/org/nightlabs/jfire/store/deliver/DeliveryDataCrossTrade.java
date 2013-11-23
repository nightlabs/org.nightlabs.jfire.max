package org.nightlabs.jfire.store.deliver;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_DeliveryDataCrossTrade")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DeliveryDataCrossTrade
extends DeliveryData
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String clientDeliveryOrganisationID;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long clientDeliveryDeliveryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String serverDeliveryOrganisationID;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
