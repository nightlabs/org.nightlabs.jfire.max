package org.nightlabs.jfire.store.deliver;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import org.nightlabs.jfire.store.deliver.id.DeliveryQueueNameID;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.DeliveryQueueNameID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryQueueName"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class field-order="organisationID, deliveryQueueID"
 *
 * @jdo.fetch-group name="DeliveryQueue.name" fields="deliveryQueue, names"
 */
@PersistenceCapable(
	objectIdClass=DeliveryQueueNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_DeliveryQueueName")
@FetchGroups(
	@FetchGroup(
		name="DeliveryQueue.name",
		members={@Persistent(name="deliveryQueue"), @Persistent(name="names")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DeliveryQueueName extends I18nText {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	/** @jdo.field primary-key="true" */
	@PrimaryKey
	private long deliveryQueueID;
	
	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DeliveryQueue deliveryQueue;
	
	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireTrade_DeliveryQueueName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryQueueName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, String> names;
	
	/** @deprecated Only for JDO! */
	@Deprecated
	protected DeliveryQueueName() {}
	
	public DeliveryQueueName(DeliveryQueue deliveryQueue) {
		this.deliveryQueueID = deliveryQueue.getDeliveryQueueID();
		this.organisationID = deliveryQueue.getOrganisationID();
		this.deliveryQueue = deliveryQueue;
		this.names = new HashMap<String, String>();
	}
	
	@Override
	protected String getFallBackValue(String languageID) {
		return String.valueOf(deliveryQueueID);
	}

	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}
}
