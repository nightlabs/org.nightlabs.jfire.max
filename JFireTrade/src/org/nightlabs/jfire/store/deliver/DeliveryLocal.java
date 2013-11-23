package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.store.deliver.id.DeliveryLocalID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;


/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.DeliveryLocalID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, deliveryID"
 */
@PersistenceCapable(
	objectIdClass=DeliveryLocalID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_DeliveryLocal")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DeliveryLocal implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long deliveryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Delivery delivery;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.store.deliver.DeliveryActionHandler"
	 *		table="JFireTrade_DeliveryLocal_deliveryActionHandlers"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryLocal_deliveryActionHandlers",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<DeliveryActionHandler> deliveryActionHandlers = new HashSet<DeliveryActionHandler>();

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Set<DeliveryActionHandler> _deliveryActionHandlers;

	public DeliveryLocal(Delivery delivery) {
		super();
		this.organisationID = delivery.getOrganisationID();
		this.deliveryID = delivery.getDeliveryID();
		this.delivery = delivery;
		delivery.setDeliveryLocal(this);
	}

	/**
	 * Adds a {@link DeliveryActionHandler} that is triggered in important stages of the lifecycle of a delivery.
	 * @param deliveryActionHandler The {@link DeliveryActionHandler} to be added.
	 */
	public void addDeliveryActionHandler(DeliveryActionHandler deliveryActionHandler) {
		deliveryActionHandlers.add(deliveryActionHandler);
	}

	/**
	 * Removes a {@link DeliveryActionHandler} from this delivery.
	 * @param deliveryActionHandler The {@link DeliveryActionHandler} to be removed.
	 */
	public void removeDeliveryActionHandler(DeliveryActionHandler deliveryActionHandler) {
		deliveryActionHandlers.remove(deliveryActionHandler);
	}

	/**
	 * Returns the set of {@link DeliveryActionHandler}s associated with this delivery.
	 * @return The set of {@link DeliveryActionHandler}s associated with this delivery.
	 */
	public Set<DeliveryActionHandler> getDeliveryActionHandlers() {
		if (_deliveryActionHandlers == null)
			_deliveryActionHandlers = Collections.unmodifiableSet(deliveryActionHandlers);

		return _deliveryActionHandlers;
	}
}
