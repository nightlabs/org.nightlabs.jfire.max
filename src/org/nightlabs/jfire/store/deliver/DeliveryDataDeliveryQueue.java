package org.nightlabs.jfire.store.deliver;
 
/**
 * @author Tobias Langner (tobias[dot]langner[at]nightlabs[dot]de)
 *
 * @jdo.persistence-capable
 *    identity-type="application"
 *    persistence-capable-superclass="org.nightlabs.jfire.store.deliver.DeliveryData"
 *    detachable="true"
 *    table="JFireTrade_DeliveryDataDeliveryQueue"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryDataDeliveryQueue extends DeliveryData {
 
  private static final long serialVersionUID = 1L;
 
  /**
   * @jdo.field persistence-modifier="persistent"
   */
  private DeliveryQueue targetQueue;
 
  public DeliveryDataDeliveryQueue(Delivery delivery) {
    super(delivery);
  }
 
  public void setTargetQueue(DeliveryQueue targetQueue) {
    this.targetQueue = targetQueue;
  }
 
  public DeliveryQueue getTargetQueue() {
    return targetQueue;
  }
}