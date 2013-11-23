package org.nightlabs.jfire.store.jbpm;


/**
 * This invocation triggers the transition specified by {@link JbpmConstantsDeliveryNote.Vendor#TRANSITION_NAME_BOOK}
 * on the vendor side. It is enqueued by {@link ActionHandlerFinalizeDeliveryNote}.
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class BookDeliveryNoteInvocation
//extends Invocation
{
//	private static final long serialVersionUID = 1L;
//
//	private DeliveryNoteID deliveryNoteID;
//
//	public BookDeliveryNoteInvocation(DeliveryNoteID deliveryNoteID)
//	{
//		this.deliveryNoteID = deliveryNoteID;
//	}
//
//	@Implement
//	public Serializable invoke()
//	throws Exception
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			if (State.hasState(pm, deliveryNoteID, JbpmConstantsDeliveryNote.Both.NODE_NAME_BOOKED)) // in case a manual booking has occured (though this should be more-or-less impossible in the short time)
//				return null;
//
//			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
//			try {
//				pm.getExtent(DeliveryNoteLocal.class);
//				DeliveryNoteLocal deliveryNoteLocal = (DeliveryNoteLocal) pm.getObjectById(DeliveryNoteLocalID.create(deliveryNoteID));
//				ProcessInstance processInstance = jbpmContext.getProcessInstance(deliveryNoteLocal.getJbpmProcessInstanceId());
//				processInstance.signal(JbpmConstantsDeliveryNote.Both.TRANSITION_NAME_BOOK);
//			} finally {
//				jbpmContext.close();
//			}
//		} finally {
//			pm.close();
//		}
//		return null;
//	}

}
