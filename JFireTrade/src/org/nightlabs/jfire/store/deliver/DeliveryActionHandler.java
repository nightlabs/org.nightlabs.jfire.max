package org.nightlabs.jfire.store.deliver;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.store.deliver.id.DeliveryActionHandlerID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Inheritance;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.DeliveryActionHandlerID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryActionHandler"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, deliveryActionHandlerID"
 */
@PersistenceCapable(
	objectIdClass=DeliveryActionHandlerID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_DeliveryActionHandler")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DeliveryActionHandler {
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String deliveryActionHandlerID;
	
	protected DeliveryActionHandler() {
	}

	public DeliveryActionHandler(String organisationID, String deliveryActionHandlerID) {
		super();
		this.organisationID = organisationID;
		this.deliveryActionHandlerID = deliveryActionHandlerID;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getDeliveryActionHandlerID() {
		return deliveryActionHandlerID;
	}
	
	/**
	 * This method is called by {@link Store#deliverBegin(org.nightlabs.jfire.security.User, DeliveryData)} on <b>precursor delivery</b> of the
	 * currently processed delivery after the {@link ServerDeliveryProcessor} for this current delivery has been triggered.
	 * @param currentDelivery The {@link Delivery} that is currently processed.
	 * @param precursorDelivery The precursor {@link Delivery} of the {@link Delivery} that is currently processed.
	 * 
	 * @throws DeliveryException
	 */
	public void onFollowUpDeliverBegin(Delivery currentDelivery, Delivery precursorDelivery) throws DeliveryException {
	}
	
	/**
	 * This method is called by {@link Store#deliverDoWork(org.nightlabs.jfire.security.User, DeliveryData)} on <b>precursor delivery</b> of the
	 * currently processed delivery after the {@link ServerDeliveryProcessor} for this current delivery has been triggered.
	 * @param currentDelivery The {@link Delivery} that is currently processed.
	 * @param precursorDelivery The precursor {@link Delivery} of the {@link Delivery} that is currently processed.
	 * 
	 * @throws DeliveryException
	 */
	public void onFollowUpDeliverDoWork(Delivery currentDelivery, Delivery precursorDelivery) throws DeliveryException {
	}
	
	/**
	 * This method is called by {@link Store#deliverEnd(org.nightlabs.jfire.security.User, DeliveryData)} on the <b>precursor delivery</b> of the
	 * currently processed delivery after the {@link ServerDeliveryProcessor} for this current delivery has been triggered.
	 * <p>
	 * You should try to avoid throwing an Exception here, because it is too late for a roll-back in an external delivery system!
	 * If you do risky things that might fail, you should better override {@link #onFollowUpDeliverDoWork(DeliveryData)} and do them
	 * there! The best solution, is to ensure already in {@link #onFollowUpDeliverBegin(DeliveryData)} that a delivery will succeed.
	 * </p>
	 * <p>
	 * An exception at this stage (i.e. thrown by this method) will require manual clean-up by an operator!
	 * </p>
	 * @param currentDelivery The {@link Delivery} that is currently processed.
	 * @param precursorDelivery The precursor {@link Delivery} of the {@link Delivery} that is currently processed.
	 * 
	 * @throws DeliveryException
	 */
	public void onFollowUpDeliverEnd(Delivery currentDelivery, Delivery precursorDelivery) throws DeliveryException {
	}
	
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deliveryActionHandlerID == null) ? 0 : deliveryActionHandlerID.hashCode());
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DeliveryActionHandler other = (DeliveryActionHandler) obj;
		if (deliveryActionHandlerID == null) {
			if (other.deliveryActionHandlerID != null)
				return false;
		} else if (!deliveryActionHandlerID.equals(other.deliveryActionHandlerID))
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}
}
