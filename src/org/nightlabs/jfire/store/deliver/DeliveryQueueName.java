package org.nightlabs.jfire.store.deliver;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

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
public class DeliveryQueueName extends I18nText {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/** @jdo.field primary-key="true" */
	private long deliveryQueueID;
	
	/** @jdo.field persistence-modifier="persistent" */
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
